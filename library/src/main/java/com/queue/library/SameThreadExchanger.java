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

/**
 * @author cenxiaozhong
 * @date 2019/2/15
 * @since 1.0.0
 */
public class SameThreadExchanger<V> extends Exchanger<V> {

	private V v;

	public SameThreadExchanger() {
	}

	void setV(V v) {
		this.v = v;
	}

	@Override
	public V exchange(V x, long timeout, TimeUnit unit) {
		return exchange(v);
	}

	@Override
	public V exchange(V x) {
		try {
			V v = this.v;
			return v;
		} finally {
			this.v = null;
		}
	}
}
