/*
 * Copyright (c) 2014, Francis Galiegue (fgaliegue@gmail.com)
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

package com.gravity9.jsonpatch;

import static com.gravity9.jsonpatch.JsonPatchOperation.BUNDLE;

public final class JsonPatchException extends Exception {

	public JsonPatchException(final String message) {
		super(message);
	}

	public JsonPatchException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public static JsonPatchException valueTestFailure(Object expected, Object found) {
		return new JsonPatchException(BUNDLE.getMessage("jsonPatch.valueTestFailure") +
			": expected '" + expected + "' but found '" + found + "'");
	}

	public static JsonPatchException notAnIndex(String index) {
		return new JsonPatchException(BUNDLE.getMessage("jsonPatch.notAnIndex") + ": " + index);
	}

	public static JsonPatchException noSuchIndex(Integer index) {
		return new JsonPatchException(BUNDLE.getMessage("jsonPatch.noSuchIndex") + ": " + index);
	}

	public static JsonPatchException parentNotContainer(String path) {
		return new JsonPatchException(BUNDLE.getMessage("jsonPatch.parentNotContainer") + ": " + path);
	}
}
