package com.github.fge.jsonpatch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;

/**
 * JSON Patch {@code create} operation.
 *
 * <p>For this operation, {@code path} is the JSON Pointer where the value
 * should be added, and {@code value} is the value to add.</p>
 *
 * <p>This is the same as the {@code add} operation with one exception. If the {@code path}
 * points to an existing non-array-element, the operation will fail. This is
 * designed to prevent clients from accidentally overwriting values they don't
 * think exist.</p>
 */
public final class CreateOperation
    extends AddableValueOperation
{
    @JsonCreator
    public CreateOperation(@JsonProperty("path") final JsonPointer path,
        @JsonProperty("value") final JsonNode value)
    {
        super("create", path, value);
    }

    /**
     * Checks if any value exists at the path before adding to the object.
     */
    @Override
    protected JsonNode addToObject(final JsonPointer path, final JsonNode node)
        throws JsonPatchException
    {
        final JsonNode existingNode = path.path(node);
        if (existingNode != MissingNode.getInstance())
            throw new JsonPatchException(BUNDLE.getMessage(
                "jsonPatch.valueAtPathAlreadyExists"));

        return addToObjectWithOverwrite(path, node);
    }
}
