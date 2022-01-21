package com.github.fge.jsonpatch.query;

import com.github.fge.jsonpatch.JsonPatchOperationTest;
import org.testng.annotations.Ignore;

import java.io.IOException;

@Ignore
public class ReplaceOperationTest extends JsonPatchOperationTest {

    public ReplaceOperationTest() throws IOException {
        super("query/replace");
    }
}
