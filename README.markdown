## Read me first

The license of this project is LGPLv3 or later. See file src/main/resources/LICENSE for the full
text.

## What this is

This is a full-featured implementation of [JSON
Patch](http://tools.ietf.org/html/draft-ietf-appsawg-json-patch-10) written in Java, which uses
[Jackson](http://jackson.codehaus.org) at its core.

There is also, as of version 1.1, the ability to generate a "JSON diff"; that is, given two JSON
values, you can generate a JSON Patch so as to turn one JSON value into another JSON value. See
below for more information.

## Versions

The current version is **1.2**.

## Maven artifact

Replace _your-version-here_ with the appropriate version:

```xml
<dependency>
    <groupId>com.github.fge</groupId>
    <artifactId>json-patch</artifactId>
    <version>your-version-here</version>
</dependency>
```

## JSON "diff": the two implementations

### New implementation (versions 1.2 and above)

The new implementation (`com.github.fge.jsonpatch.diff.JsonDiff`) is able, unlike the old one, to
factorize add and remove operations into both moves and copies.

This implementation is courtesy of [Randy Watler](https://github.com/rwatler) and should now be used
in preference to the old one.

### Old implementation (versions 1.1 and below)

The old implementation (`com.github.fge.jsonpatch.JsonDiff`), available since version 1.1, is fully
functional but is quite naive. It is scheduled for removal in version 1.4.

It is functional in the sense that given two JSON values, it will always generate the correct JSON
Patch (as a `JsonNode`). It is however quite naive in the sense that it does not try and factorize
any operations.

Since 1.2, it is marked as obsolete, and you should consider using the newer one.

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

The backing, naive class is `JsonDiff`. It returns the patch as a `JsonNode`. Sample usage:

```java
final JsonNode patchNode = JsonDiff.asJson(firstNode, secondNode);
```

You can then use the generated `JsonNode` to build a patch using the code sample above.

Note that the generated patch will always yield operations in the same order:

* additions,
* removals,
* replacements.

The patch is generated recursively, and numeric equality is also respected.

## Further note about JSON diff

There is one important thing to consider when using JSON diff: in order to comply with JSON Patch
test operations, numeric JSON values are considered equal if they are mathematically equal.

This is arguably debatable: for instance, are `[ 1 ]` and `[ 1.0 ]` the same? Right now, this
implementation considers that they are. It may, or may not, lead to problems; it is unknown whether
this will be a problem given the scarce usage of JSON Patch at this point in time.

There is, however, a good reason that the implementation behaves this way: JSON Patch's test
operation does behave this way -- that is, two numeric JSON values are equal if their _mathematical_
value is equal.

