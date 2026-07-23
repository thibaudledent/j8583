# XML configuration

The `MessageFactory` can read an XML file to set up message templates, ISO headers by type, and
parsing templates — the most cumbersome parts to configure programmatically.

There are three types of top-level elements you can specify in the config file: `header`,
`template`, and `parse`. All of them must be contained in a single `j8583-config` element. The
DTD is bundled with the library, so a config file should start with:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE j8583-config PUBLIC "-//J8583//DTD CONFIG 1.0//EN"
    "http://j8583.sourceforge.net/j8583.dtd">
<j8583-config>
    <!-- header, template and parse elements go here -->
</j8583-config>
```

## The `header` element

Specify a `header` element for every message type that needs an ISO header. Only one per message
type:

```xml
<header type="0200">ISO015000050</header>
<header type="0400">ISO015000050</header>
```

You can also define a header as a reference to another header:

```xml
<header type="0800" ref="0200" />
```

The header for `0800` messages will be the same as the header for `0200` messages.

## The `template` element

Each `template` element defines a message template, with the message type and the fields that
the template should include. Every new message of that type that the `MessageFactory` creates
will contain those same values, so this is very useful for defining fixed values that will be the
same in every message. Only one template per type.

```xml
<template type="0200">
    <field num="3" type="NUMERIC" length="6">650000</field>
    <field num="32" type="LLVAR">456</field>
    <field num="35" type="LLVAR">4591700012340000=</field>
    <field num="43" type="ALPHA" length="40">Fixed-width data</field>
    <field num="48" type="LLLVAR">Life, the Universe, and Everything|42</field>
    <field num="49" type="ALPHA" length="3">840</field>
    <field num="60" type="LLLVAR">B456PRO1+000</field>
    <field num="61" type="LLLVAR">This field can have a value up to 999 characters long.</field>
    <field num="100" type="LLVAR">999</field>
    <field num="102" type="LLVAR">ABCD</field>
</template>
```

A template can also extend another template, so that it includes all the fields from the
referenced template as well as any new fields defined in it, and can also exclude fields from the
referenced template:

```xml
<template type="0400" extends="0200">
    <field num="90" type="ALPHA" length="42">Bla bla</field>
    <field num="102" type="exclude" />
</template>
```

In the example above, the template for message type `0400` includes all fields defined in the
template for message type `0200` *except* field 102, and additionally includes field 90.

## The `parse` element

Each `parse` element defines a parsing template for a message type. It must include every field
that an incoming message can contain, each with its type and length (if needed). Only `ALPHA` and
`NUMERIC` types need to have a length specified — the other types either have a fixed length, or
have their length encoded as part of the field itself (`LLVAR`, `LLLVAR`, `LLLLVAR` and their
binary/BCD variants). See [What is ISO 8583?](iso8583-protocol.md#data-types) for the complete
list of supported types.

```xml
<parse type="0210">
    <field num="3" type="NUMERIC" length="6" />
    <field num="4" type="AMOUNT" />
    <field num="7" type="DATE10" />
    <field num="11" type="NUMERIC" length="6" />
    <field num="12" type="TIME" />
    <field num="13" type="DATE4" />
    <field num="15" type="DATE4" />
    <field num="17" type="DATE_EXP" />
    <field num="32" type="LLVAR" />
    <field num="35" type="LLVAR" />
    <field num="37" type="NUMERIC" length="12" />
    <field num="38" type="NUMERIC" length="6" />
    <field num="39" type="NUMERIC" length="2" />
    <field num="41" type="ALPHA" length="16" />
    <field num="43" type="ALPHA" length="40" />
    <field num="48" type="LLLVAR" />
    <field num="49" type="ALPHA" length="3" />
    <field num="60" type="LLLVAR" />
    <field num="61" type="LLLVAR" />
    <field num="70" type="ALPHA" length="3" />
    <field num="100" type="LLVAR" />
    <field num="102" type="LLVAR" />
    <field num="126" type="LLLVAR" />
</parse>
```

As with message templates, parsing guides can extend other parsing guides:

```xml
<parse type="0410" extends="0210">
    <field num="90" type="ALPHA" length="42" />
    <field num="102" type="exclude" />
</parse>
```

## Composite fields

`CompositeField` is a `CustomField` that acts as a container for several `IsoValue`s, and it can
be configured in the parsing guide of a message type:

```xml
<parse type="0410">
    <field num="125" type="LLLVAR">
        <field num="1" type="ALPHA" length="5" />
        <field num="2" type="LLVAR" />
        <field num="3" type="NUMERIC" length="6" />
        <field num="4" type="ALPHA" length="2" />
    </field>
</parse>
```

In the example above, when a message of type `0410` is parsed, the value for field 125 will be a
`CompositeField`, and you can obtain the sub-fields via `getField()` or `getObjectValue()`. The
`num` attribute of the sub-fields is ignored — it's only there to satisfy DTD validation.

This means you can do the following in code:

```java
// Assuming original data for field 125 is "018one  03two123456OK"
CompositeField f = message.getObjectValue(125);
String sub1 = f.getObjectValue(0); // "one  "
String sub2 = f.getObjectValue(1); // "two"
String sub3 = f.getObjectValue(2); // "123456"
String sub4 = f.getObjectValue(3); // "OK"
```

You can also create a `CompositeField`, store several sub-fields inside it, and store it in any
field of an `IsoMessage`, passing the same instance as the `CustomField`:

```java
CompositeField f = new CompositeField().addValue(new IsoValue<String>(IsoType.ALPHA, "one", 5))
    .addValue(new IsoValue<String>(IsoType.LLVAR, "two"))
    .addValue(new IsoValue<Long>(IsoType.NUMERIC, 123L, 6))
    .addValue(new IsoValue<String>(IsoType.ALPHA, "OK", 2));
message.setValue(125, f, f, IsoType.LLLVAR, 0);
```

When the message is encoded, field 125 will be `018one  03two000123OK`.
