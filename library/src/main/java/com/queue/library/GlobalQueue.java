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

import android.os.Looper;

/**
 * @author cenxiaozhong
 * @date 2020/7/5
 * @since 1.0.0
 */
public final class GlobalQueue {
    private static volatile Dispatch mMainQueue = null;

    public static Dispatch getMainQueue() {
        if (mMainQueue == null) {
            synchronized (GlobalQueue.class) {
                if (mMainQueue == null) {
                    mMainQueue = new Dispatch(Looper.getMainLooper());
                }
            }
        }
        return mMainQueue;
    }
}
