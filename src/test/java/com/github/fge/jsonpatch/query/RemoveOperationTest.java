package com.github.fge.jsonpatch.query;

import com.github.fge.jsonpatch.JsonPatchOperationTest;
import org.testng.annotations.Ignore;

import java.io.IOException;

@Ignore
public class RemoveOperationTest extends JsonPatchOperationTest {

    public RemoveOperationTest() throws IOException {
        super("query/remove");
    }
}
