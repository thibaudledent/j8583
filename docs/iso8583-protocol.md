# What is ISO 8583?

ISO 8583 is a message format used for credit card transactions, banking and other commercial
interactions between different systems. It has an ASCII variant and a binary one, and it is
somewhat convoluted and difficult to implement.

The full specification is available at [iso.org](https://www.iso.org/standard/31628.html) and has
gotten really complicated over its different versions; this page is just an introduction to the
generalities of the protocol. Wikipedia also has a
[good article](https://en.wikipedia.org/wiki/ISO_8583) covering the whole spec and its variants.

## Message structure

The general format of an ISO 8583 message looks like this:

```
ISO header (optional) | Message Type | primary bitmap | secondary bitmap (optional) | data fields
```

- The **ISO header** is a string containing some code that can vary according to the message type.
- The **message type** is a number expressed as 4 hex digits (or 2 bytes when using binary
  format).
- The **bitmap** is 64 bits long, encoded as 16 hex characters in text mode or as 8 bytes in
  binary mode. Every bit that is set in the bitmap indicates that the corresponding field is
  present. If the first bit is set, field 1 is present, and so on.
- The **fields** in the message are numbered from 1 to 64. Field 1 is the secondary bitmap, if
  present, which extends the range to fields 65-128. j8583 also supports a tertiary bitmap in
  field 65, extending the range further to fields 129-192.

## Data types

ISO 8583 specifies a lot of data types; the most common ones are implemented directly in j8583
through the `IsoType` enum. Here is a table of the most common ISO types and their j8583
counterpart:

| ISO type | j8583 type | Description |
| --- | --- | --- |
| Fixed-length numeric | `NUMERIC` | Fixed-width numeric value, padded with zeroes to the left. |
| Fixed-length alphanumeric | `ALPHA` | Fixed-width alphanumeric value, padded with spaces to the right. |
| Date | `DATE4` | Date in format `MMdd`, fixed width of 4. |
| Date | `DATE6` | Date in format `yyMMdd`, fixed width of 6. |
| Date | `DATE10` | Date in format `MMddHHmmss`, fixed width of 10. |
| Date | `DATE12` | Date in format `yyMMddHHmmss`, fixed width of 12. |
| Date | `DATE14` | Date in format `yyyyMMddHHmmss`, fixed width of 14. |
| Expiration date | `DATE_EXP` | Date in format `yyMM`, fixed width of 4. Used for credit card expiration dates. |
| Time | `TIME` | Time of day in format `HHmmss`, fixed width of 6. |
| Amount | `AMOUNT` | Currency amount, a positive number expressed in cents, with a fixed width of 12. For example, one dollar is encoded as `000000000100`. |
| Variable-length alphanumeric | `LLVAR` | Up to 99 characters long. The length is encoded in the first 2 characters of the value, e.g. `"HEY"` is encoded as `03HEY`. |
| Variable-length alphanumeric | `LLLVAR` | Up to 999 characters long. The length is encoded in the first 3 characters of the value, e.g. `"HEY"` is encoded as `003HEY`. |
| Variable-length alphanumeric | `LLLLVAR` | Up to 9999 characters long. The length is encoded in the first 4 characters of the value, e.g. `"HEY"` is encoded as `0003HEY`. |
| Fixed-length binary | `BINARY` | Similar to `ALPHA`, but stores byte arrays directly instead of text. |
| Fixed-length binary | `RAW_BINARY` | Same as `BINARY`, but always treated as a binary field even when the rest of the message is encoded as text. |
| Variable-length binary | `LLBIN` | Similar to `LLVAR`, but stores byte arrays directly instead of text. |
| Variable-length binary | `LLLBIN` | Similar to `LLLVAR`, but stores byte arrays directly instead of text. |
| Variable-length binary | `LLLLBIN` | Similar to `LLLLVAR`, but stores byte arrays directly instead of text. |

This table lists the most commonly used types. j8583 also supports several variants used by
specific providers, such as BCD-encoded lengths (`LLBCDBIN`, `LLLBCDBIN`, `LLLLBCDBIN`,
`LLBCDLENGTHALPHANUM`) and fields whose length is a single literal byte (`LLBINLENGTHNUM`,
`LLBINLENGTHALPHANUM`, `LLBINLENGTHBIN`, and their 2-byte-length counterparts, `LLLLBINLENGTHNUM`,
`LLLLBINLENGTHALPHANUM` and `LLLLBINLENGTHBIN`). See the `IsoType` enum in the
[Javadoc](https://javadoc.io/doc/io.github.thibaudledent.j8583/j8583) for the complete,
up-to-date list.

## Common scenarios

ISO 8583 implementations can vary a lot, depending on the provider and the type of products and
transactions it is used for, but certain fields are almost always used in a very similar manner.

> **Disclaimer:** the fields and message types described below reflect common practice rather than
> a strict part of the standard — experience here has been mostly with mobile carrier providers.

### Message types

These are some of the most common message types. There is some logic to this if you look at the
message types as 2-byte hex values: the first byte states the type of operation (`02` payments,
`04` reversals, `08` tests); the second byte indicates if it's a request or a response. Repeated
requests sometimes end in `1` instead of `0` — for example, reversals are `0400` the first time
but `0401` the next time you send them.

| Type | Meaning |
| --- | --- |
| `0200` | A payment or sale request. |
| `0210` | A payment or sale response. |
| `0400` | A reversal request (to undo a previous `0200` operation). |
| `0410` | A reversal response. |
| `0600` | A query (to check the status of a previous operation, an account's balance, etc). |
| `0610` | A query response. |
| `0800` | An echo request (to keep the connection alive and make sure the other side is responsive). |
| `0810` | An echo response. |

Some implementations require you to send `0400` for a reversal when you only have the request (in
case of a timeout), and `0420` when you did get a response (in case of some other error in your
system).

### Common fields

| Field | Meaning |
| --- | --- |
| 3 | Operation code, `NUMERIC` of length 6. |
| 4 | Amount, `AMOUNT`. |
| 7 | Date, `DATE10`. |
| 11 | Trace, `NUMERIC` of length 6. |
| 17 | Transaction date, `DATE4`. |
| 37 | Reference number, `NUMERIC` of length 12. |
| 38 | Confirmation number, `NUMERIC` of length 6. |
| 39 | Response code, `NUMERIC` of length 2. |
| 41 | Terminal ID, `ALPHA` of length 8 or 16. |
| 49 | Currency, `NUMERIC` of length 3. See the [ISO 4217](https://en.wikipedia.org/wiki/ISO_4217) table of currencies. |
| 128 | MAC (Message Authentication Code). Usually `ALPHA` or `NUMERIC` of length 16. |

## Asynchronous transmission and reception

When communicating between two systems with ISO 8583, the communication is usually asynchronous,
with all messages handled through a single connection. Between a terminal and a bigger system,
the communication will typically be synchronous.

In asynchronous communications, the client can send any number of requests to the server, and the
server may respond to those requests in a different order than they were sent. This is why the
trace number (field 11) is very important and should not be reused: if the client sends requests
with traces 123000, 123001 and 123002, the server may send back first 123001, then 123002, then
123000. The client knows which response corresponds to which request by checking the trace of the
responses.

Messages can vary in length depending on the message type and the fields it contains. The
protocol was designed so that a message can be read in parts: first the message type, then the
primary bitmap, and from there the rest of the fields (secondary bitmap included) can be read and
processed one by one. This is convenient for small devices like POS terminals, but is not very
efficient in larger systems with a high transaction volume. In those cases, the sender first
encodes the whole message, measures it, and sends the length as a 2- or 4-byte unsigned integer
(most significant byte first). This way the receiver knows to read 2 or 4 bytes, interpret them as
an unsigned integer, and then read that many bytes to get a complete ISO message.

Using 2 bytes allows for messages of up to 65535 bytes; 4 bytes allows for over 4 billion bytes.
A message encoded in ASCII with 127 `LLLVAR` fields each holding 999 characters can already be
around 127290 bytes long, which is over the 2-byte limit; and since j8583 also supports
`LLLLVAR`/`LLLLBIN` fields (up to 9999 characters each), providers that use those can produce
messages well over a million bytes. Because of this, a 2-byte length header is only safe if you
know your provider's messages will stay under 65535 bytes — otherwise a 4-byte length header is
the safer choice.

### Message terminator

The length header sent before a message is very useful for separating the reading operation from
the parsing of the message: you can dedicate a thread to read from a socket, and put those
buffers on a queue where another thread parses them. However, a length header only says how many
bytes to read, and the reader must read all of them — but what if the message is invalid because
it's actually *longer* than the length says? To avoid this problem, some systems use a message
terminator: a character that must be found at the end of every message. If messages are encoded
as text, a common terminator is ASCII `0x03`. Whether the terminator is counted as part of the
message length depends on the implementation. You can specify the terminator via the `etx`
property of an `IsoMessage`, or preferably on the `MessageFactory` so that all new messages
already have the property set. To disable the terminator, set `etx` to `-1`.

### Binary encoding

Many ISO 8583 implementations use plain text for the messages (even if the length header is
binary). But some implementations encode the messages as binary. The main differences are that
the message type is only 2 bytes long instead of 4 (the ASCII version is a hex representation of
the message type), the bitmap is not hex-encoded so it's only 8 bytes long, and all numeric values
(including date/time fields) are encoded using
[BCD](https://en.wikipedia.org/wiki/Binary-coded_decimal) — including the length headers for
`LLVAR` and `LLLVAR` fields.

You can encode messages in binary by setting the `binaryHeader`/`binaryFields` properties on an
`IsoMessage`, or on the `MessageFactory` directly so they get passed to all new messages. They
must be set on the `MessageFactory` if you want it to parse binary messages.
