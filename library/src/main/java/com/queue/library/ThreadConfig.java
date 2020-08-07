package com.queue.library;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author xiaozhongcen
 * @date 20-8-7
 * @since 1.0.0
 */
public final class ThreadConfig {

    private final static AtomicInteger THREAD_ID_GENERATOR = new AtomicInteger(1);

    public static int getUniqueThreadId() {
        return THREAD_ID_GENERATOR.getAndIncrement();
    }

}
