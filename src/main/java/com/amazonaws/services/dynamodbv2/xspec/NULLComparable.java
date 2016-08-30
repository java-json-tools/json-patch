/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.amazonaws.services.dynamodbv2.xspec;

import com.amazonaws.services.dynamodbv2.xspec.NULL;

/**
 * TODO for daisuke
 *
 * @since 0.13
 * @version $Id$
 * @author Alexander Patrikalakis
 */
public class NULLComparable extends NULL {
	public NULLComparable(String path) {
		super(path);
	}
	public ComparatorCondition eq(Operand that) {
		return new ComparatorCondition("=", this, that);
	}
	public static final LiteralOperand generateNull() {
		return new LiteralOperand((Object) null);
	}
}
