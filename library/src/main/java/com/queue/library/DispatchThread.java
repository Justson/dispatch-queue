package com.queue.library;

import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;

/**
 * @author cenxiaozhong
 * @date 2020/7/11
 * @since 1.0.0
 */
public final class DispatchThread {

    public static Dispatch create(String name) {
        return create(name, Process.THREAD_PRIORITY_DEFAULT);
    }

    public static Dispatch create(String name, int priority) {
        HandlerThread handlerThread = new HandlerThread(name, priority);
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        return new Dispatch(looper);
    }
}
