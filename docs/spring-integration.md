# Spring integration

[🏠 Documentation home](README.md)

You can configure a `MessageFactory` as a bean in a Spring `ApplicationContext`, and inject it
into any other components that need to create or parse `IsoMessage`s. j8583 has no dependency on
Spring itself — `MessageFactory` is a plain Java bean, so it works with Spring's standard XML or
Java configuration out of the box.

## Setting up the MessageFactory

The properties you'll typically want to set up in the Spring config are:

| Property | Description |
| --- | --- |
| `configPath` | Path to the [XML configuration file](xml-configuration.md) with the message and parsing templates. |
| `customFields` | A map with the `CustomField` implementations that should be used for each field (the keys are the field numbers). |
| `characterEncoding` | Character encoding to use for parsing `ALPHA`, `LLVAR` and `LLLVAR` fields, when it's different from the default platform encoding. |
| `assignDate` | Set to `true` to have the `MessageFactory` set field 7 to a `DATE10` with the date the message was created. |
| `etx` | Numeric value of the message terminator, or `-1` if you don't want to use one. |
| `forceSecondaryBitmap` | If `true`, messages include the secondary bitmap even if they don't contain any fields above 64. Some providers require this. |
| `forceStringEncoding` | By default, text messages are encoded/decoded using the byte array directly, for performance, but this can cause problems with an encoding other than the default, or if a field contains non-ASCII characters. Set this flag to force proper string encoding/decoding with the configured character encoding. |
| `ignoreLastMissingField` | If `true`, the factory only logs a warning when parsing incomplete messages, instead of throwing a `ParseException`. |
| `traceNumberGenerator` | An implementation of `TraceNumberGenerator` so the factory sets a new trace value in field 11 on new messages. |
| `useBinaryBitmap` | Makes newly created messages encode their bitmap in binary format, even if the rest of the message is encoded in text. Only affects text messages. |
| `useTertiaryBitmap` | If `true`, field 65 is interpreted as a tertiary bitmap, allowing messages to use fields 129-192. |
| `variableLengthFieldsInHex` | If `true`, the length header of variable-length fields (`LLVAR`, `LLLVAR`, etc.) is encoded/decoded in hexadecimal instead of decimal. |
| `binaryHeader` | If `true`, the message header portion is written/parsed as binary. Default `false`. |
| `binaryFields` | If `true`, the message fields portion is written/parsed as binary. Default `false`. |
| `useBinaryMessages` | **Deprecated**, use `binaryHeader` and `binaryFields` instead. Setting it sets both `binaryHeader` and `binaryFields` to the same value. |
| `sensitiveFields` | A set of field numbers to mask with `*` when calling `debugString()` on messages this factory creates or parses. See [Masking sensitive fields when logging](usage-guide.md#masking-sensitive-fields-when-logging). |
| `unsafeNonPciDssCompliantRawMessageLoggingEnabled` | **Unsafe, not PCI DSS compliant.** If `true`, logs the raw, hex-encoded message buffer at `ERROR` level when a message can't be parsed because its type has no parsing guide. Default `false`; only meant for temporary use while developing or debugging. |

Example:

```xml
<bean id="isoMessageFactory" class="com.solab.iso8583.MessageFactory">
  <property name="assignDate" value="true" />
  <property name="etx" value="3" />
  <property name="ignoreLastMissingField" value="true" />
  <property name="customFields"><map>
    <entry key="48"><bean class="your.app.DataProductEncoder" /></entry>
    <entry key="62"><bean class="your.app.AdditionalDataEncoder" /></entry>
  </map></property>
  <!-- It is important to set this AFTER the custom fields
       if your message templates contain some of those fields -->
  <property name="configPath" value="/your-j8583.xml" />
</bean>
```

## Properties that can only be set programmatically

Some properties can't be expressed as simple Spring bean properties and need to be set in code
after retrieving the bean (or in a factory method / `@PostConstruct`):

| Property | Description |
| --- | --- |
| `timezoneForParseGuide` | Sets the timezone for a specific field of a specific message type. Useful if you need to encode/decode dates with a timezone other than the local one. |
| `customField` | Sets a single `CustomField` encoder for one field. |
| `isoHeader` | Sets the ISO header to use for a specific message type. |

---

← Previous: [Custom field encoders](custom-field-encoders.md) | 🏠 [Documentation home](README.md) | Next: [Groovy and Scala compatibility](polyglot-compatibility.md) →
