/*
 * Copyright (c) 2016, Alexander Patrikalakis (amcp@me.com)
 * Copyright (c) 2015, Daisuke Miyamoto (dai.0304@gmail.com)
 *
 * This software is dual-licensed under:
 *
 * - the Lesser General Public License (LGPL) version 3.0 or, at your option, any
 *   later version;
 * - the Apache Software License (ASL) version 2.0.
 *
 * The text of this file and of both licenses is available at the root of this
 * project or, if you have the jar distribution, in directory META-INF/, under
 * the names LGPL-3.0.txt and ASL-2.0.txt respectively.
 *
 * Direct link to the sources:
 *
 * - LGPL 3.0: https://www.gnu.org/licenses/lgpl-3.0.txt
 * - ASL 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package com.github.fge.jsonpatch;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.TokenResolver;
import com.google.common.base.Function;
import com.google.common.base.Joiner;

public class JsonPathToAttributePath implements Function<JsonPointer, String> {
	
	private static Pattern ARRAY_PATTERN = Pattern.compile("(0|[1-9][0-9]+)");
	
	
	@Override
	public String apply(JsonPointer pointer) {
		List<String> elements = new ArrayList<String>();
		for (TokenResolver<JsonNode> tokenResolver : pointer) {
			String token = tokenResolver.getToken().getRaw();
			if (ARRAY_PATTERN.matcher(token).matches()) {
				String last = elements.get(elements.size() - 1);
				elements.set(elements.size() - 1, String.format("%s[%s]", last, token));
			} else {
				elements.add(token);
			}
		}
		
		return Joiner.on(".").join(elements);
	}
}
