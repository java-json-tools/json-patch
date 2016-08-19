package com.github.fge.jsonpatch;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.amazonaws.services.dynamodbv2.xspec.ExpressionSpecBuilder;
import com.amazonaws.services.dynamodbv2.xspec.UpdateItemExpressionSpec;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;

public class JsonPatchToXSpecTest {
	@Test
	public void testEmpty() throws Exception {
		// setup
		String patchExpression = "[]";
		JsonNode jsonNode = JsonLoader.fromString(patchExpression);
		JsonPatch jsonPatch = JsonPatch.fromJson(jsonNode);
		// exercise
		ExpressionSpecBuilder actual = jsonPatch.get();
		// verify
		Assert.assertNotNull(actual);
		UpdateItemExpressionSpec actualSpec = actual.buildForUpdate();
		Assert.assertNull(actualSpec.getConditionExpression());
		Assert.assertEquals(actualSpec.getUpdateExpression(), "");
		Assert.assertNull(actualSpec.getNameMap());
		Assert.assertNull(actualSpec.getValueMap());
	}
	
	@Test
	public void test_replace_singlePath_number() throws Exception {
		// setup
		String patchExpression = "[ { \"op\": \"replace\", \"path\": \"/a\", \"value\": 1 } ]";
		JsonNode jsonNode = JsonLoader.fromString(patchExpression);
		JsonPatch jsonPatch = JsonPatch.fromJson(jsonNode);
		UpdateItemExpressionSpec expectedSpec = new ExpressionSpecBuilder()
			.addUpdate(ExpressionSpecBuilder.N("a").set(1))
			.withCondition(ExpressionSpecBuilder.attribute_exists("a"))
			.buildForUpdate();
		// exercise
		ExpressionSpecBuilder actual = jsonPatch.get();
		// verify
		Assert.assertNotNull(actual);
		UpdateItemExpressionSpec actualSpec = actual.buildForUpdate();
		//the spec builder agressively replaces path components with expression attribute
		//with sequentially increasing number strings (#0, #1 etc)
		//names in order to avoid name clashes with reserved words/symbols in documents
		//"a" was the only path element in the update expression and the only path element
		//in the conditions, so it gets the number zero in this example ("attribute_exists(#0)")
		Assert.assertEquals(actualSpec.getConditionExpression(), expectedSpec.getConditionExpression());
		Assert.assertEquals(actualSpec.getUpdateExpression(), expectedSpec.getUpdateExpression());
		Assert.assertEquals(actualSpec.getNameMap(), expectedSpec.getNameMap());
		Assert.assertEquals(actualSpec.getValueMap(), expectedSpec.getValueMap());
	}
	
	@Test
	public void test_replace_nestedPath_string() throws Exception {
		// setup
		String patchExpression = "[ { \"op\": \"replace\", \"path\": \"/a/b\", \"value\": \"foo\" } ]";
		JsonNode jsonNode = JsonLoader.fromString(patchExpression);
		JsonPatch jsonPatch = JsonPatch.fromJson(jsonNode);
		UpdateItemExpressionSpec expectedSpec = new ExpressionSpecBuilder()
			.addUpdate(ExpressionSpecBuilder.S("a.b").set("foo"))
			.withCondition(ExpressionSpecBuilder.attribute_exists("a.b"))
			.buildForUpdate();
		// exercise
		ExpressionSpecBuilder actual = jsonPatch.get();
		// verify
		Assert.assertNotNull(actual);
		UpdateItemExpressionSpec actualSpec = actual.buildForUpdate();
		Assert.assertEquals(actualSpec.getConditionExpression(), expectedSpec.getConditionExpression());
		Assert.assertEquals(actualSpec.getUpdateExpression(), expectedSpec.getUpdateExpression());
		Assert.assertEquals(actualSpec.getNameMap(), expectedSpec.getNameMap());
		Assert.assertEquals(actualSpec.getValueMap(), expectedSpec.getValueMap());
	}
	
	@Test
	public void test_remove_singlePath() throws Exception {
		// setup
		String patchExpression = "[ { \"op\": \"remove\", \"path\": \"/a\" } ]";
		JsonNode jsonNode = JsonLoader.fromString(patchExpression);
		JsonPatch jsonPatch = JsonPatch.fromJson(jsonNode);
		UpdateItemExpressionSpec expectedSpec = new ExpressionSpecBuilder()
			.addUpdate(ExpressionSpecBuilder.NULL("a").remove())
			.buildForUpdate();
		// exercise
		ExpressionSpecBuilder actual = jsonPatch.get();
		// verify
		Assert.assertNotNull(actual);
		UpdateItemExpressionSpec actualSpec = actual.buildForUpdate();
		Assert.assertNull(actualSpec.getConditionExpression());
		Assert.assertEquals(actualSpec.getUpdateExpression(), expectedSpec.getUpdateExpression());
		Assert.assertEquals(actualSpec.getNameMap(), expectedSpec.getNameMap());
		Assert.assertEquals(actualSpec.getValueMap(), expectedSpec.getValueMap());
	}
}
