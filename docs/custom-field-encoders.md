# Custom field encoders

Sometimes a field contains several sub-fields or separate pieces of data. j8583 will only parse
the field itself for you — you still have to parse those pieces of data yourself when reading a
message, and encode several pieces of data into a field yourself when creating one.

j8583 can help with this via custom field encoders. To use this feature, first implement the
`CustomField` interface. Let's say, for example, that you want to store a list of strings inside
an `LLLVAR` field. You need an encoder that transforms a list into a string and decodes a list
from a string:

```java
public class ListEncoder implements CustomField<List<String>> {

    // Transform a List of Strings into a String
    public String encodeField(List<String> value) {
        StringBuilder sb = new StringBuilder();
        for (String s : value) {
            sb.append(s).append(',');
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    // Transform a String into a List of Strings
    public List<String> decodeField(String value) {
        return Arrays.asList(value.split(","));
    }
}
```

Now that you have your field encoder, pass it to your `MessageFactory`, indicating the field where
you want to use it — for example, field 62. After this, when your message factory parses a
message, the resulting `IsoMessage` will contain a list of strings in field 62, and you can pass a
list directly into that field on new messages; it will be encoded into a string when writing the
message to a stream or creating the byte array with the ISO 8583 data.

```java
messageFactory.setCustomField(62, new ListEncoder());
IsoMessage parsed = messageFactory.parseMessage(someBytes, 0);
assert parsed.getObjectValue(62) instanceof List;

IsoMessage nm = messageFactory.newMessage(0x200);
// you need to pass the CustomField
nm.setValue(62, Arrays.asList("a", "b", "c", "d", "e"), messageFactory.getCustomField(62), IsoType.LLLVAR, 0);
// when nm gets written, field 62 will be "009a,b,c,d,e"
```

> **Note:** if your custom field only needs to handle binary messages, you can implement
> `CustomBinaryField` instead of `CustomField`. It adds `decodeBinaryField()` and
> `encodeBinaryField()` methods for working directly with byte arrays; in that case the
> `String`-based methods inherited from `CustomField` may simply return `null`.

If you set up `CustomField` encoders on a `MessageFactory` and then configure it using an
[XML configuration file](xml-configuration.md), the values for fields that have `CustomField`
encoders defined will be decoded with them. In this example, if you configure the
`MessageFactory` after setting up the `ListEncoder` for field 62, a message template containing a
value for field 62 will have that field decoded as a list of strings.

## Sharing custom objects across message templates

If you use a `CustomField` to handle a custom value object that you have in a message template,
you should make your custom object `Cloneable`, and then manually replace the value in each
message obtained from the factory with a new copy. Otherwise, every message the factory creates
from that template will hold a reference to *the same value object*, which causes trouble if the
object's values are modified — you'd effectively have several `IsoMessage`s sharing state in that
field. So you should do something like this:

```java
IsoMessage myIso = messageFactory.newMessage(0x200);
// Let's suppose we have some custom object with its CustomField encoder for field 126
// Our class will have a copy() method or something similar
MyCustomClass customField = ((MyCustomClass) myIso.getObjectValue(126)).copy();
// Now you can modify this instance
customField.setSomeValue("blabla");
myIso.setValue(126, customField, messageFactory.getCustomField(126), IsoType.LLLVAR, 0);
```

j8583 cannot perform this copy operation for you: even though the `MessageFactory` can detect if
your custom class implements `Cloneable`, it can't invoke the `clone()` method because it's
protected, and there's no guarantee that your implementation made it public.
