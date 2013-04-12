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
