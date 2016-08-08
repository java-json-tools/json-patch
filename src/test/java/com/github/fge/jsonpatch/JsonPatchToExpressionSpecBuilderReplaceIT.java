/*
 * Copyright (c) 2016, Alexander Patrikalakis (amcp@me.com)
 * Copyright (c) 2015, Daisuke Miyamoto (dai.0304@gmail.com)
 *
 * This software is dual-licensed under:
 *
 * - the Lesser General Public License (LGPL) version 3.0 or, at your option, any
 *   later version;
 * - the Apache Software License (ASL) version 2.0.
 *
 * The text of this file and of both licenses is available at the root of this
 * project or, if you have the jar distribution, in directory META-INF/, under
 * the names LGPL-3.0.txt and ASL-2.0.txt respectively.
 *
 * Direct link to the sources:
 *
 * - LGPL 3.0: https://www.gnu.org/licenses/lgpl-3.0.txt
 * - ASL 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
 */
package com.github.fge.jsonpatch;

import java.math.BigDecimal;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.xspec.ExpressionSpecBuilder;
import com.amazonaws.services.dynamodbv2.xspec.UpdateItemExpressionSpec;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class JsonPatchToExpressionSpecBuilderReplaceIT {
	
	private static final String KEY_ATTRIBUTE_NAME = "key";
	
	private static final String VALUE = "keyValue";
	
	private static final PrimaryKey PK = new PrimaryKey(KEY_ATTRIBUTE_NAME, VALUE);
	
	private static final String TABLE_NAME = "json_patch_test";
	
	private Table table;
	
	
	@BeforeTest
	public void setUp() throws Exception {
		AmazonDynamoDB amazonDynamoDB = DynamoDBEmbedded.create().amazonDynamoDB();
		amazonDynamoDB.createTable(new CreateTableRequest()
				.withTableName(TABLE_NAME)
				.withProvisionedThroughput(new ProvisionedThroughput(1L, 1L))
				.withAttributeDefinitions(new AttributeDefinition()
						.withAttributeName(KEY_ATTRIBUTE_NAME)
						.withAttributeType(ScalarAttributeType.S))
				.withKeySchema(new KeySchemaElement()
						.withAttributeName(KEY_ATTRIBUTE_NAME)
						.withKeyType(KeyType.HASH)));
		table = new Table(amazonDynamoDB, TABLE_NAME);
	}
	
	/**
	 * try to update an item that doesnt exist. will create new item
	 */
	@Test
	public void test_replace_singlePath_number() throws Exception {
		// setup
		String patchExpression = "[ { \"op\": \"replace\", \"path\": \"/a\", \"value\": 1 } ]";
		JsonNode jsonNode = JsonLoader.fromString(patchExpression);
		JsonPatch jsonPatch = JsonPatch.fromJson(jsonNode);
		// exercise
		ExpressionSpecBuilder builder = jsonPatch.get();
		UpdateItemExpressionSpec spec = builder.buildForUpdate();
		table.updateItem(KEY_ATTRIBUTE_NAME, VALUE, spec);
		// verify
		Item item = table.getItem(PK);
		Assert.assertTrue(item.hasAttribute("key"));
		Assert.assertEquals(item.getString("key"), "keyValue");
		Assert.assertTrue(item.hasAttribute("a"));
		Assert.assertEquals(item.getNumber("a").longValue(), 1L);
	}
	
	@Test
	public void test_replace_nestedPath_string() throws Exception {
		// setup
		table.putItem(Item.fromMap(ImmutableMap.<String, Object> builder()
			.put(KEY_ATTRIBUTE_NAME, VALUE)
			.put("a", ImmutableMap.of("a", 1L))
			.build()));
		
		String patchExpression = "[ { \"op\": \"replace\", \"path\": \"/a/b\", \"value\": \"foo\" } ]";
		JsonNode jsonNode = JsonLoader.fromString(patchExpression);
		JsonPatch jsonPatch = JsonPatch.fromJson(jsonNode);
		// exercise
		ExpressionSpecBuilder builder = jsonPatch.get();
		UpdateItemExpressionSpec spec = builder.buildForUpdate();
		table.updateItem(KEY_ATTRIBUTE_NAME, VALUE, spec);
		// verify
		Item item = table.getItem(PK);
		Assert.assertTrue(item.hasAttribute("key"));
		Assert.assertEquals(item.getString("key"), "keyValue");
		Assert.assertTrue(item.hasAttribute("a"));
		Assert.assertTrue(item.getRawMap("a").containsKey("a"));
		Assert.assertEquals(((BigDecimal) item.getMap("a").get("a")).longValue(), 1L);
		Assert.assertTrue(item.getMap("a").containsKey("b"));
		Assert.assertEquals(item.getMap("a").get("b"), "foo");
	}
	
	@Test
	public void test_replace_existingNestedPath_string() throws Exception {
		// setup
		table.putItem(Item.fromMap(ImmutableMap.<String, Object> builder()
			.put(KEY_ATTRIBUTE_NAME, VALUE)
			.put("a", ImmutableMap.of("a", 2L, "b", true))
			.build()));
		
		String patchExpression = "[ { \"op\": \"replace\", \"path\": \"/a/b\", \"value\": \"bar\" } ]";
		JsonNode jsonNode = JsonLoader.fromString(patchExpression);
		JsonPatch jsonPatch = JsonPatch.fromJson(jsonNode);
		// exercise
		ExpressionSpecBuilder builder = jsonPatch.get();
		UpdateItemExpressionSpec spec = builder.buildForUpdate();
		table.updateItem(KEY_ATTRIBUTE_NAME, VALUE, spec);
		// verify
		Item item = table.getItem(PK);
		Assert.assertTrue(item.hasAttribute("key"));
		Assert.assertEquals(item.getString("key"), "keyValue");
		Assert.assertTrue(item.hasAttribute("a"));
		Assert.assertTrue(item.getRawMap("a").containsKey("a"));
		Assert.assertEquals(((BigDecimal) item.getRawMap("a").get("a")).longValue(), 2L);
		Assert.assertTrue(item.getRawMap("a").containsKey("b"));
		Assert.assertEquals(item.getRawMap("a").get("b"), "bar");
	}
	
	@Test(expectedExceptions = AmazonServiceException.class)
	public void test_replace_property_toScalar_string() throws Exception {
		// setup
		table.putItem(Item.fromMap(ImmutableMap.<String, Object> builder()
			.put(KEY_ATTRIBUTE_NAME, VALUE)
			.put("a", 1L)
			.build()));
		
		String patchExpression = "[ { \"op\": \"replace\", \"path\": \"/a/b\", \"value\": \"bar\" } ]";
		JsonNode jsonNode = JsonLoader.fromString(patchExpression);
		JsonPatch jsonPatch = JsonPatch.fromJson(jsonNode);
		// exercise
		ExpressionSpecBuilder builder = jsonPatch.get();
		UpdateItemExpressionSpec spec = builder.buildForUpdate();
		table.updateItem(KEY_ATTRIBUTE_NAME, VALUE, spec);
	}
	
	@Test
	public void test_replace_singlePath_numberSet() throws Exception {
		// setup
		String patchExpression = "[ { \"op\": \"replace\", \"path\": \"/a\", \"value\": [1,2] } ]";
		JsonNode jsonNode = JsonLoader.fromString(patchExpression);
		JsonPatch jsonPatch = JsonPatch.fromJson(jsonNode);
		// exercise
		ExpressionSpecBuilder builder = jsonPatch.get();
		UpdateItemExpressionSpec spec = builder.buildForUpdate();
		table.updateItem(KEY_ATTRIBUTE_NAME, VALUE, spec);
		// verify
		Item item = table.getItem(PK);
		Assert.assertTrue(item.hasAttribute("key"));
		Assert.assertEquals(item.getString("key"), "keyValue");
		Assert.assertTrue(item.hasAttribute("a"));
		//number comparisons are failing so comment this out for now
		Assert.assertTrue(item.getList("a").contains(BigDecimal.valueOf(1L)));
		Assert.assertTrue(item.getList("a").contains(BigDecimal.valueOf(2L)));
	}
	
	@Test
	public void test_replace_singlePath_stringSet() throws Exception {
		// setup
		String patchExpression = "[ { \"op\": \"replace\", \"path\": \"/a\", \"value\": [\"foo\",\"bar\"] } ]";
		JsonNode jsonNode = JsonLoader.fromString(patchExpression);
		JsonPatch jsonPatch = JsonPatch.fromJson(jsonNode);
		// exercise
		ExpressionSpecBuilder builder = jsonPatch.get();
		UpdateItemExpressionSpec spec = builder.buildForUpdate();
		table.updateItem(KEY_ATTRIBUTE_NAME, VALUE, spec);
		// verify
		Item item = table.getItem(PK);
		Assert.assertTrue(item.hasAttribute("key"));
		Assert.assertEquals(item.getString("key"), "keyValue");
		Assert.assertTrue(item.hasAttribute("a"));
		Assert.assertTrue(item.getList("a").contains("foo"));
		Assert.assertTrue(item.getList("a").contains("bar"));
	}
	
	@Test
	public void test_replace_replaceExisting_singlePath_stringSet() throws Exception {
		// setup
		table.putItem(Item.fromMap(ImmutableMap.<String, Object> builder()
			.put(KEY_ATTRIBUTE_NAME, VALUE)
			.put("a", ImmutableSet.of("foo", "bar"))
			.build()));
		
		String patchExpression = "[ { \"op\": \"replace\", \"path\": \"/a\", \"value\": [\"baz\",\"qux\"] } ]";
		JsonNode jsonNode = JsonLoader.fromString(patchExpression);
		JsonPatch jsonPatch = JsonPatch.fromJson(jsonNode);
		// exercise
		ExpressionSpecBuilder builder = jsonPatch.get();
		UpdateItemExpressionSpec spec = builder.buildForUpdate();
		table.updateItem(KEY_ATTRIBUTE_NAME, VALUE, spec);
		// verify
		Item item = table.getItem(PK);
		Assert.assertTrue(item.hasAttribute("key"));
		Assert.assertEquals(item.getString("key"), "keyValue");
		Assert.assertTrue(item.hasAttribute("a"));
		Assert.assertEquals(item.getList("a").size(), 2);
		Assert.assertTrue(item.getList("a").contains("baz"));
		Assert.assertTrue(item.getList("a").contains("qux"));
	}
	
	@Test
	public void test_replace_singlePath_object() throws Exception {
		// setup
		String patchExpression = "[ { \"op\": \"replace\", \"path\": \"/a\", \"value\": {\"b\": \"c\", \"d\": 1} } ]";
		JsonNode jsonNode = JsonLoader.fromString(patchExpression);
		JsonPatch jsonPatch = JsonPatch.fromJson(jsonNode);
		// exercise
		ExpressionSpecBuilder builder = jsonPatch.get();
		UpdateItemExpressionSpec spec = builder.buildForUpdate();
		table.updateItem(KEY_ATTRIBUTE_NAME, VALUE, spec);
		// verify
		Item item = table.getItem(PK);
		Assert.assertTrue(item.hasAttribute("key"));
		Assert.assertEquals(item.getString("key"), "keyValue");
		Assert.assertTrue(item.hasAttribute("a"));
		Assert.assertTrue(item.getRawMap("a").containsKey("b"));
		Assert.assertEquals(item.getRawMap("a").get("b"), "c");
		Assert.assertTrue(item.getRawMap("a").containsKey("d"));
		Assert.assertEquals(((BigDecimal) item.getRawMap("a").get("d")).longValue(), 1L);
	}
}
