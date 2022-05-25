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

echo "Github configuration"
[ -z "$RELEASE_TOKEN" ] && echo "RELEASE_TOKEN not set"
echo "$GITHUB_REPOSITORY"
echo "$GITHUB_REF"

git remote set-url origin https://x-access-token:"$RELEASE_TOKEN"@github.com/"$GITHUB_REPOSITORY" # workaround to be able to push (master branch) see https://stackoverflow.com/a/58393457/9321274 and https://www.paulmowat.co.uk/blog/resolve-github-action-gh006-protected-branch-update-failed
git checkout "${GITHUB_REF:11}"
git branch | grep .

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
  -DpushChanges=true \
  -DautoVersionSubmodules \
  -DreleaseVersion="$RELEASE_VERSION" \
  -DdevelopmentVersion="$NEXT_DEV_VERSION" \
  -DscmCommentPrefix="Releasing $RELEASE_VERSION [maven-release-plugin]"

git push origin --tags

echo "Performing release $RELEASE_VERSION."
mvn -B -C -Darguments='-DdeployAtEnd -DskipDepCheck -Dmaven.javadoc.skip=true' release:perform --settings ./settings.xml
