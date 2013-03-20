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

import com.github.fge.jackson.jsonpointer.JsonPointer;

/**
 * Base class for JSON Patch operations taking two JSON Pointers as arguments
 */
public abstract class DualPathOperation
    extends JsonPatchOperation
{
    protected final JsonPointer from;

    protected DualPathOperation(final JsonPointer from,
        final JsonPointer path)
    {
        super(path);
        this.from = from;
    }

    @Override
    public String toString()
    {
        return "from = \"" + from + "', " + super.toString();
    }
}
