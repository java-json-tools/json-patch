[![License LGPLv3][LGPLv3 badge]][LGPLv3]
[![License ASL 2.0][ASL 2.0 badge]][ASL 2.0]
[![Build Status][Travis badge]][Travis]
[![Maven Central][Maven Central badge]][Maven]

## Read me first

This project, as of version 1.4, is licensed under both LGPLv3 and ASL 2.0. See
file LICENSE for more details. Versions 1.3 and lower are licensed under LGPLv3
only.

**Note the "L" in "LGPL". LGPL AND GPL ARE QUITE DIFFERENT!**

## What this is

This is an implementation of [RFC 6902 (JSON Patch)](http://tools.ietf.org/html/rfc6902) and [RFC
7386 (JSON
Merge Patch)](http://tools.ietf.org/html/rfc7386) written in Java,
which uses [Jackson](https://github.com/FasterXML/jackson-databind) (2.2.x) at its core.

Its features are:

* {de,}serialization of JSON Patch and JSON Merge Patch instances with Jackson;
* full support for RFC 6902 operations, including `test`;
* JSON "diff" (RFC 6902 only) with operation factorization.

## Versions

The current version is **1.13**. See file `RELEASE-NOTES.md` for details of releases before 1.11.

## Using it in your project

With Gradle:

```groovy
dependencies {
    compile(group: "com.github.java-json-tools", name: "json-patch", version: "yourVersionHere");
}
```

With Maven:

```xml
<dependency>
    <groupId>com.github.java-json-tools</groupId>
    <artifactId>json-patch</artifactId>
    <version>yourVersionHere</version>
</dependency>
```

Versions before 1.10 are available at `groupId` `com.github.fge`.

## JSON "diff" factorization

When computing the difference between two JSON texts (in the form of `JsonNode` instances), the diff
will factorize value removals and additions as moves and copies.

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
[ { "op": "move", "from": "/a", "path": "/c" } ]
```

It is able to do even more than that. See the test files in the project.

## Note about the `test` operation and numeric value equivalence

RFC 6902 mandates that when testing for numeric values, however deeply nested in the tested value,
a test is successful if the numeric values are _mathematically equal_. That is, JSON texts:

```json
1
```

and:

```json
1.00
```

must be considered equal.

This implementation obeys the RFC; for this, it uses the numeric equivalence of
[jackson-coreutils](https://github.com/fge/jackson-coreutils).

## Sample usage

### JSON Patch

You have two choices to build a `JsonPatch` instance: use Jackson deserialization, or initialize one
directly from a `JsonNode`. Examples:

```java
// Using Jackson
final ObjectMapper mapper = new ObjectMapper();
final InputStream in = ...;
final JsonPatch patch = mapper.readValue(in, JsonPatch.class);
// From a JsonNode
final JsonPatch patch = JsonPatch.fromJson(node);
```

You can then apply the patch to your data:

```java
// orig is also a JsonNode
final JsonNode patched = patch.apply(orig);
```

### JSON diff

The main class is `JsonDiff`. It returns the patch as a `JsonPatch` or as a `JsonNode`. Sample usage:

```java
final JsonPatch patch = JsonDiff.asJsonPatch(source, target);
final JsonNode patchNode = JsonDiff.asJson(source, target);
```

**Important note**: the API offers **no guarantee at all** about patch "reuse";
that is, the generated patch is only guaranteed to safely transform the given
source to the given target. Do not expect it to give the result you expect on
another source/target pair!

### JSON Merge Patch

As for `JsonPatch`, you may use either Jackson or "direct" initialization:

```java
// With Jackson
final JsonMergePatch patch = mapper.readValue(in, JsonMergePatch.class);
// With a JsonNode
final JsonMergePatch patch = JsonMergePatch.fromJson(node);
```

Applying a patch also uses an `.apply()` method:

```java
// orig is also a JsonNode
final JsonNode patched = patch.apply(orig);
```

[LGPLv3 badge]: https://img.shields.io/:license-LGPLv3-blue.svg
[LGPLv3]: http://www.gnu.org/licenses/lgpl-3.0.html
[ASL 2.0 badge]: https://img.shields.io/:license-Apache%202.0-blue.svg
[ASL 2.0]: http://www.apache.org/licenses/LICENSE-2.0.html
[Travis Badge]: https://travis-ci.com/java-json-tools/json-patch.svg?branch=master
[Travis]: https://travis-ci.com/java-json-tools/json-patch
[Maven Central badge]: https://img.shields.io/maven-central/v/com.github.java-json-tools/json-patch.svg
[Maven]: https://search.maven.org/artifact/com.github.java-json-tools/json-patch
