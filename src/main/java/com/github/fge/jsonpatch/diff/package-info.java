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
 * JSON "diff" implementation
 *
 * <p>The main class, {@link com.github.fge.jsonpatch.diff.JsonDiff}, does the
 * reverse of what JSON Patch does: given two JSON values, it generates a patch
 * (as JSON) to apply to the first node in order to obtain the second node.</p>
 *
 * <p>This implementation is able to factorize additions and removals into
 * moves and copies.</p>
 */
package com.github.fge.jsonpatch.diff;
