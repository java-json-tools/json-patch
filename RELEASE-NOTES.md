## 1.7

* Fix bug with multiple array renames; detected by @royclarkson, fixed by
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


