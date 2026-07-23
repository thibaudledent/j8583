# Groovy and Scala compatibility

[🏠 Documentation home](README.md)

j8583 is a plain Java library, so it's compatible with any JVM language, including Groovy and
Scala. Beyond that, a few j8583 classes have extra methods specifically to give a nicer,
shorthand syntax in those languages.

## Groovy shorthand

In Java, you deal with values in an `IsoMessage` like this:

```java
message.setValue(32, "Some value", IsoType.LLVAR, 0);
IsoValue isoValue = message.getField(32);
String value = (String) message.getObjectValue(32);
Object value2 = message.getField(32).getValue();
```

In Groovy, you can do the same like this:

```groovy
message[32] = IsoType.LLVAR("Some value")
message[7] = IsoType.DATE10(new Date())
message[41] = IsoType.ALPHA("Term", 16)
message[11] = IsoType.NUMERIC(123, 6)
IsoValue isoValue = message[32]
def value = message[32]?.value
```

This works because `IsoMessage` has `getAt()` and `putAt()` methods that work like `getField()`
and `setField()`, and `IsoType` has two `call()` methods (with and without a length) that return
`IsoValue` instances of the proper type and length. The variant with a length parameter must be
used with `NUMERIC` and `ALPHA` types; the other types can use the variant without a length.

## Scala shorthand

Similarly, you can use a shorter notation in Scala to set and get fields:

```scala
message(32) = IsoType.LLVAR("Some value")
message(7) = IsoType.DATE10(new Date())
message(41) = IsoType.ALPHA("Term", 16)
message(11) = IsoType.NUMERIC(123, 6)
val v1: String = message(32).getValue
val v2: String = message.getObjectValue(32)
```

This works because `IsoMessage` also implements `apply()` and `update()` methods that behave like
`getField()` and `setField()`. `IsoType` also implements `apply()`, so you can create `IsoValue`s
directly from an `IsoType`.

## Builder-style chaining

In any language, you can chain calls in a builder-like style, since `setValue()` and `setField()`
return the message itself:

```java
message.setValue(4, amount, IsoType.AMOUNT, 12)
    .setValue(7, new Date(), IsoType.DATE10, 10)
    .setValue(11, trace, IsoType.NUMERIC, 6);
```

Of course, it can be more succinct in Groovy or Scala:

```groovy
message.setField(4, IsoType.AMOUNT(amount))
    .setField(7, IsoType.DATE10(new Date()))
    .setField(11, IsoType.NUMERIC(trace, 6))
```

---

← Previous: [Spring integration](spring-integration.md) | 🏠 [Documentation home](README.md) | Next: [Simple message parser](simple-parser.md) →
