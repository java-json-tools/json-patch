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
 * Implementation of JSON Patch
 *
 * <p>As its name implies, JSON Patch is a mechanism designed to modify JSON
 * documents. It consists of a series of operations to apply in order to the
 * source JSON document until all operations are applied or an error has been
 * encountered.</p>
 *
 * <p>The main class is {@link com.github.fge.jsonpatch.JsonPatch}.</p>
 */
package com.github.fge.jsonpatch;
