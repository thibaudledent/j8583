#!/usr/bin/env bash
set -eEuxo pipefail

git --no-pager log -n 1 --pretty=oneline

if git --no-pager log -n 1 --pretty=oneline | grep -qE "Merge .*after-release-[0-9]+\.[0-9]+\.[0-9]+"; then
  echo "This pull request is a follow-up of a release and will not trigger a new one."
  exit 0
fi

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

# Configure Github Hub credentials, see https://github.com/github/hub/issues/225#issuecomment-414618488
cat > ~/.config/hub << EOF
github.com:
- user: thibaudledent
  oauth_token: $RELEASE_TOKEN
  protocol: https
EOF

# Get release version & next dev version
git fetch --tags

echo "Determine the release version of the application from the previous tag and the provided release scope."
CURRENT_DEV_VERSION="$(mvn org.apache.maven.plugins:maven-help-plugin:3.2.0:evaluate -Dexpression=project.version -q -DforceStdout)"
RELEASE_VERSION="${CURRENT_DEV_VERSION//-SNAPSHOT/}"
echo "The release version is $RELEASE_VERSION"

NEXT_DEV_VERSION=$(echo "$RELEASE_VERSION" | awk '{split($1,a,"."); print a[1] "." a[2] "."  a[3]+1 "-SNAPSHOT"}')
echo "The next development version is $NEXT_DEV_VERSION"

# Create a new branch to avoid the error: GH006: Protected branch update failed for refs/heads/master (At least 1 approving review is required by reviewers with write access)
BRANCH_NAME=after-release-"$RELEASE_VERSION"-$RANDOM
git checkout -b "$BRANCH_NAME"

echo "Preparing release $RELEASE_VERSION."
mvn -B -C release:prepare --settings ./settings.xml \
  -DpushChanges=true \
  -DautoVersionSubmodules \
  -DreleaseVersion="$RELEASE_VERSION" \
  -DdevelopmentVersion="$NEXT_DEV_VERSION" \
  -DscmCommentPrefix="Releasing $RELEASE_VERSION [maven-release-plugin]"

# Update the versions in the readme
README_VERSION=$(grep -E "^\s{2}<version>([0-9]|.)+</version>" README.md | grep -oP "[0-9]+\.[0-9]+\.[0-9]+")
sed -i "s/$README_VERSION/$RELEASE_VERSION/g" README.md
git commit -am "Update the versions in the readme to $RELEASE_VERSION"

git push origin --tags
git push --set-upstream origin "$BRANCH_NAME"
hub pull-request -m "Update snapshot version after release-$RELEASE_VERSION"

echo "Performing release $RELEASE_VERSION."
mvn -B -C -Darguments='-DdeployAtEnd -DskipDepCheck -DskipRelease=false' release:perform --settings ./settings.xml
