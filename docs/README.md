# j8583 documentation

[← Back to main README](../README.md)

j8583 is a Java library to generate and read ISO 8583 messages. It does not handle sending or
receiving them over a network connection — that part is up to you — but it parses the data you
have read and generates the data you need to write, either as a byte array, a `ByteBuffer`, or
directly to an `OutputStream`.

For the full API reference, see the [Javadoc on javadoc.io](https://javadoc.io/doc/io.github.thibaudledent.j8583/j8583).

## Guides

- [What is ISO 8583?](iso8583-protocol.md) — a short introduction to the protocol itself: message
  structure, data types, common fields and message types, and the differences between ASCII and
  binary encoding.
- [Usage guide](usage-guide.md) — the `MessageFactory` and `IsoMessage` classes, and how to
  configure headers, templates, parsing templates, trace number generation, custom field
  encoders, and masking sensitive fields when logging.
- [XML configuration](xml-configuration.md) — configuring a `MessageFactory` from an XML file
  instead of programmatically: headers, templates, parsing guides, template inheritance, and
  composite fields.
- [Custom field encoders](custom-field-encoders.md) — encoding and decoding structured values
  (lists, custom objects, sub-fields) stored inside a single ISO field.
- [Spring integration](spring-integration.md) — configuring a `MessageFactory` bean in a Spring
  `ApplicationContext`.
- [Groovy and Scala compatibility](polyglot-compatibility.md) — shorthand syntax available when
  using j8583 from Groovy or Scala.
- [Simple message parser](simple-parser.md) — a small command-line tool bundled with the library
  for parsing ISO 8583 messages read from standard input.

## How j8583 works

j8583 offers a `MessageFactory`, which once properly configured, can create different message
types with some values predefined, and can also parse a byte array to create an ISO message.
Messages are represented by `IsoMessage` objects, which store `IsoValue` instances for their data
fields. You can work with the `IsoValue`s directly, or use the convenience methods of `IsoMessage`
to work with the stored values.

j8583 can directly handle the basic data types: numeric values, strings, amounts, dates and byte
arrays. If you want to encode a custom object in a field, you can implement a
[custom field encoder](custom-field-encoders.md).
