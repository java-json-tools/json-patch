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

public class JsonPatchToExpressionSpecBuilderRemoveIT {
	private static final String TABLE_NAME = "json_patch_test";

	private static final String KEY_ATTRIBUTE_NAME = "key";
	
	private static final String VALUE = "keyValue";
	
	private static final PrimaryKey PK = new PrimaryKey(KEY_ATTRIBUTE_NAME, VALUE);
	
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
	
	@Test
	public void test_remove_singlePath() throws Exception {
		// setup
		table.putItem(Item.fromMap(ImmutableMap.<String, Object> builder()
			.put(KEY_ATTRIBUTE_NAME, VALUE)
			.put("a", ImmutableMap.of("a", 2, "b", true))
			.build()));
		
		// setup
		String patchExpression = "[ { \"op\": \"remove\", \"path\": \"/a\" } ]";
		JsonNode jsonNode = JsonLoader.fromString(patchExpression);
		JsonPatch jsonPatch = JsonPatch.fromJson(jsonNode);
		// exercise
		ExpressionSpecBuilder actual = jsonPatch.get();
		UpdateItemExpressionSpec actualSpec = actual.buildForUpdate();
		table.updateItem(KEY_ATTRIBUTE_NAME, VALUE, actualSpec);
		// verify
		Item item = table.getItem(PK);
		Assert.assertTrue(item.hasAttribute("key"));
		Assert.assertEquals(item.getString("key"), "keyValue");
		Assert.assertFalse(item.hasAttribute("a"));
	}
	
	@Test
	public void test_remove_nestedPath() throws Exception {
		// setup
		table.putItem(Item.fromMap(ImmutableMap.<String, Object> builder()
			.put(KEY_ATTRIBUTE_NAME, VALUE)
			.put("a", ImmutableMap.of("a", 2, "b", true))
			.build()));
		
		// setup
		String patchExpression = "[ { \"op\": \"remove\", \"path\": \"/a/a\" } ]";
		JsonNode jsonNode = JsonLoader.fromString(patchExpression);
		JsonPatch jsonPatch = JsonPatch.fromJson(jsonNode);
		// exercise
		ExpressionSpecBuilder actual = jsonPatch.get();
		UpdateItemExpressionSpec actualSpec = actual.buildForUpdate();
		table.updateItem(KEY_ATTRIBUTE_NAME, VALUE, actualSpec);
		// verify
		Item item = table.getItem(PK);
		Assert.assertTrue(item.hasAttribute("key"));
		Assert.assertEquals(item.getString("key"), "keyValue");
		Assert.assertTrue(item.hasAttribute("a"));
		Assert.assertTrue(item.getRawMap("a").containsKey("b"));
		Assert.assertEquals(item.getRawMap("a").get("b"), true);
		Assert.assertFalse(item.getRawMap("a").containsKey("a"));
	}
	
	@Test
	public void test_remove_absentPath() throws Exception {
		// setup
		table.putItem(Item.fromMap(ImmutableMap.<String, Object> builder()
			.put(KEY_ATTRIBUTE_NAME, VALUE)
			.put("a", "b")
			.build()));
		
		// setup
		String patchExpression = "[ { \"op\": \"remove\", \"path\": \"/c\" } ]"; // $.c does not exist in target
		JsonNode jsonNode = JsonLoader.fromString(patchExpression);
		JsonPatch jsonPatch = JsonPatch.fromJson(jsonNode);
		// exercise
		ExpressionSpecBuilder actual = jsonPatch.get();
		UpdateItemExpressionSpec actualSpec = actual.buildForUpdate();
		table.updateItem(KEY_ATTRIBUTE_NAME, VALUE, actualSpec);
		// verify
		Item item = table.getItem(PK);
		Assert.assertTrue(item.hasAttribute("key"));
		Assert.assertEquals(item.getString("key"), "keyValue");
		Assert.assertTrue(item.hasAttribute("a"));
		Assert.assertEquals(item.getString("a"), "b");
	}
	
	@Test(expectedExceptions = AmazonServiceException.class)
	public void test_remove_absentObjectPath() throws Exception {
		// setup
		table.putItem(Item.fromMap(ImmutableMap.<String, Object> builder()
			.put(KEY_ATTRIBUTE_NAME, VALUE)
			.put("a", "b")
			.build()));
		
		// setup
		String patchExpression = "[ { \"op\": \"remove\", \"path\": \"/c/d\" } ]"; // $.c does not exist in target
		JsonNode jsonNode = JsonLoader.fromString(patchExpression);
		JsonPatch jsonPatch = JsonPatch.fromJson(jsonNode);
		// exercise
		ExpressionSpecBuilder actual = jsonPatch.get();
		UpdateItemExpressionSpec actualSpec = actual.buildForUpdate();
		table.updateItem(KEY_ATTRIBUTE_NAME, VALUE, actualSpec);
	}
}
