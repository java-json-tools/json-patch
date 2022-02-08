package com.github.fge.jsonpatch;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class JsonPathParserTest {

    @Test
    public void shouldConvertQueryToJsonPath() throws JsonPatchException {
        String jsonPointerWithQuery = "/productPrice/prodPriceAlteration?productPrice.name=Regular Price";
        String expected = "$.productPrice[?(@.name=='Regular Price')].prodPriceAlteration";
        String result = JsonPathParser.tmfStringToJsonPath(jsonPointerWithQuery);
        assertEquals(result, expected);
    }

    @Test
    public void shouldConvertArrayPathToJsonPath() throws JsonPatchException {
        String jsonPointerWithQuery = "/2/1/-";
        String expected = "$.[2].[1].-";
        String result = JsonPathParser.tmfStringToJsonPath(jsonPointerWithQuery);
        assertEquals(result, expected);
    }

    @Test
    public void shouldConvertBooleans() throws JsonPatchException {
        String jsonPointerWithQuery = "/orderItem/quantity?orderItem.productOffering.valid=true&orderItem.product.relatedParty.role=customer";
        String expected = "$.orderItem[?((@.productOffering.valid==true || @.productOffering.valid=='true') && @.product.relatedParty.role=='customer')].quantity";
        String result = JsonPathParser.tmfStringToJsonPath(jsonPointerWithQuery);
        assertEquals(result, expected);
    }

    @Test
    public void shouldConvertFloatingPoint() throws JsonPatchException {
        String jsonPointerWithQuery = "/orderItem/quantity?orderItem.productOffering.price=1513.77&orderItem.product.relatedParty.role=customer";
        String expected = "$.orderItem[?((@.productOffering.price==1513.77 || @.productOffering.price=='1513.77') && @.product.relatedParty.role=='customer')].quantity";
        String result = JsonPathParser.tmfStringToJsonPath(jsonPointerWithQuery);
        assertEquals(result, expected);
    }

    @Test
    public void shouldConvertIntegers() throws JsonPatchException {
        String jsonPointerWithQuery = "/orderItem/quantity?orderItem.productOffering.id=1513&orderItem.product.relatedParty.role=customer";
        String expected = "$.orderItem[?((@.productOffering.id==1513 || @.productOffering.id=='1513') && @.product.relatedParty.role=='customer')].quantity";
        String result = JsonPathParser.tmfStringToJsonPath(jsonPointerWithQuery);
        assertEquals(result, expected);
    }

    @Test
    public void shouldConvertManyConditions() throws JsonPatchException {
        String jsonPointerWithQuery = "/orderItem/quantity?orderItem.product.relatedParty.role=customer&orderItem.product.relatedParty.name=Mary";
        String expected = "$.orderItem[?(@.product.relatedParty.role=='customer' && @.product.relatedParty.name=='Mary')].quantity";
        String result = JsonPathParser.tmfStringToJsonPath(jsonPointerWithQuery);
        assertEquals(result, expected);
    }

    @Test
    public void shouldConvertNestedArrayQuery() throws JsonPatchException {
        String jsonPointerWithQuery = "/orderItem/quantity?orderItem.productOffering.id=1513&orderItem.product.relatedParty[?(@.role=='customer' && @.name=='Mary')]";
        String expected = "$.orderItem[?((@.productOffering.id==1513 || @.productOffering.id=='1513') && @.product.relatedParty[?(@.role=='customer' && @.name=='Mary')] empty false)].quantity";
        String result = JsonPathParser.tmfStringToJsonPath(jsonPointerWithQuery);
        assertEquals(result, expected);
    }

    @Test
    public void shouldConvertNestedArrayQueryWhichIsNotLastStatement() throws JsonPatchException {
        String jsonPointerWithQuery = "/orderItem/quantity?orderItem.product.relatedParty[?(@.role=='customer' && @.name=='Mary')]&orderItem.productOffering.id=1513";
        String expected = "$.orderItem[?(@.product.relatedParty[?(@.role=='customer' && @.name=='Mary')] empty false && (@.productOffering.id==1513 || @.productOffering.id=='1513'))].quantity";
        String result = JsonPathParser.tmfStringToJsonPath(jsonPointerWithQuery);
        assertEquals(result, expected);
    }

    @Test
    public void shouldConvertFilterQuery() throws JsonPatchException {
        String filterQuery = "note[?(@.author=='John Doe')].date";
        String expected = "$.note[?(@.author=='John Doe')].date";
        String result = JsonPathParser.tmfStringToJsonPath(filterQuery);
        assertEquals(result, expected);
    }

}