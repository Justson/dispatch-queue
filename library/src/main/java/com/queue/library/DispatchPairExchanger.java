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

import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author cenxiaozhong
 * @date 2019/2/15
 * @since 1.0.0
 */
public class DispatchPairExchanger<V> extends Exchanger<V> {
	private final long mThreadId;
	private final String mThreadName;

	public DispatchPairExchanger() {
		mThreadId = Thread.currentThread().getId();
		mThreadName = Thread.currentThread().getName();
	}

	V exchange0(V x) throws InterruptedException {
		return super.exchange(x);
	}

	V exchange0(V x, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		return super.exchange(x, timeout, unit);
	}

	@Override
	public V exchange(V x) throws InterruptedException {
		long id = Thread.currentThread().getId();
		if (id != mThreadId) {
			throw new RuntimeException("you must call exchange in the thread id:" + id + " thread name:" + mThreadName);
		}
		return super.exchange(x);
	}

	@Override
	public V exchange(V x, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		long id = Thread.currentThread().getId();
		if (id != mThreadId) {
			throw new RuntimeException("you must call exchange in the thread id:" + id + " thread name:" + mThreadName);
		}
		return super.exchange(x, timeout, unit);
	}
}
