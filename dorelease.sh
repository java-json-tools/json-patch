#!/bin/bash

#
# This will build everything that is needed and push to Maven central.
#

./gradlew --recompile-scripts clean test uploadArchives

