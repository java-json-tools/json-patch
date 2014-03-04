## Read me first

This project, as of version 1.4, is licensed under both LGPLv3 and ASL 2.0. See
file LICENSE for more details. Versions 1.3 and lower are licensed under LGPLv3
only.

**Note the "L" in "LGPL". LGPL AND GPL ARE QUITE DIFFERENT!**

## What this is

This is a full-featured implementation of [RFC 6902 (JSON
Patch)](http://tools.ietf.org/html/rfc6902) written in Java, which uses
[Jackson](https://github.com/FasterXML/jackson-databind) (2.2.x) at its core.

There is also, as of version 1.1, the ability to generate a "JSON diff"; that is, given two JSON
values, you can generate a JSON Patch so as to turn one JSON value into another JSON value. See
below for more information.

Starting from version 1.5, this package also supports [JSON Merge
Patch](http://tools.ietf.org/html/draft-ietf-appsawg-json-merge-patch-02) and full
serialization/deserialization via Jackson.

## Versions

The current version is **1.5**. See file `RELEASE-NOTES.md` for details.

## Using it in your project

With Gradle:

```groovy
dependencies {
    compile(group: "com.github.fge", name: "json-patch", version: "yourVersionHere");
}
```

With Maven:

```xml
<dependency>
    <groupId>com.github.fge</groupId>
    <artifactId>json-patch</artifactId>
    <version>yourVersionHere</version>
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

As of version 1.5, The `JsonPatch` class, which implements JSON Patch, implements full
serialization/deserialization via Jackson (2.x, NOT 1.9.x). You can therefore use an `ObjectMapper`
to read a patch from any source `ObjectMapper` allows. For instance:

```
final ObjectMapper mapper = new ObjectMapper();
final InputStream in = ...;
final JsonPatch patch = mapper.readValue(in, JsonPatch.class);
```

Alternatively (with 1.5 or older), you can use `JsonNode` instances. As this package depends on
[jackson-coreutils](https://github.com/fge/jackson-coreutils), you can use this package's
`JsonLoader` to load your JSON documents.

You then build a JSON Patch from a JsonNode using:

```java
final JsonPatch patch = JsonPatch.fromJson(node);
```

You can then apply the patch to your data:

```java
// orig is also a JsonNode
final JsonNode patched = patch.apply(orig);
```

### JSON diff

The main class is `JsonDiff`. It returns the patch as a `JsonNode`. Sample usage:

```java
final JsonNode patchNode = JsonDiff.asJson(firstNode, secondNode);
```

You can then use the generated `JsonNode` to build a patch using the code sample above.

### JSON Merge Patch (new in 1.5)

Since 1.5, this package also provides support for [JSON Merge
Patch](http://tools.ietf.org/html/draft-ietf-appsawg-json-merge-patch-02). This is an alternative to
JSON Patch, which is certainly easier to understand, but which is far less powerful.

Just like `JsonPatch`, the implementing class (`JsonMergePatch`) implements full serialization and
deserialization using Jackson. Therefore you can do:

```java
// With an ObjectMapper
final JsonMergePatch patch = mapper.readValue(in, JsonMergePatch.class);
// With a JsonNode
final JsonMergePatch patch = JsonMergePatch.fromJson(node);
```

Applying a patch also uses an `.apply()` method:

```java
// orig is also a JsonNode
final JsonNode patched = patch.apply(orig);
```

Note that unlike JSON Patch, it is impossible to generate a diff from two inputs.

