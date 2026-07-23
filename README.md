# j8583

![Build Status](https://github.com/thibaudledent/j8583/actions/workflows/build.yml/badge.svg) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=thibaudledent_j85832&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=thibaudledent_j85832) [![javadoc](https://javadoc.io/badge2/io.github.thibaudledent.j8583/j8583/javadoc.svg)](https://javadoc.io/doc/io.github.thibaudledent.j8583/j8583)

j8583 is a Java implementation of the ISO8583 protocol.

This repository is a **fork** of [https://bitbucket.org/chochos/j8583](https://bitbucket.org/chochos/j8583).

## Download

[![Maven Central](https://img.shields.io/maven-central/v/io.github.thibaudledent.j8583/j8583.svg?label=Maven%20Central)](https://central.sonatype.com/search?smo=true&namespace=io.github.thibaudledent.j8583&name=j8583)

The Javadoc is published on [javadoc.io](https://javadoc.io/doc/io.github.thibaudledent.j8583/j8583).

Maven:
```xml
<dependency>
  <groupId>io.github.thibaudledent.j8583</groupId>
  <artifactId>j8583</artifactId>
  <version>1.26.2</version>
</dependency>
```

Gradle:
```gradle
dependencies {
  implementation 'io.github.thibaudledent.j8583:j8583:1.26.2'
}
```

## Quick Start

j8583 is a codec: it builds and parses ISO8583 byte messages, but does not itself send or receive them over
a network. The `MessageFactory` is the main entry point; it's usually configured once (often from an XML
file) and then used to build outgoing messages and parse incoming ones.

### 1. Configure a `MessageFactory` from XML

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE j8583-config PUBLIC "-//J8583//DTD CONFIG 1.0//EN" "http://j8583.sourceforge.net/j8583.dtd">
<j8583-config>
    <header type="0200">ISO015000050</header>
    <header type="0210">ISO015000055</header>

    <template type="0200">
        <field num="3" type="NUMERIC" length="6">000000</field>
        <field num="32" type="LLVAR">456</field>
    </template>

    <parse type="0210">
        <field num="3" type="NUMERIC" length="6" />
        <field num="4" type="AMOUNT" />
        <field num="7" type="DATE10" />
        <field num="11" type="NUMERIC" length="6" />
        <field num="32" type="LLVAR" />
        <field num="39" type="NUMERIC" length="2" />
    </parse>
</j8583-config>
```

Save this as `j8583.xml` on the classpath. `header` elements define the string each message type is
prefixed with, `template` elements define default field values for newly created messages, and `parse`
elements tell the factory what fields to expect (and their type/length) when parsing incoming messages of
that type. See the full [XML configuration guide](src/site/apt/xmlconf.apt) for template/parsing-guide
inheritance and composite fields.

### 2. Build and write a message

```java
MessageFactory<IsoMessage> mf = ConfigParser.createDefault(); // reads j8583.xml from the classpath
mf.setAssignDate(true); // auto-sets field 7 (date) on every new message
mf.setTraceNumberGenerator(new SimpleTraceGenerator(1)); // auto-sets field 11 (trace number)

IsoMessage request = mf.newMessage(0x200);
request.setValue(4, new BigDecimal("12.34"), IsoType.AMOUNT, 0);

byte[] bytes = request.writeData(); // or request.write(outputStream, 2) to include a 2-byte length header
```

### 3. Parse a received message

```java
byte[] received = ...; // read from your own transport; j8583 has no networking of its own
IsoMessage response = mf.parseMessage(received, mf.getIsoHeader(0x210).length());
BigDecimal amount = response.getObjectValue(4);
```

For templates, custom field encoders, composite fields and the `TraceNumberGenerator`, see the
[usage guide](src/site/apt/guide.apt).

## How to release

A release is automatically triggered after each merge to the main branch. Your new version will appear after some time in [repo1.maven.org/.../j8583/](https://repo1.maven.org/maven2/io/github/thibaudledent/j8583/j8583/) (and a bit later in: [search.maven.org/artifact/.../j8583](https://search.maven.org/artifact/io.github.thibaudledent.j8583/j8583)).

More info about the release [here](https://github.com/thibaudledent/j8583/blob/main/RELEASE.md).

## How to contribute

To contribute to this repository:
* fork this repository
* do your changes and propose a merge request from your fork to this repository

More info: ["GitHub Standard Fork & Pull Request Workflow"](https://gist.github.com/Chaser324/ce0505fbed06b947d962)
