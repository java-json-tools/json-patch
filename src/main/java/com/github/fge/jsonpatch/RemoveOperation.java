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


//        this.type = type;
//        this.from = from;
//        this.oldValue = oldValue;
//        this.path = path;
//        this.value = value;

package com.github.fge.jsonpatch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.jsonpointer.JsonPointerCustom;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;

import java.io.IOException;

/**
 * JSON Path {@code remove} operation
 *
 * <p>This operation only takes one pointer ({@code path}) as an argument. It
 * is an error condition if no JSON value exists at that pointer.</p>
 */
public final class RemoveOperation
        extends JsonPatchOperation {

    Logger logger = LoggerFactory.getLogger(RemoveOperation.class);


    @JsonCreator
    public RemoveOperation(@JsonProperty("path") final JsonPointerCustom path, @JsonProperty("value_locator") final JsonNode value_locator) {
        super("remove", path, value_locator);
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
    public JsonNode apply(final JsonNode node)
            throws JsonPatchException {
        if (path.isEmpty())
            return MissingNode.getInstance();
        if (path.path(node).isMissingNode())
            throw new JsonPatchException(BUNDLE.getMessage(
                    "jsonPatch.noSuchPath"));
        final JsonNode ret = node.deepCopy();
        final JsonNode parentNode = path.parent().get(ret);
        final String raw = Iterables.getLast(path).getToken().getRaw();
        if (parentNode.isObject())
            ((ObjectNode) parentNode).remove(raw);
        else
            ((ArrayNode) parentNode).remove(Integer.parseInt(raw));
        return ret;
    }


    @Override
    public JsonNode apply(JsonNode node, boolean flag) throws JsonPatchException {

        JsonNode result = null;

        //see if path is null and check if its has ?
        if (path != null && path.toString().contains("?")) {

            //get the value locator
            JsonNode valueLocatorNode = value_locator.deepCopy();

            JsonPointerCustom array_node_path = null;
            try {
                //get the path before ? i.e.>> /Entitlements
                array_node_path = JsonPointerCustom.getBeforeUnknown(path.toString());
            } catch (JsonPointerException e) {
                logger.error("JsonPointerException: " + e.getMessage() + " " + "Please provide valid path string or tokens");
            }
            if (array_node_path != null) {
                //get the raw representation of the field i.e >> Entitlements
                final String raw = Iterables.getLast(array_node_path).getToken().getRaw();
                //get the array node Entitlements
                ArrayNode array = (ArrayNode) node.get(raw);

                //if array is null and flag is true then throw exception
                if (array == null && flag)
                    throw new JsonPatchException(BUNDLE.getMessage(
                            "jsonPatch.noSuchPath"));
                    //els log the exception
                else if (array == null && !flag) {
                    logger.error("jsonPatch.noSuchPath");
                }

                //taking indexes of nodes that we want to remove using valueLocatorNode
                int indOg = getNodeToUpdate(valueLocatorNode, array);

                // if its not present then throw exception
                if (indOg == -1 && flag)
                    throw new JsonPatchException(BUNDLE.getMessage(
                            "jsonPatch.noSuchPath"));
                    //else log the exception
                else if (indOg == -1 && flag == false) {
                    logger.error("jsonPatch.noSuchPath");
                }

                //remove the node
                array.remove(indOg);
                result = node;
            }

        } else {
            // if path is null  or if path is missing
            if (path == null || (path.path(node).isMissingNode() && flag))
                //throw exception
                throw new JsonPatchException(BUNDLE.getMessage(
                        "jsonPatch.noSuchPath"));
            result = apply(node);
        }
        return result;
    }

    @Override
    public void serialize(final JsonGenerator jgen,
                          final SerializerProvider provider)
            throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        jgen.writeStringField("op", "remove");
        jgen.writeStringField("path", path.toString());
        jgen.writeEndObject();
    }

    @Override
    public void serializeWithType(final JsonGenerator jgen,
                                  final SerializerProvider provider, final TypeSerializer typeSer)
            throws IOException, JsonProcessingException {
        serialize(jgen, provider);
    }

    @Override
    public String toString() {
        return "op: " + op + "; path: \"" + path + '"';
    }
}
