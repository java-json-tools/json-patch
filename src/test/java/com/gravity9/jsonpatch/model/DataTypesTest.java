package com.gravity9.jsonpatch.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gravity9.jsonpatch.JsonPatch;
import com.gravity9.jsonpatch.JsonPatchException;
import org.testng.annotations.Test;

public class DataTypesTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void should_pass_test_operation_for_string_data_type() throws JsonProcessingException, JsonPatchException {
        // given
        SimpleModel simpleModel = new SimpleModel("test", 10, 100L, 10.56f, 10.56, true, new EmbeddedModel("embeddedValue"));

        // when
        String operation = "[{\"op\":\"test\",\"path\":\"/stringField\",\"value\":\"test\"}]";
        JsonPatch jsonPatch = objectMapper.readValue(operation, JsonPatch.class);

        JsonNode jsonNode = objectMapper.valueToTree(simpleModel);

        jsonPatch.apply(jsonNode);
    }

    @Test
    void should_pass_test_operation_for_float_data_type() throws JsonProcessingException, JsonPatchException {
        // given
        SimpleModel simpleModel = new SimpleModel("test", 10, 100L, 10.56f, 10.56, true, new EmbeddedModel("embeddedValue"));

        // when
        String operation = "[{\"op\":\"test\",\"path\":\"/floatField\",\"value\": 10.56}]";
        JsonPatch jsonPatch = objectMapper.readValue(operation, JsonPatch.class);

        JsonNode jsonNode = objectMapper.valueToTree(simpleModel);

        jsonPatch.apply(jsonNode);
    }

    @Test
    void should_pass_test_operation_for_int_data_type() throws JsonProcessingException, JsonPatchException {
        // given
        SimpleModel simpleModel = new SimpleModel("test", 10, 100L, 10.56f, 10.56, true, new EmbeddedModel("embeddedValue"));

        // when
        String operation = "[{\"op\":\"test\",\"path\":\"/intField\",\"value\": 10}]";
        JsonPatch jsonPatch = objectMapper.readValue(operation, JsonPatch.class);

        JsonNode jsonNode = objectMapper.valueToTree(simpleModel);

        jsonPatch.apply(jsonNode);
    }

    @Test
    void should_pass_test_operation_for_long_data_type() throws JsonProcessingException, JsonPatchException {
        // given
        SimpleModel simpleModel = new SimpleModel("test", 10, 100L, 10.56f, 10.56, true, new EmbeddedModel("embeddedValue"));

        // when
        String operation = "[{\"op\":\"test\",\"path\":\"/longField\",\"value\": 100}]";
        JsonPatch jsonPatch = objectMapper.readValue(operation, JsonPatch.class);

        JsonNode jsonNode = objectMapper.valueToTree(simpleModel);

        jsonPatch.apply(jsonNode);
    }

    @Test
    void should_pass_test_operation_for_double_data_type() throws JsonProcessingException, JsonPatchException {
        // given
        SimpleModel simpleModel = new SimpleModel("test", 10, 100L, 10.56f, 10.56, true, new EmbeddedModel("embeddedValue"));

        // when
        String operation = "[{\"op\":\"test\",\"path\":\"/doubleField\",\"value\": 10.56}]";
        JsonPatch jsonPatch = objectMapper.readValue(operation, JsonPatch.class);

        JsonNode jsonNode = objectMapper.valueToTree(simpleModel);

        jsonPatch.apply(jsonNode);
    }

    @Test
    void should_pass_test_operation_for_boolean_data_type() throws JsonProcessingException, JsonPatchException {
        // given
        SimpleModel simpleModel = new SimpleModel("test", 10, 100L, 10.56f, 10.56, true, new EmbeddedModel("embeddedValue"));

        // when
        String operation = "[{\"op\":\"test\",\"path\":\"/booleanField\",\"value\": true}]";
        JsonPatch jsonPatch = objectMapper.readValue(operation, JsonPatch.class);

        JsonNode jsonNode = objectMapper.valueToTree(simpleModel);

        jsonPatch.apply(jsonNode);
    }

    @Test
    void should_pass_test_operation_for_embedded_data_type() throws JsonProcessingException, JsonPatchException {
        // given
        SimpleModel simpleModel = new SimpleModel("test", 10, 100L, 10.56f, 10.56, true, new EmbeddedModel("embeddedValue"));

        // when
        String operation = "[{\"op\":\"test\",\"path\":\"/embeddedField\",\"value\": {\"embeddedField\": \"embeddedValue\"}}]";
        JsonPatch jsonPatch = objectMapper.readValue(operation, JsonPatch.class);

        JsonNode jsonNode = objectMapper.valueToTree(simpleModel);

        jsonPatch.apply(jsonNode);
    }
}
