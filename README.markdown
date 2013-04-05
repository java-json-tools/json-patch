## Read me first

The license of this project is LGPLv3 or later. See file src/main/resources/LICENSE for the full
text.

## What this is

This is a full-featured implementation of [JSON
Patch](http://tools.ietf.org/html/draft-ietf-appsawg-json-patch-10) written in Java, which uses
[Jackson](http://jackson.codehaus.org) at its core.

There is also a "JSON diff" implementation, insofar as you can generate a JSON Patch from two JSON
values. More details below.

## Versions

The current version is **1.1**.

## Maven artifact

Replace _your-version-here_ with the appropriate version:

```xml
<dependency>
    <groupId>com.github.fge</groupId>
    <artifactId>json-patch</artifactId>
    <version>your-version-here</version>
</dependency>
```

## Sample usage: JSON Patch

Both the JSON Patch and data to patch are backed by `JsonNode` instances. As this package depends on
[jackson-coreutils](https://github.com/fge/jackson-coreutils), you can use this package's
`JsonLoader` to load your JSON documents.

You then build a JSON Patch from a JsonNode using:

```java
// Throws IOException if the patch is incorrect
final JsonPatch patch = JsonPatch.fromJson(node);
```

You can then apply the patch to your data:

```java
// Throws JsonPatchException if the patch cannot be applied
final JsonNode patched = patch.apply(orig);
```

## Sample usage: JSON diff

The backing class is `JsonDiff`. It returns the patch as a `JsonNode`. Sample usage:

```java
final JsonNode patchNode = JsonDiff.asJson(firstNode, secondNode);
```

You can then use the generated `JsonNode` to build a patch using the code sample above.

Note that the generated patch will always yield operations in the same order:

* additions,
* removals,
* replacements.

The patch is generated recursively, and numeric equality is also respected.

## Notes about JSON diff

There are two things to consider when using JSON diff:

* as for JSON Patch's test operations, numeric JSON values are considered equal if they are
  mathematically equal;
* operations are not "factorized" (see the javadoc of `JsonDiff` for more details).

The first point is arguably debatable (for instance, are `[ 1 ]` and `[ 1.0 ]` the same?). The
second point could probably be fixed. Now, the question is whether it is worth the extra work.

