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
 * Implementation of JSON Patch
 *
 * <p>As its name implies, JSON Patch is a mechanism designed to modify JSON
 * documents. It consists of a series of operations to apply in order to the
 * source JSON document until all operations are applied or an error has been
 * encountered.</p>
 *
 * <p>The main class is {@link com.github.fge.jsonpatch.JsonPatch}.</p>
 *
 * <p>Note that at this moment, the only way to build a patch is from a JSON
 * representation (as a {@link com.fasterxml.jackson.databind.JsonNode}).</p>
 *
 */
package com.github.fge.jsonpatch;
