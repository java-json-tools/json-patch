package com.github.fge.jsonpatch;

import tools.jackson.databind.JsonNode;

public interface Patch {

    JsonNode apply(JsonNode node) throws JsonPatchException;
}
