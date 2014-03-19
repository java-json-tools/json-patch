package com.github.fge.jsonpatch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.ReferenceToken;
import com.github.fge.jackson.jsonpointer.TokenResolver;
import com.google.common.collect.Iterables;

/**
 * Represents an operation that can add a {@code value} given a {@code path}.
 */
public abstract class AdditionOperation
    extends PathValueOperation
{
    private static final ReferenceToken LAST_ARRAY_ELEMENT
        = ReferenceToken.fromRaw("-");

    private boolean overwriteExisting;

    /**
     * @param op operation name
     * @param path affected path
     * @param value value to add
     * @param overwriteExisting whether the operation is allowed to overwrite an existing value at the specified path
     */
    protected AdditionOperation(final String op, final JsonPointer path, final JsonNode value, boolean overwriteExisting)
    {
        super(op, path, value);
        this.overwriteExisting = overwriteExisting;
    }

    @Override
    public JsonNode apply(final JsonNode node)
        throws JsonPatchException
    {
        if (path.isEmpty())
            return value;

        /*
         * Check the parent node: it must exist and be a container (ie an array
         * or an object) for the addition operation to work.
         */
        final JsonNode parentNode = path.parent().path(node);
        if (parentNode.isMissingNode())
            throw new JsonPatchException(BUNDLE.getMessage(
                "jsonPatch.noSuchParent"));
        if (!parentNode.isContainerNode())
            throw new JsonPatchException(BUNDLE.getMessage(
                "jsonPatch.parentNotContainer"));
        return parentNode.isArray()
            ? addToArray(path, node)
            : addToObject(path, node);
    }

    private JsonNode addToArray(final JsonPointer path, final JsonNode node)
        throws JsonPatchException
    {
        final JsonNode ret = node.deepCopy();
        final ArrayNode target = (ArrayNode) path.parent().get(ret);
        final TokenResolver<JsonNode> token = Iterables.getLast(path);

        if (token.getToken().equals(LAST_ARRAY_ELEMENT)) {
            target.add(value);
            return ret;
        }

        final int size = target.size();
        final int index;
        try {
            index = Integer.parseInt(token.toString());
        } catch (NumberFormatException ignored) {
            throw new JsonPatchException(BUNDLE.getMessage(
                "jsonPatch.notAnIndex"));
        }

        if (index < 0 || index > size)
            throw new JsonPatchException(BUNDLE.getMessage(
                "jsonPatch.noSuchIndex"));

        target.insert(index, value);
        return ret;
    }

    private JsonNode addToObject(final JsonPointer path, final JsonNode node)
        throws JsonPatchException
    {
        if (!overwriteExisting)
        {
            final JsonNode existingNode = path.path(node);
            if (!existingNode.isMissingNode())
                throw new JsonPatchException(BUNDLE.getMessage(
                    "jsonPatch.valueAtPathAlreadyExists"));
        }

        final JsonNode ret = node.deepCopy();
        final ObjectNode target = (ObjectNode) path.parent().get(ret);
        target.put(Iterables.getLast(path).getToken().getRaw(), value);
        return ret;
    }
}
