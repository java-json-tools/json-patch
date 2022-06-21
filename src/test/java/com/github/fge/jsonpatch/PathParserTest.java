package com.github.fge.jsonpatch;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class PathParserTest {

    @Test
    void should_parse_json_pointer_to_json_path() throws JsonPatchException {
        // given
        String path = "/user/1/name";

        // when
        PathDetails result = PathParser.getParentPathAndNewNodeName(path);

        // then
        assertEquals(result.getPathToParent(), "$['user'][1]");
        assertEquals(result.getNewNodeName(), "name");
    }

    @Test
    void should_parse_json_pointer_with_embedded_field_to_json_path() throws JsonPatchException {
        // given
        String path = "/user/name";

        // when
        PathDetails result = PathParser.getParentPathAndNewNodeName(path);

        // then
        assertEquals(result.getPathToParent(), "$['user']");
        assertEquals(result.getNewNodeName(), "name");
    }

    @Test
    void should_parse_json_path_with_dot_notation() throws JsonPatchException {
        // given
        String path = "$.user[1].name";

        // when
        PathDetails result = PathParser.getParentPathAndNewNodeName(path);

        // then
        assertEquals(result.getPathToParent(), "$['user'][1]");
        assertEquals(result.getNewNodeName(), "name");
    }

    @Test
    void should_parse_json_path_with_bracket_notation() throws JsonPatchException {
        // given
        String path = "$['user'][1]['name']";

        // when
        PathDetails result = PathParser.getParentPathAndNewNodeName(path);

        // then
        assertEquals(result.getPathToParent(), "$['user'][1]");
        assertEquals(result.getNewNodeName(), "name");
    }

    @Test
    void should_parse_json_path_with_mixed_notation() throws JsonPatchException {
        // given
        String path = "$['user'][1].name";

        // when
        PathDetails result = PathParser.getParentPathAndNewNodeName(path);

        // then
        assertEquals(result.getPathToParent(), "$['user'][1]");
        assertEquals(result.getNewNodeName(), "name");
    }

    @Test
    void should_parse_json_path_with_filters() throws JsonPatchException {
        // given
        String path = "$[?(@.book[?(@.ratingScore == '111')] empty false)].title";

        // when
        PathDetails result = PathParser.getParentPathAndNewNodeName(path);

        // then
        assertEquals(result.getPathToParent(), "$[?(@.book[?(@.ratingScore == '111')] empty false)]");
        assertEquals(result.getNewNodeName(), "title");
    }

    // This notation is not supported by JsonPath PathCompiler.compile
    @Test(expectedExceptions = JsonPatchException.class)
    void should_throw_exception_with_double_dots_and_length() throws JsonPatchException {
        // given
        String path = "$..book[(@.length-1)].title";

        // when
        PathParser.getParentPathAndNewNodeName(path);
    }

    @Test
    void should_parse_json_path_with_double_dots_and_dynamic_array_index2() throws JsonPatchException {
        // given
        String path = "$..book[-1:].title";

        // when
        PathDetails result = PathParser.getParentPathAndNewNodeName(path);

        // then
        assertEquals(result.getPathToParent(), "$..['book'][-1:]");
        assertEquals(result.getNewNodeName(), "title");
    }

    @Test
    void should_parse_json_path_with_double_dots_and_dynamic_array_index3() throws JsonPatchException {
        // given
        String path = "$..book[1:2].title";

        // when
        PathDetails result = PathParser.getParentPathAndNewNodeName(path);

        // then
        assertEquals(result.getPathToParent(), "$..['book'][1:2]");
        assertEquals(result.getNewNodeName(), "title");
    }

    @Test
    void should_parse_json_path_with_double_dots_and_dynamic_array_index4() throws JsonPatchException {
        // given
        String path = "$..book[1:].title";

        // when
        PathDetails result = PathParser.getParentPathAndNewNodeName(path);

        // then
        assertEquals(result.getPathToParent(), "$..['book'][1:]");
        assertEquals(result.getNewNodeName(), "title");
    }

    @Test
    void should_parse_json_path_with_double_dots_and_dynamic_array_index5() throws JsonPatchException {
        // given
        String path = "$..book[:5].title";

        // when
        PathDetails result = PathParser.getParentPathAndNewNodeName(path);

        // then
        assertEquals(result.getPathToParent(), "$..['book'][:5]");
        assertEquals(result.getNewNodeName(), "title");
    }


    @Test
    void should_parse_json_path_with_star_as_array_index() throws JsonPatchException {
        // given
        String path = "$..book[*].title";

        // when
        PathDetails result = PathParser.getParentPathAndNewNodeName(path);

        // then
        assertEquals(result.getPathToParent(), "$..['book'][*]");
        assertEquals(result.getNewNodeName(), "title");
    }


    @Test(expectedExceptions = JsonPatchException.class)
    void should_throw_json_patch_exception_when_invalid_json_path_provided() throws JsonPatchException {
        // given
        final String invalidPath = "$.user[[]][.name";

        // when
        PathParser.getParentPathAndNewNodeName(invalidPath);
    }

    @Test
    void should_parse_json_path_when_json_path_with_opening_bracket_at_the_end() throws JsonPatchException {
        // given
        final String path = "$.user[0].name[";

        // when
        PathDetails result = PathParser.getParentPathAndNewNodeName(path);

        // then
        assertEquals(result.getPathToParent(), "$['user'][0]");
        assertEquals(result.getNewNodeName(), "name");
    }

    @Test
    void should_treat_index_between_apostrophes_as_field_name() throws JsonPatchException {
        // given
        final String path = "$.user['0'].name[";

        // when
        PathDetails result = PathParser.getParentPathAndNewNodeName(path);

        // then
        assertEquals(result.getPathToParent(), "$['user']['0']");
        assertEquals(result.getNewNodeName(), "name");
    }

    @Test
    void should_parse_json_path_with_top_level_field_without_parent2() throws JsonPatchException {
        // given
        String path = "/id";

        // when
        PathDetails result = PathParser.getParentPathAndNewNodeName(path);

        // then
        assertEquals(result.getPathToParent(), "$");
        assertEquals(result.getNewNodeName(), "id");
    }

    @Test
    void should_parse_json_path_with_top_level_field_without_parent3() throws JsonPatchException {
        // given
        String path = "$.id";

        // when
        PathDetails result = PathParser.getParentPathAndNewNodeName(path);

        // then
        assertEquals(result.getPathToParent(), "$");
        assertEquals(result.getNewNodeName(), "id");
    }

    @Test
    void should_parse_json_path_with_top_level_field_without_parent4() throws JsonPatchException {
        // given
        String path = "$['id']";

        // when
        PathDetails result = PathParser.getParentPathAndNewNodeName(path);

        // then
        assertEquals(result.getPathToParent(), "$");
        assertEquals(result.getNewNodeName(), "id");
    }
}