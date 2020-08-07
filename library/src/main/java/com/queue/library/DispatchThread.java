/*
 * Copyright (C)  Justson(https://github.com/Justson/Queue)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.queue.library;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.Process;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;
import java.util.concurrent.Exchanger;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author cenxiaozhong
 * @date 2019/2/15
 * @since 1.0.0
 */
public class DispatchThread implements Executor {

    private static final String TAG = DispatchThread.class.getSimpleName();
    private final Handler handler;
    private final Looper mLooper;
    private long ms = 5000L;
    private static final Object T_OBJECT = new Object();
    private MessageQueue mMessageQueue;

    private final SameThreadExchanger<Object> exchanger = new SameThreadExchanger<>();
    private static final ThreadLocal<Exchanger<Object>> EXCHANGER_THREAD_LOCAL = new ThreadLocal<Exchanger<Object>>() {
        @Override
        protected Exchanger<Object> initialValue() {
            return new DispatchPairExchanger<>();
        }
    };

    public static DispatchThread create() {
        return create("DispatchThread-" + ThreadConfig.getUniqueThreadId());
    }

    public static DispatchThread create(String name) {
        return create(name, Process.THREAD_PRIORITY_DEFAULT);
    }

    public static DispatchThread create(String name, int priority) {
        HandlerThread handlerThread = new HandlerThread(name, priority);
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        return new DispatchThread(looper);
    }

    private static <T> T requireNonNull(T obj) {
        if (obj == null)
            throw new NullPointerException();
        return obj;
    }

    /**
     * Only allow invoke in looper prepare thread
     */
    public DispatchThread() {
        this(requireNonNull(Looper.myLooper()));
    }

    public DispatchThread(Looper looper) {
        requireNonNull(looper);
        this.mLooper = looper;
        this.handler = new Handler(looper);
    }

    public void sendMessage(Message msg) {
        this.sendMessage(msg, 0);
    }

    public void sendMessage(Message msg, int delay) {
        if (delay <= 0) {
            handler.sendMessage(msg);
        } else {
            handler.sendMessageDelayed(msg, delay);
        }
    }

    public void cancelRunnable(Runnable runnable) {
        handler.removeCallbacks(runnable);
    }

    public <T> Exchanger<T> exchange(final Callable<T> callable) {
        try {
            if (Looper.myLooper() == getLooper()) {
                T t = null;
                try {
                    t = callable.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                exchanger.setV(t);
                return (Exchanger<T>) exchanger;
            }
            final DispatchPairExchanger<T> exchanger = (DispatchPairExchanger<T>) EXCHANGER_THREAD_LOCAL.get();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    T t = null;
                    try {
                        t = callable.call();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        if (ms < 0) {
                            exchanger.exchange0(t);
                        } else {
                            exchanger.exchange0(t, ms, TimeUnit.MILLISECONDS);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            return exchanger;
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new UnknownError("UnknownError exchange error ");
    }

    public <T> T call(Callable<T> callable) {
        try {
            return call(callable, -1L);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        throw new UnknownError("UnknownError exchange error ");
    }

    public <T> T call(Callable<T> callable, long timeout) throws TimeoutException {
        Exchanger<T> exchanger = exchange(callable);
        try {
            if (timeout < 0) {
                return exchanger.exchange((T) T_OBJECT);
            } else {
                return exchanger.exchange((T) T_OBJECT, timeout, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void postRunnableBlocking(final Runnable runnable) {
        call(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                runnable.run();
                return null;
            }
        });
    }


    public void postRunnableScissors(final Runnable runnable) {
        this.postRunnableScissors(runnable, -1L);
    }

    public void postRunnableScissors(final Runnable runnable, long timeout) {
        if (Looper.myLooper() == getLooper()) {
            runnable.run();
            return;
        }
        new BlockingRunnable(runnable).postAndWait(handler, timeout);
    }

    public void postRunnable(Runnable runnable) {
        postRunnable(runnable, 0);
    }

    public void postRunnable(Runnable runnable, long delay) {
        if (delay <= 0) {
            handler.post(runnable);
        } else {
            handler.postDelayed(runnable, delay);
        }
    }


    public void postRunnableImmediately(Runnable runnable) {
        if (Looper.myLooper() == getLooper()) {
            runnable.run();
            return;
        }
        postAtFont(runnable);
    }


    public void post(Runnable runnable) {
        if (Looper.myLooper() == getLooper()) {
            runnable.run();
            return;
        }
        postRunnable(runnable);
    }

    public void postAtFont(Runnable runnable) {
        handler.postAtFrontOfQueue(runnable);
    }

    public void cleanupQueue() {
        handler.removeCallbacksAndMessages(null);
    }

    public Handler getHandler() {
        return this.handler;
    }

    public Looper getLooper() {
        return mLooper;
    }

    public boolean addIdleHandler(MessageQueue.IdleHandler idleHandler) {
        MessageQueue messageQueue = getMessageQueue();
        if (messageQueue == null) {
            return false;
        }
        messageQueue.addIdleHandler(idleHandler);
        return true;
    }

    public boolean postRunnableInIdleRunning(final Runnable runnable) {
        MessageQueue messageQueue = getMessageQueue();
        if (messageQueue == null) {
            return false;
        }
        messageQueue.addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                runnable.run();
                return false;
            }
        });
        return true;
    }

    synchronized MessageQueue getMessageQueue() {
        if (null != this.mMessageQueue) {
            return this.mMessageQueue;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.mMessageQueue = mLooper.getQueue();
            return this.mMessageQueue;
        }
        Class clazz = mLooper.getClass();
        try {
            Field field = clazz.getDeclaredField("mQueue");
            field.setAccessible(true);
            Object mQueue = field.get(mLooper);
            if (mQueue instanceof MessageQueue) {
                this.mMessageQueue = (MessageQueue) mQueue;
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return this.mMessageQueue;
    }

    public boolean quit() {
        Looper looper = getLooper();
        if (looper != null) {
            looper.quit();
            return true;
        }
        return false;
    }

    @Override
    public void execute(Runnable command) {
        postRunnable(command);
    }
}
