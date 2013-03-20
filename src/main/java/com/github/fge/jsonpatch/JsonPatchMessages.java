/*
 * Copyright (c) 2013, Francis Galiegue <fgaliegue@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.fge.jsonpatch;

public final class JsonPatchMessages
{
    public static final String NULL_INPUT = "input cannot be null";
    public static final String NOT_JSON_PATCH
        = "input is not a valid JSON Patch";
    public static final String NO_SUCH_PARENT
        = "parent of node to add does not exist";
    public static final String NOT_AN_INDEX
        = "reference token is not an array index";
    public static final String NO_SUCH_INDEX
        = "no such index in target array";
    public static final String NO_SUCH_PATH
        = "no such path in target JSON document";
    public static final String VALUE_TEST_FAILURE
        = "value differs from expectations";

    private JsonPatchMessages()
    {
    }
}
