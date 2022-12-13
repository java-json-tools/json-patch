/*
 * Copyright (c) 2014, Francis Galiegue (fgaliegue@gmail.com)
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

package com.github.fge.jackson.jsonpointer;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;

import javax.annotation.concurrent.Immutable;
import java.sql.Ref;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A {@link TreePointer} for {@link JsonNode}
 *
 * <p>This is the "original" JSON Pointer in that it addresses JSON documents.
 * </p>
 *
 * <p>It also has a lot of utility methods covering several usage scenarios.</p>
 */
@Immutable
public final class JsonPointerCustom
        extends TreePointer<JsonNode> {
    /**
     * The empty JSON Pointer
     */
    private static final JsonPointerCustom EMPTY
            = new JsonPointerCustom(Collections.<TokenResolver<JsonNode>>emptyList());

    /**
     * Return an empty JSON Pointer
     *
     * @return an empty, statically allocated JSON Pointer
     */
    public static JsonPointerCustom empty() {
        return EMPTY;
    }

    /**
     * Build a JSON Pointer out of a series of reference tokens
     *
     * <p>These tokens can be everything; be sure however that they implement
     * {@link Object#toString()} correctly!</p>
     *
     * <p>Each of these tokens are treated as <b>raw</b> tokens (ie, not
     * encoded).</p>
     *
     * @param first the first token
     * @param other other tokens
     * @return a JSON Pointer
     * @throws NullPointerException one input token is null
     */
    public static JsonPointerCustom of(final Object first, final Object... other) {
        final List<ReferenceToken> tokens = new ArrayList<>();
        tokens.add(ReferenceToken.fromRaw(first.toString()));
        for (final Object o : other)
            tokens.add(ReferenceToken.fromRaw(o.toString()));
        return new JsonPointerCustom(fromTokens(tokens));
    }


    /**
     * Build a JSON Pointer out of a series of reference tokens
     * gives path before unknown
     */
    public static JsonPointerCustom getBeforeUnknown(String input) throws JsonPointerException {
        return new JsonPointerCustom(fromTokensGetBefore(tokensFromInput(input)));
    }

    /**
     * Build a JSON Pointer out of a series of reference token
     * gives the path after unknown
     */
    public static JsonPointerCustom getAfterUnknown(String input) throws JsonPointerException {
        return new JsonPointerCustom(fromTokensGetAfter(tokensFromInput(input)));
    }

    private static List<TokenResolver<JsonNode>> fromTokensGetAfter(
            final List<ReferenceToken> tokens) {

        boolean flag = false;
        final List<TokenResolver<JsonNode>> list = new ArrayList<>();

        for (final ReferenceToken token : tokens) {
            if (token.getRaw().equals("?")) {
                flag = true;
                continue;
            }
            if (flag) list.add(new JsonNodeResolver(token));
        }
        return list;
    }


    /**
     * Build a list of token resolvers from a list of reference tokens
     *
     * <p>Here, the token resolvers are {@link JsonNodeResolver}s.</p>
     *
     * @param tokens the token list
     * @return a (mutable) list of token resolvers
     */
    private static List<TokenResolver<JsonNode>> fromTokens(
            final List<ReferenceToken> tokens) {
        final List<TokenResolver<JsonNode>> list = new ArrayList<>();
        for (final ReferenceToken token : tokens) {
            list.add(new JsonNodeResolver(token));
        }
        return list;
    }

    private static List<TokenResolver<JsonNode>> fromTokensGetBefore(
            final List<ReferenceToken> tokens) {
        final List<TokenResolver<JsonNode>> list = new ArrayList<>();
        for (final ReferenceToken token : tokens) {
            if (token.getRaw().equals("?")) break;
            list.add(new JsonNodeResolver(token));
        }
        return list;
    }


    /**
     * The main constructor
     *
     * @param input the input string
     * @throws JsonPointerException malformed JSON Pointer
     * @throws NullPointerException null input
     */
    public JsonPointerCustom(final String input)
            throws JsonPointerException {
        this(fromTokens(tokensFromInput(input)));
    }

    /**
     * Alternate constructor
     *
     * <p>This calls {@link TreePointer#TreePointer(TreeNode, List)} with a
     * {@link MissingNode} as the missing tree node.</p>
     *
     * @param tokenResolvers the list of token resolvers
     */
    public JsonPointerCustom(final List<TokenResolver<JsonNode>> tokenResolvers) {
        super(MissingNode.getInstance(), tokenResolvers);
    }


    /**
     * Return a new pointer with a new token appended
     *
     * @param raw the raw token to append
     * @return a new pointer
     * @throws NullPointerException input is null
     */
    public JsonPointerCustom append(final String raw) {
        final ReferenceToken refToken = ReferenceToken.fromRaw(raw);
        final JsonNodeResolver resolver = new JsonNodeResolver(refToken);
        final List<TokenResolver<JsonNode>> list
                = new ArrayList<>();
        for (final TokenResolver<JsonNode> tokenResolver : tokenResolvers) {
            if (tokenResolver != null) {
                list.add(tokenResolver);
            } else {
                throw new NullPointerException();
            }
        }
        list.add(resolver);
        return new JsonPointerCustom(list);
    }

    /**
     * Return a new pointer with a new integer token appended
     *
     * @param index the integer token to append
     * @return a new pointer
     */
    public JsonPointerCustom append(final int index) {
        return append(Integer.toString(index));
    }

    /**
     * Return a new pointer with another pointer appended
     *
     * @param other the other pointer
     * @return a new pointer
     * @throws NullPointerException other pointer is null
     */
    public JsonPointerCustom append(final JsonPointerCustom other) {
        BUNDLE.checkNotNull(other, "nullInput");
        final List<TokenResolver<JsonNode>> list
                = new ArrayList<>();
        for (final TokenResolver<JsonNode> tokenResolver : tokenResolvers) {
            if (tokenResolver != null) {
                list.add(tokenResolver);
            } else {
                throw new NullPointerException();
            }
        }
        list.addAll(other.tokenResolvers);
        return new JsonPointerCustom(list);
    }

    /**
     * Return the immediate parent of this JSON Pointer
     *
     * <p>The parent of the empty pointer is itself.</p>
     *
     * @return a new JSON Pointer representing the parent of the current one
     */
    public JsonPointerCustom parent() {
        final int size = tokenResolvers.size();
        return size <= 1 ? EMPTY
                : new JsonPointerCustom(tokenResolvers.subList(0, size - 1));
    }


}
