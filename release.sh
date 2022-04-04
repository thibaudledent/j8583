#!/usr/bin/env bash
set -eEuxo pipefail

# Get release version & next dev version
git fetch --tags

echo "Determine the release version of the application from the previous tag and the provided release scope."
CURRENT_DEV_VERSION="$(mvn org.apache.maven.plugins:maven-help-plugin:3.2.0:evaluate -Dexpression=project.version -q -DforceStdout)"
RELEASE_VERSION="$(echo "$CURRENT_DEV_VERSION" | sed -e "s/-SNAPSHOT$//")"
echo "The release version is $RELEASE_VERSION"

NEXT_DEV_VERSION=$(echo "$RELEASE_VERSION" | awk '{split($1,a,"."); print a[1] "." a[2] "."  a[3]+1 "-SNAPSHOT"}')
echo "The next development version is $NEXT_DEV_VERSION"

echo "Preparing release $RELEASE_VERSION."
mvn -B -C release:prepare --settings ./settings.xml \
  -DpushChanges=false \
  -DautoVersionSubmodules \
  -DreleaseVersion="$RELEASE_VERSION" \
  -DdevelopmentVersion="$NEXT_DEV_VERSION" \
  -DscmCommentPrefix="Releasing $RELEASE_VERSION [maven-release-plugin]"

git push origin --tags

echo "Performing release $RELEASE_VERSION."
mvn -B -C -Darguments='-DdeployAtEnd -DskipDepCheck -Dmaven.javadoc.skip=true' release:perform --settings ./settings.xml
