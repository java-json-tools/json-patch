package com.github.fge.jsonpatch.query;

import com.github.fge.jsonpatch.JsonPatchOperationTest;

import java.io.IOException;

public class RemoveQueryOperationTest extends JsonPatchOperationTest {

    public RemoveQueryOperationTest() throws IOException {
        super("query/remove");
    }
}
