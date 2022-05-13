#!/usr/bin/env bash
set -eEuxo pipefail

# Install required packages
sudo apt-get update && sudo apt-get install -y gpg gpg-agent git --no-install-recommends

# Export variables for the release
# shellcheck disable=SC2155 # Declare and assign separately to avoid masking return values -> it does not work for tty
export GPG_TTY=$(tty) # to fix the 'gpg: signing failed: Inappropriate ioctl for device', see https://github.com/keybase/keybase-issues/issues/2798#issue-205008630
echo "$GPG_SECRET_KEY" | base64 --decode | gpg --batch --import # use 'batch' otherwise gpg2 is asking for a passphrase, see https://superuser.com/a/1135950
echo "$GPG_OWNERTRUST" | base64 --decode | gpg --import-ownertrust

# Configure git credentials
git config --global user.email "action@github.com"
git config --global user.name "GitHub Action"

# Get release version & next dev version
git fetch --tags

echo "Determine the release version of the application from the previous tag and the provided release scope."
CURRENT_DEV_VERSION="$(mvn org.apache.maven.plugins:maven-help-plugin:3.2.0:evaluate -Dexpression=project.version -q -DforceStdout)"
RELEASE_VERSION="${CURRENT_DEV_VERSION//-SNAPSHOT/}"
echo "The release version is $RELEASE_VERSION"

NEXT_DEV_VERSION=$(echo "$RELEASE_VERSION" | awk '{split($1,a,"."); print a[1] "." a[2] "."  a[3]+1 "-SNAPSHOT"}')
echo "The next development version is $NEXT_DEV_VERSION"

echo "Preparing release $RELEASE_VERSION."
mvn -B -C release:prepare --settings ./settings.xml \
  -Darguments='-DsuppressionFiles=owasp-dependency-check-exclude.xml -DretireJsAnalyzerEnabled=false -DskipTests -DskipDepCheck' \
  -DpushChanges=false \
  -DautoVersionSubmodules \
  -DreleaseVersion="$RELEASE_VERSION" \
  -DdevelopmentVersion="$NEXT_DEV_VERSION" \
  -DscmCommentPrefix="Releasing $RELEASE_VERSION [maven-release-plugin]"

git push --tags remote-origin HEAD:master

echo "Performing release $RELEASE_VERSION."
mvn -B -C -Darguments='-DdeployAtEnd -Dmaven.javadoc.skip=true' release:perform --settings ./settings.xml
