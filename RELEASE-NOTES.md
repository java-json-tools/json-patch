## 2.0.1

* Added public access to JsonPathParser

## 2.0.0

The project has been taken over by [gravity9](https://www.gravity9.com).

* Changed groupId and artifactId
* The library now uses Java 11 as base
* Added support for JSON Path
* Added support for ignoring fields in JSON diff
* Added support for defining a custom ObjectMapper for JsonMergePatch
* Added more context to JsonPatchException thrown in all operations
* Added more test cases and examples
* Upgraded versions of most libraries and tools used in the project
  * Fixed outstanding CVE vulnerabilities where possible
* Multiple bugfixes

## 1.10

* First release at java-json-tools.
* Update Gradle to 3.5.

## 1.9

* Completely new JSON diff implementation; less smart than the previous one but
  bug free
* Depend on AssertJ.

## 1.8

* JSON Merge Patch is now RFC 7386 compliant.
* Merge gradle files; use Spring's propdeps plugin.
* Fix issue #12: name and description now appear in generated site pom.

## 1.7

* Fix bug with diffs and multiple array removals; detected by @royclarkson, fixed by
  @rwatler. See [issue 11](https://github.com/fge/json-patch/issues/11).

## 1.6

* Update jackson-coreutils dependency.
* Change license file placement/text.
* Make all tests run from the command line.
* Disable propdeps plugin for the moment.

## 1.5

* Full Jackson serialization/deserialization support.
* JSON merge-patch support.
  (http://tools.ietf.org/html/draft-ietf-appsawg-json-merge-patch-02)
* Fix bug in add operation where the parent node of the path to add to was not
  a container node.
* Update to gradle 1.11.

## 1.4

* Use gradle for build
* Many backwards-compatible code changes to diff code
* Use msg-simple
* Update TestNG dependency
* Update jackson-coreutils dependency; use new method for "split pointers"


