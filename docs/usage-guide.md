# Usage guide

This guide explains how to set up j8583. If you want to know more about the ISO 8583 protocol
itself, read [What is ISO 8583?](iso8583-protocol.md) first.

## The MessageFactory and IsoMessage classes

These are the two main classes you need to use to work with ISO 8583 messages. An `IsoMessage`
can be encoded into a byte array or a `ByteBuffer` to be written to an `OutputStream`, or it can
directly write itself to an `OutputStream`. You can set and get the values for each field in an
`IsoMessage`, and it will adjust itself to use a secondary (and, if needed, tertiary) bitmap
automatically. An `IsoMessage` has settings to encode itself in binary or ASCII, to use a
secondary bitmap even if it's not necessary, and it can have its own ISO header.

However, it can be cumbersome to programmatically create `IsoMessage`s all the time. The
`MessageFactory` is a big aid in creating `IsoMessage`s with predefined values; it can also set
the date and the trace number on each new message.

## A quick example

Before going through each configuration option in detail, here's a minimal example that ties the
concepts above together: a `MessageFactory` with a message template and a parsing template, used
to create a message and parse it back.

```java
MessageFactory<IsoMessage> mf = new MessageFactory<>();

// A template: values shared by every 0200 message the factory creates
IsoMessage template = new IsoMessage();
template.setType(0x200);
template.setValue(3, 0, IsoType.NUMERIC, 6);
mf.addMessageTemplate(template);

// A parsing template: tells the factory how to read an incoming 0200 message
Map<Integer, FieldParseInfo> parseGuide = new HashMap<>();
parseGuide.put(3, FieldParseInfo.getInstance(IsoType.NUMERIC, 6, mf.getCharacterEncoding()));
parseGuide.put(41, FieldParseInfo.getInstance(IsoType.ALPHA, 8, mf.getCharacterEncoding()));
mf.setParseMap(0x200, parseGuide);

IsoMessage message = mf.newMessage(0x200);
message.setValue(41, "TERM0001", IsoType.ALPHA, 8);
byte[] data = message.writeData();

IsoMessage parsed = mf.parseMessage(data, 0);
// parsed now has field 3 ("000000", copied from the template) and field 41 ("TERM0001")
```

`IsoMessage` can also write itself directly to an `OutputStream` (with `write()`) or to a
`ByteBuffer` (with `writeToBuffer()`); both can prepend a length header, useful when framing
messages sent over a socket — see [message terminator](iso8583-protocol.md#message-terminator) for
more on that. Setting up templates and parsing guides by hand like this works, but for anything
beyond a handful of fields it's usually easier to use an [XML configuration
file](xml-configuration.md) instead, as described below.

## Configuring the MessageFactory

There are six main things you can configure in a `MessageFactory`: ISO headers, message
templates, parsing templates, a `TraceNumberGenerator`, custom field encoders, and which fields to
mask when logging messages.

### ISO headers

ISO headers are strings that are associated with a message type. Whenever you ask the message
factory to create an `IsoMessage`, it will pass the corresponding ISO header (if present) to the
new message.

### Message templates

A message template is an `IsoMessage` itself; the `MessageFactory` can have a template for each
message type it needs to create. When it creates a message and it has a template for that message
type, it copies the fields from the template to the new message before returning it.

### Parsing templates

A parsing template is a map containing `FieldParseInfo` objects as values and the field numbers as
the keys. A `FieldParseInfo` object contains an `IsoType` and an optional length; with this
information and the field number, the `MessageFactory` can parse incoming messages, first
analyzing the message type and then using the parsing template for that type. When parsing a
message, the `MessageFactory` only parses the fields that are specified in the message's bitmap.
For example, if the bitmap specifies field 4, the factory will get the `FieldParseInfo` stored in
the map under key 4, and will attempt to parse the field according to the type and length
specified by that `FieldParseInfo`.

A message does not need to contain all the fields specified in a parsing template, but a parsing
template must contain all the fields specified in the bitmap of a message, or the
`MessageFactory` won't be able to parse it — it has no way of knowing how it should parse that
field (and consequently all subsequent fields).

### The TraceNumberGenerator

When creating new messages, they usually need a unique trace number, contained in field 11. They
also usually need the date they were created (or the date the transaction was originated) in
field 7. The `MessageFactory` can automatically set the current date on all new messages — you
just need to set the `assignDate` property to `true`. It can also assign a new trace number to
each message it creates, but for this it needs a `TraceNumberGenerator`.

The `TraceNumberGenerator` interface defines a `nextTrace()` method, which must return a new trace
number between 1 and 999999. It needs to be cyclic, so it returns 1 again after returning 999999.
And usually, it needs to be thread-safe.

j8583 only defines the interface; in production environments you will usually need to implement
your own `TraceNumberGenerator`, getting the new trace number from a sequence in a database or
some similar mechanism. As an example, the library includes `SimpleTraceGenerator`, which simply
increments an in-memory value.

### Custom field encoders

Certain implementations of ISO 8583 specify fields which contain many sub-fields. If you only
handle strings in those fields, you'll have to parse those pieces of data yourself when reading a
message, and encode several pieces of data into a field yourself when creating one.

In these cases you can implement a `CustomField`, which is an interface that defines two methods:
one for encoding an object into a `String`, another for decoding an object from a `String`. You
pass the `MessageFactory` a `CustomField` for every field where you want to store custom values,
so that parsed messages will return the objects decoded by the `CustomField`s instead of just
strings; and when you set a value in an `IsoMessage`, you can specify the `CustomField` to be used
to encode the value as a `String`. See [Custom field encoders](custom-field-encoders.md) for more.

### Masking sensitive fields when logging

`IsoMessage` has a `debugString()` method that returns a text representation of the message,
useful for logging, without needing to encode it first. By default it shows every field in clear
text, which is a problem for real ISO 8583 traffic: fields like the PAN, track data or PIN block
are cardholder data that shouldn't end up in application logs.

Tell the `MessageFactory` which fields to mask, and every message it creates or parses will pick
up the setting automatically:

```java
messageFactory.setSensitiveFields(IsoMessage.COMMONLY_SENSITIVE_FIELDS);
IsoMessage m = messageFactory.parseMessage(someBytes, 0);
log.debug(m.debugString());
```

`IsoMessage.COMMONLY_SENSITIVE_FIELDS` (fields 2, 34, 35, 36, 45, 52 and 55) is a reasonable
starting point, but you should review it against the fields your own provider actually uses for
cardholder data. Masking replaces a field's value with `*` characters of the same length; the
length header of variable-length fields still shows the real length. You can also skip the
factory-wide setting and call `debugString(Set<Integer>)` directly on a message with an explicit
set of fields to mask.

`MessageFactory` also has an `unsafeNonPciDssCompliantRawMessageLoggingEnabled` flag (disabled by
default) that logs the raw, hex-encoded message buffer at `ERROR` level when a message can't be
parsed because its type has no parsing guide. As the name says, this is unsafe and not PCI DSS
compliant — only enable it temporarily while developing or debugging a new parsing guide, never in
production or against real cardholder data.

## XML configuration

The easiest way to configure message templates and parsing templates is by using an
[XML config file](xml-configuration.md) and passing it to the `MessageFactory`.
