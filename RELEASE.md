# About the release

A release is automatically triggered after each merge to the master branch.

## Manual release

To force a release a new version of the library to Maven Central:

1) Increment the [version](https://github.com/thibaudledent/j8583/blob/master/pom.xml#L7) of the library in the main `pom.xml`  and create a Pull Request

2) Merge the Pull Request to the master branch

3) Run the [manual-release](https://github.com/thibaudledent/j8583/actions/workflows/manual-release.yaml) workflow (see also [Manually running a workflow](https://docs.github.com/en/actions/managing-workflow-runs/manually-running-a-workflow))

4) Once the pipeline is green, your new version will appear after some time in [repo1.maven.org/.../j8583/](https://repo1.maven.org/maven2/io/github/thibaudledent/j8583/j8583/) (and a bit later in: [search.maven.org/artifact/.../j8583](https://search.maven.org/artifact/io.github.thibaudledent.j8583/j8583))

## Secrets Repository Variables

See [Secret Actions](https://github.com/thibaudledent/j8583/settings/secrets/actions) in the settings of the repository. The following variables should be set:

* `GPG_PASSPHRASE`: secret gpg key passphrase
* `GPG_KEYNAME`: gpg key keyname
* `OSSRH_USER_TOKEN`: the `XXX` part of `<username>XXX</username>` from https://s01.oss.sonatype.org/#profile;User%20Token
* `OSSRH_PWD_TOKEN`:  the `XXX` part of `<password>XXX</password>` from https://s01.oss.sonatype.org/#profile;User%20Token
* `GPG_OWNERTRUST`: `gpg --export-ownertrust | base64 | tr -d '\n'`
* `GPG_SECRET_KEY`: `gpg -a --export-secret-keys thibaudledent@gmail.com | base64 | tr -d '\n'`
* `RELEASE_TOKEN`: a Personal Access Token to create a pull request when performing the release (see https://github.com/settings/tokens)
* `NVD_API_KEY`: NVD API Key from https://nvd.nist.gov/developers/request-an-api-key for OWASP vulnerability check

## Distribute the key

List of available servers: https://www.duinsoft.nl/keyservers.php.

To distribute the key `gpg --keyserver pgp.surfnet.nl --send-keys DB85FB2159287141`.

Then it should be available here: https://pgp.surfnet.nl/pks/lookup?search=0xDB85FB2159287141&fingerprint=on&op=index.

## Key expiration

The `mvn deploy` might fail on:

```
gpg: signing failed: No secret key
gpg: no default secret key: No secret key
```

It is possible that the reason for this error is simply that the gpg key has expired.

## More info about automating a Maven release

Article *"Creating a Bitbucket pipeline to automate a Maven release"*, see this [link](https://thibaudledent.github.io/2019/03/01/bitbucket-pipeline/).
