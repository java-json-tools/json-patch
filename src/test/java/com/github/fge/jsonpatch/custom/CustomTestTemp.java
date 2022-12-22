
package com.github.fge.jsonpatch.custom;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;

import javax.json.Json;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Test
public class CustomTestTemp {
//
//    @Test
//    public void addOperation() {
//        JsonNode source = null;
//        try {
//            source = new ObjectMapper().readTree(new File("src/test/resources/custom/base/test/old_add.json"));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        JsonNode target = null;
//        try {
//            target = new ObjectMapper().readTree(new File("src/test/resources/custom/base/test/new_add.json"));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        //get json patch using #asJsonPatch
//        final JsonPatch jsonPatch = JsonDiff.asJsonPatch(source, target);
//
//        final JsonPatch jsonpatch = JsonDiff.asJsonPatch(source, target);
//        try {
//            target = new ObjectMapper().readTree(new File("src/test/resources/custom/base/test/old_add.json"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.out.println(jsonPatch);
//        try {
//            jsonPatch.operations.get(0).setPath(new JsonPointerCustom("/Entitlements/-"));
//        } catch (JsonPointerException e) {
//            throw new RuntimeException(e);
//        }
//        //apply patch to source using #apply
//        // source + patch = target
//        final JsonNode patched;
//        try {
//            patched = jsonPatch.apply(source, true);
//        } catch (JsonPatchException e) {
//            throw new RuntimeException(e);
//        }
//        System.out.println(patched.toPrettyString());
//    }
//
//    @Test
//    public void removeOperation() {
//        JsonNode source = null;
//        try {
//            source = new ObjectMapper().readTree(new File("src/test/resources/custom/base/test/old_remove.json"));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        JsonNode target = null;
//        try {
//            target = new ObjectMapper().readTree(new File("src/test/resources/custom/base/test/new_remove.json"));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        //get json patch using #asJsonPatch
//        final JsonPatch jsonPatch = JsonDiff.asJsonPatch(source, target);
//        System.out.println(jsonPatch);
//
//        try {
//            jsonPatch.operations.get(0).setPath(new JsonPointerCustom("/Entitlements/?"));
//        } catch (JsonPointerException e) {
//            throw new RuntimeException(e);
//        }
//
//        //apply patch to source using #apply
//        // source + patch = target
//        final JsonNode patched;
//        try {
//            patched = jsonPatch.apply(source, true);
//        } catch (JsonPatchException e) {
//            throw new RuntimeException(e);
//        }
//        System.out.println(patched.toPrettyString());
//    }
//
//
//    @Test
//    public void replaceOperation() {
//        JsonNode source = null;
//        try {
//            source = new ObjectMapper().readTree(new File("src/test/resources/custom/base/test/old_replace.json"));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        JsonNode target = null;
//        try {
//            target = new ObjectMapper().readTree(new File("src/test/resources/custom/base/test/new_replace.json"));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        //get json patch using #asJsonPatch
//        final JsonPatch jsonPatch = JsonDiff.asJsonPatch(source, target);
//        System.out.println(jsonPatch);
//        //apply patch to source using #apply
//        // source + patch = target
//
//        try {
//            jsonPatch.operations.get(0).setPath(new JsonPointerCustom("/Entitlements/?/Additional Info"));
//        } catch (JsonPointerException e) {
//            throw new RuntimeException(e);
//        }
//        try {
//            jsonPatch.operations.get(1).setPath(new JsonPointerCustom("/Entitlements/?/Entitlement Key"));
//        } catch (JsonPointerException e) {
//            throw new RuntimeException(e);
//        }
//
//    }
//
//
//    @Test
//    public void normalTest() {
//        JsonNode source = null;
//        try {
//            source = new ObjectMapper().readTree(new File("src/test/resources/jsonpatch/custom/separate/old_replace.json"));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        JsonNode target = null;
//        try {
//            target = new ObjectMapper().readTree(new File("src/test/resources/jsonpatch/custom/separate/new_replace.json"));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        //get json patch using #asJsonPatch
//        final JsonPatch jsonPatch = JsonDiff.asJsonPatch(source, target);
//        System.out.println(jsonPatch);
//        //apply patch to source using #apply
//        // source + patch = target
//
//        final JsonNode patched;
//        try {
//            patched = jsonPatch.apply(source, true);
//        } catch (JsonPatchException e) {
//            throw new RuntimeException(e);
//        }
//        System.out.println(patched.toPrettyString());
//    }


    @Test
    public void testPractice() {

        //map1
        JsonNode source;
        ObjectMapper mapper = new ObjectMapper();
        try {
            source = new ObjectMapper().readTree(new File("/Users/confluxsys/IdeaProjects/json-patch/src/test/resources/jsonpatch/custom.base/old.json"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Map<String, Object> source_result = mapper.convertValue(source, new TypeReference<Map<String, Object>>() {
        });
        for (String key : source_result.keySet()) {
            System.out.println(key + " : " + source_result.get(key));
        }
        System.out.println(source_result);

        //map2
        JsonNode value_locator = null;
        try {
            value_locator = new ObjectMapper().readTree("{\n" +
                    "  \"Application Key\": \"100\",\n" +
                    "  \"Entitlement Type\": \"UD_GROUPS_GROUPS\",\n" +
                    "  \"Entitlement Name\": \"565~Deployment Owners\"\n" +
                    "}");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Map<String, Object> value_locator_map = mapper.convertValue(value_locator, new TypeReference<Map<String, Object>>() {
        });
        System.out.println(value_locator_map);
    }
}
