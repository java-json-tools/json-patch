# This project is looking for a new maintainer

I don't have the time, and worse, the incentive, to maintain this project anymore.

If you are interested in taking over, please [contact me personally](mailto:fgaliegue@gmail.com).

## Read me first

This project, as of version 1.4, is licensed under both LGPLv3 and ASL 2.0. See
file LICENSE for more details. Versions 1.3 and lower are licensed under LGPLv3
only.

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

The current version is **1.9**. See file `RELEASE-NOTES.md` for details.

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

You have to choices to build a `JsonPatch` instance: use Jackson deserialization, or initialize one
directly from a `JsonNode`. Examples:

```
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

### JSON patch history

All patch operations allow you to store the history of elements for future reference. This allows systems to verify 
that the patches applied are not altering values which may have changed since the generation of the patch. The 
history elements are tied directly to paths in the operation and are simply the <pathName>Value. These elements have 
no bearing when applying the patch. 

```json
{ "op": { "op": "add", "path": "/a", "value": 1, "pathValue": 2 } }
{ "op": { "op": "move", "from": "/a", "path": "/b", "fromValue": 1, "pathValue": 2 } }
```

The history has the ability to distinguish between a missing value and being explicitly set to null. If there was no 
value the history element will not be present. If the value was explicitly set to null the history element will be 
present and set to null. The java implementation also distinguishes between these two by using null when missing
from the json and NullNode when set explicitly to null in the json.

Json Diff has the ability to generate patches with or without the history elements present.

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

