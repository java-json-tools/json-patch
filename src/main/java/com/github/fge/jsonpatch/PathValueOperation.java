/*
 * Copyright (c) 2014, Francis Galiegue (fgaliegue@gmail.com)
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

import com.amazonaws.services.dynamodbv2.xspec.ExpressionSpecBuilder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.github.fge.jackson.jsonpointer.JsonPointer;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Base class for patch operations taking a value in addition to a path
 */
public abstract class PathValueOperation
    extends JsonPatchOperation
{
    @JsonSerialize
    protected final JsonNode value;

    /**
     * Protected constructor
     *
     * @param op operation name
     * @param path affected path
     * @param value JSON value
     */
    protected PathValueOperation(final String op, final JsonPointer path,
        final JsonNode value)
    {
        super(op, path);
        this.value = value.deepCopy();
    }

    @Override
    public final void serialize(final JsonGenerator jgen,
        final SerializerProvider provider)
        throws IOException, JsonProcessingException
    {
        jgen.writeStartObject();
        jgen.writeStringField("op", op);
        jgen.writeStringField("path", path.toString());
        jgen.writeFieldName("value");
        jgen.writeTree(value);
        jgen.writeEndObject();
    }

    @Override
    public final void serializeWithType(final JsonGenerator jgen,
        final SerializerProvider provider, final TypeSerializer typeSer)
        throws IOException, JsonProcessingException
    {
        serialize(jgen, provider);
    }
    
	@Override
	public void applyToBuilder(ExpressionSpecBuilder builder) {
		String attributePath = pathGenerator.apply(path);
		JsonNodeType type = value.getNodeType();
		switch (type) {
			case NUMBER:
				builder.addUpdate(ExpressionSpecBuilder.N(attributePath).set(value.numberValue()));
				break;

			case STRING:
				builder.addUpdate(ExpressionSpecBuilder.S(attributePath).set(value.textValue()));
				break;

			case BOOLEAN:
				builder.addUpdate(ExpressionSpecBuilder.BOOL(attributePath).set(value.booleanValue()));
				break;

			case NULL:
				builder.addUpdate(ExpressionSpecBuilder.NULL(attributePath).set());
				break;

			case ARRAY:
				if (value.iterator().hasNext() == false) {
					builder.addUpdate(ExpressionSpecBuilder.L(attributePath).set(Collections.emptyList()));
				} else {
					JsonNode repNode = value.iterator().next();
					if (repNode.isNumber()) {
						builder.addUpdate(ExpressionSpecBuilder.NS(attributePath).set(convertNumberList(value)));
					} else if (repNode.isTextual()) {
						builder.addUpdate(ExpressionSpecBuilder.SS(attributePath).set(convertStringList(value)));
					} else {
						throw new UnsupportedOperationException("Not implemented yet: " + repNode.getNodeType());
					}
				}
				break;

			case OBJECT:
				Map<String, ?> m = toMap(value);
				builder.addUpdate(ExpressionSpecBuilder.M(attributePath).set(m));
				break;

			default:
				// TODO Auto-generated method stub
				throw new UnsupportedOperationException("Not implemented yet: " + type);
		}
	}

	private static Map<String, ?> toMap(JsonNode value) {
		Map<String, Object> m = new LinkedHashMap<String, Object>();
		for (Iterator<Map.Entry<String, JsonNode>> iterator = value.fields(); iterator.hasNext();) {
			Map.Entry<String, JsonNode> e = iterator.next();
			JsonNodeType nodeType = e.getValue().getNodeType();
			if (nodeType.equals(JsonNodeType.OBJECT)) {
				m.put(e.getKey(), toMap(e.getValue()));
			} else if (nodeType.equals(JsonNodeType.BOOLEAN)) {
				m.put(e.getKey(), e.getValue().booleanValue());
			} else if (nodeType.equals(JsonNodeType.NUMBER)) {
				m.put(e.getKey(), e.getValue().numberValue());
			} else if (nodeType.equals(JsonNodeType.STRING)) {
				m.put(e.getKey(), e.getValue().textValue());
			} else if (nodeType.equals(JsonNodeType.ARRAY)) {
				if (e.getValue().iterator().hasNext() == false) {
					m.put(e.getKey(), Collections.emptyList());
				} else {
					JsonNode repNode = e.getValue().iterator().next();
					if (repNode.isNumber()) {
						m.put(e.getKey(), convertNumberList(e.getValue()));
					} else if (repNode.isTextual()) {
						m.put(e.getKey(), convertStringList(e.getValue()));
					} else {
						throw new UnsupportedOperationException("Not implemented yet: " + repNode.getNodeType());
					}
				}
			}
		}
		return m;
	}

	private static Set<Number> convertNumberList(JsonNode parent) {
		Set<Number> ns = new HashSet<Number>();
		for(JsonNode node : parent) {
			ns.add(node.numberValue());
		}
		return ns;
	}
	
	private static Set<String> convertStringList(JsonNode parent) {
		Set<String> ns = new HashSet<String>();
		for(JsonNode node : parent) {
			ns.add(node.textValue());
		}
		return ns;
	}

    @Override
    public final String toString()
    {
        return "op: " + op + "; path: \"" + path + "\"; value: " + value;
    }
}
