package com.gravity9.jsonpatch.model;

public class EmbeddedModel {

    private String embeddedField;

    public EmbeddedModel(String embeddedField) {
        this.embeddedField = embeddedField;
    }

    public String getEmbeddedField() {
        return embeddedField;
    }

    public void setEmbeddedField(String embeddedField) {
        this.embeddedField = embeddedField;
    }
}
