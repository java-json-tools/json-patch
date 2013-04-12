## Read me first

The license of this project is LGPLv3 or later. See file src/main/resources/LICENSE for the full
text.

## What this is

This is a full-featured implementation of [RFC 6902 (JSON
Patch)](http://tools.ietf.org/html/rfc6902) written in Java, which uses
[Jackson](http://jackson.codehaus.org) at its core.

There is also, as of version 1.1, the ability to generate a "JSON diff"; that is, given two JSON
values, you can generate a JSON Patch so as to turn one JSON value into another JSON value. See
below for more information.

## Versions

The current version is **1.3**.

## Maven artifact

Replace _your-version-here_ with the appropriate version:

```xml
<dependency>
    <groupId>com.github.fge</groupId>
    <artifactId>json-patch</artifactId>
    <version>your-version-here</version>
</dependency>
```

## JSON "diff"

As its name implies, this is a reverse of the patch operation.

The implementation takes two JSON values as arguments (as `JsonNode` instances) and returns a JSON
patch, also as a `JsonNode`.

This implementation is courtesy of [Randy Watler](https://github.com/rwatler). It is able to
factorize value removals and additions as moves and copies.

For instance, given this node to patch:

```json
{ "a": "b" }
```

in order to obtain:

```json
{ "c": "b" }
```

the implementation will return the following patch:

```json
[ { "op": "move", "from": "/a", "to": "/c" } ]
```

### Important note

In order to comply with JSON Patch test operations, numeric JSON values are considered equal if they
are mathematically equal.

This is arguably debatable: for instance, are `[ 1 ]` and `[ 1.0 ]` the same? Right now, this
implementation considers that they are. It may, or may not, lead to problems; it is unknown whether
this will be a problem given the scarce usage of JSON Patch at this point in time.

There is, however, a good reason that the implementation behaves this way: JSON Patch's test
operation does behave this way -- that is, two numeric JSON values are equal if their mathematical
value is equal.

## Sample usage

### JSON Patch

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

### JSON diff

The main class is `JsonDiff`. It returns the patch as a `JsonNode`. Sample usage:

```java
final JsonNode patchNode = JsonDiff.asJson(firstNode, secondNode);
```

You can then use the generated `JsonNode` to build a patch using the code sample above.

