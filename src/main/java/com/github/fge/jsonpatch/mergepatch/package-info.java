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

/**
 * JSON Merge Patch implementation
 *
 * <p>This is a complete implementation of <a
 * href="http://tools.ietf.org/html/rfc7386">RFC 7386 (JSON Merge Patch)</a>.
 * </p>
 *
 * <p>You may want to use this instead of a "pure" (aka RFC 6902) JSON Patch if
 * you want to do simple patching of JSON Objects, where this implementation
 * really shines. For instance, if you want to replace a value for one
 * property {@code p} with JSON String {@code "bar"}, a JSON Patch will read:
 * </p>
 *
 * <pre>
 *     [
 *         { "op": "replace", "path": "/p", "value": "bar" }
 *     ]
 * </pre>
 *
 * <p>whereas the equivalent JSON Merge Patch will be:</p>
 *
 * <pre>
 *     { "p": "bar"}
 * </pre>
 *
 * <p>Note that this is recursive; therefore, this:</p>
 *
 * <pre>
 *     { "a": { "b": "c" } }
 * </pre>
 *
 * <p>will replace (or add, if not present) the value at JSON Pointer {@code
 * /a/b} with JSON String {@code "c"}.</p>
 *
 * <p><b>HOWEVER:</b> while this seems attractive, there are a few traps. One
 * of them is that, when a value of an object member in a JSON Merge Patch is
 * a JSON null, the target will see the equivalent member <b>removed</b>; there
 * is no way to specify that a value should be set with a JSON null value (JSON
 * Patch allows for this).</p>
 *
 * <p>The RFC defines a pseudo code for how a JSON Merge Patch is applied; this
 * function is replicated in the javadoc for {@link
 * com.github.fge.jsonpatch.mergepatch.JsonMergePatch}, so you are encouraged to
 * read the javadoc for this class, and the RFC itself.</p>
 */
package com.github.fge.jsonpatch.mergepatch;