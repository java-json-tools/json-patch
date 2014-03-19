package com.github.fge.jsonpatch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;

/**
 * JSON Patch {@code create} operation.
 *
 * <p>For this operation, {@code path} is the JSON Pointer where the value
 * should be added, and {@code value} is the value to add.</p>
 *
 * <p>This operation behaves like {@code add}, except for JSON Objects,
 * where it will raise an error if the target {@code path} points to an
 * existing value. This is designed to prevent clients from actually
 * overwriting values they don't think exist.</p>
 */
public final class CreateOperation
    extends AdditionOperation
{
    @JsonCreator
    public CreateOperation(@JsonProperty("path") final JsonPointer path,
        @JsonProperty("value") final JsonNode value)
    {
        super("create", path, value, false);
    }
}
