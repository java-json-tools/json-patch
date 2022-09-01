package com.github.fge.jsonpatch;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static com.github.fge.jsonpatch.JsonPatchOperation.BUNDLE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class ValidationTest {
    // the test cases have comments in them to explain the specific test case
    // hence use a custom Object mapper that allows Json comments
    private static final ObjectMapper MAPPER = new ObjectMapper()
                                                   .enable(JsonParser.Feature.ALLOW_COMMENTS);

    @DataProvider
    public final Iterator<Object[]> getInvalidJsonPatches() throws IOException
    {
        final JsonNode patches = MAPPER.readTree(
            new InputStreamReader(
                Objects.requireNonNull(ValidationTest.class.getResourceAsStream(
                    "/jsonpatch/invalid-patches.json"
                )),
                StandardCharsets.UTF_8
            )
        );

        final List<Object[]> list = Lists.newArrayList();

        for (final JsonNode patch: patches) {
            list.add(new Object[] {patch});
        }

        return list.iterator();
    }

    @Test(dataProvider = "getInvalidJsonPatches")
    public final void invalidPatchesAreDetected(final JsonNode patch) throws IOException
    {
        try {
            JsonPatch.fromJson(patch);
            fail("No exception thrown!!");
        } catch (IllegalArgumentException ie) {
            assertEquals(ie.getMessage(), BUNDLE.format("jsonPatch.invalidPatch", patch));
        }
    }
}
