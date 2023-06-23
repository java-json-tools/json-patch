package com.gravity9.jsonpatch;

import com.fasterxml.jackson.databind.JsonNode;

public interface Patch {

	JsonNode apply(JsonNode node) throws JsonPatchException;
}
