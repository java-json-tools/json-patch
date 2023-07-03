package com.gravity9.jsonpatch.model;

public class SimpleModel {

    private String stringField;

    private int intField;

    private long longField;

    private float floatField;

    private double doubleField;

    private boolean booleanField;

    private EmbeddedModel embeddedField;

    public SimpleModel(String stringField, int intField, long longField, float floatField, double doubleField,
                       boolean booleanField, EmbeddedModel embeddedField) {
        this.stringField = stringField;
        this.intField = intField;
        this.longField = longField;
        this.floatField = floatField;
        this.doubleField = doubleField;
        this.booleanField = booleanField;
        this.embeddedField = embeddedField;
    }

    public String getStringField() {
        return stringField;
    }

    public void setStringField(String stringField) {
        this.stringField = stringField;
    }

    public int getIntField() {
        return intField;
    }

    public void setIntField(int intField) {
        this.intField = intField;
    }

    public long getLongField() {
        return longField;
    }

    public void setLongField(long longField) {
        this.longField = longField;
    }

    public float getFloatField() {
        return floatField;
    }

    public void setFloatField(float floatField) {
        this.floatField = floatField;
    }

    public double getDoubleField() {
        return doubleField;
    }

    public void setDoubleField(double doubleField) {
        this.doubleField = doubleField;
    }

    public boolean isBooleanField() {
        return booleanField;
    }

    public void setBooleanField(boolean booleanField) {
        this.booleanField = booleanField;
    }

    public EmbeddedModel getEmbeddedField() {
        return embeddedField;
    }

    public void setEmbeddedField(EmbeddedModel embeddedField) {
        this.embeddedField = embeddedField;
    }
}
