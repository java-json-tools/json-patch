package com.github.fge.jsonpatch;

import java.math.BigDecimal;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

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
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.xspec.ExpressionSpecBuilder;
import com.amazonaws.services.dynamodbv2.xspec.UpdateItemExpressionSpec;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.google.common.collect.ImmutableMap;

public class JsonPatchToXSpecAdd {
	private static final String KEY_ATTRIBUTE_NAME = "key";
	
	private static final String VALUE = "keyValue";
	
	private static final PrimaryKey PK = new PrimaryKey(KEY_ATTRIBUTE_NAME, VALUE);
	
	private static final String TABLE_NAME = "json_patch_test";
	
	private Table table;
	
	
	@BeforeTest
	public void setUp() throws Exception {
		AmazonDynamoDB amazonDynamoDB = DynamoDBEmbedded.create().amazonDynamoDB();
		try {
			amazonDynamoDB.deleteTable(TABLE_NAME);
		} catch(ResourceNotFoundException e) {
			//do nothing because the first run will not have the table.
		}
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
	public void testAddSinglePathNumber() throws Exception {
		// setup
		table.putItem(Item.fromMap(ImmutableMap.<String, Object> builder()
				.put(KEY_ATTRIBUTE_NAME, VALUE)
				.build()));
		String patchExpression = "[ { \"op\": \"add\", \"path\": \"/a\", \"value\": 1 } ]";
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
	public void testAddNestedPathString() throws Exception {
		// setup
		table.putItem(Item.fromMap(ImmutableMap.<String, Object> builder()
			.put(KEY_ATTRIBUTE_NAME, VALUE)
			.put("a", ImmutableMap.of("a", 1L))
			.build()));
		
		String patchExpression = "[ { \"op\": \"add\", \"path\": \"/a/b\", \"value\": \"foo\" } ]";
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
	public void createItemWithJsonPatch() throws Exception {
		// setup
		String patchExpression = "[ { \"op\": \"add\", \"path\": \"/a\", \"value\": \"b\" } ]";
		JsonNode jsonNode = JsonLoader.fromString(patchExpression);
		JsonPatch jsonPatch = JsonPatch.fromJson(jsonNode);
		// exercise
		ExpressionSpecBuilder builder = jsonPatch.get();
		UpdateItemExpressionSpec spec = builder.buildForUpdate();
		table.updateItem(KEY_ATTRIBUTE_NAME, VALUE, spec);//throw
		// verify
		Item item = table.getItem(PK); 
		Assert.assertTrue(item.hasAttribute("key"));
		Assert.assertEquals(item.getString("key"), "keyValue");
		Assert.assertTrue(item.hasAttribute("a"));
		Assert.assertEquals(item.getString("a"), "b");
	}
}
