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

package com.github.fge.jsonpatch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.jsonpointer.JsonPointerCustom;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;

import java.util.Comparator;


/**
 * JSON Patch {@code replace} operation
 *
 * <p>For this operation, {@code path} points to the value to replace, and
 * {@code value} is the replacement value.</p>
 *
 * <p>It is an error condition if {@code path} does not point to an actual JSON
 * value.</p>
 */
public final class ReplaceOperation
        extends PathValueOperation {

    Logger logger = LoggerFactory.getLogger(ReplaceOperation.class);

    @JsonCreator
    public ReplaceOperation(@JsonProperty("path") final JsonPointerCustom path,
                            @JsonProperty("value") final JsonNode value,
                            @JsonProperty("value_locator") final JsonNode value_locator) {
        super("replace", path, value, value_locator);
    }

    private void removeOriginal(ArrayNode array, JsonNode originalNode) throws JsonPatchException {
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).equals(originalNode)) {
                array.remove(i);
                break;
            }
        }
    }

    @Override
    public JsonNode apply(final JsonNode node)
            throws JsonPatchException {

        /*
         * FIXME cannot quite be replaced by a remove + add because of arrays.
         * For instance:
         *
         * { "op": "replace", "path": "/0", "value": 1 }
         *
         * with
         *
         * [ "x" ]
         *
         * If remove is done first, the array is empty and add rightly complains
         * that there is no such index in the array.
         */
        final JsonNode replacement = value.deepCopy();
        if (path.isEmpty())
            return replacement;
        final JsonNode ret = node.deepCopy();
        final JsonNode parent = path.parent().get(ret);
        final String rawToken = Iterables.getLast(path).getToken().getRaw();
        if (parent.isObject())
            ((ObjectNode) parent).replace(rawToken, replacement);
        else
            ((ArrayNode) parent).set(Integer.parseInt(rawToken), replacement);
        return ret;
    }


    private int getNodeToUpdate(JsonNode valueLocatorNode, ArrayNode array) {
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).get("Application Key").equals(valueLocatorNode.get("Application Key")) &&
                    array.get(i).get("Entitlement Type").equals(valueLocatorNode.get("Entitlement Type")) &&
                    array.get(i).get("Entitlement Name").equals(valueLocatorNode.get("Entitlement Name"))) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public JsonNode apply(JsonNode node, boolean flag) throws JsonPatchException {

        JsonNode result = null;

        if (path.toString().contains("?")) {

            JsonNode valueLocatorNode = value_locator.deepCopy();
            ArrayNode array = (ArrayNode) node.get("Entitlements");

            //taking indexes of nodes that we want to update using valueLocatorNode
            int indOg = getNodeToUpdate(valueLocatorNode, array);
            //taking indexes of nodes that we want to update using valueLocatorNode
            int indToUpdate = getNodeToUpdate(valueLocatorNode, array);

            //get the pointer of the node where we want to insert value of the value field
            JsonPointerCustom toUpdateNodePointer = null;
            try {
                //taking pointer by using getAfterUnknown function which will return /Entitlements/?/Entitlement Key >> Entitlement Key
                toUpdateNodePointer = JsonPointerCustom.getAfterUnknown(path.toString());
            } catch (JsonPointerException e) {
                logger.error("JsonPointerException: " + e.getMessage());
            }

            //get raw token to update
            String rawToken = null;
            if (toUpdateNodePointer != null) {
                rawToken = Iterables.getLast(toUpdateNodePointer).getToken().getRaw();
            }

            //get original node and toUpdate node from the entitlements array using the index given by getNodeToUpdate
            JsonNode ogNode = array.get(indOg);
            JsonNode updatedNode = array.get(indToUpdate);

            if (updatedNode.get(rawToken) == null && flag)
                throw new JsonPatchException(BUNDLE.getMessage(
                        "jsonPatch.noSuchPath"));
            else if (updatedNode.get(rawToken) == null && !flag)
                logger.error("jsonPatch.noSuchPath");


            //update the node >> updatedNode
            if (updatedNode.isObject() && rawToken != null) {
                ((ObjectNode) updatedNode).replace(rawToken, value);
            } else if (rawToken != null) {
                ((ArrayNode) updatedNode).set(Integer.parseInt(rawToken), value);
            }
            //update the updated node in the Entitlements
            ((ArrayNode) node.get("Entitlements")).add(updatedNode);
            removeOriginal(array, ogNode);
            result = node;
        } else {

            if (path.path(node).isMissingNode() && flag)
                throw new JsonPatchException(BUNDLE.getMessage(
                        "jsonPatch.noSuchPath"));
            else if (path.path(node).isMissingNode() && !flag)
                logger.error("jsonPatch.noSuchPath");

            result = apply(node);
        }
        return result;
    }

}
