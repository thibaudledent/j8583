# Simple message parser

[🏠 Documentation home](README.md)

The j8583 jar contains a small command-line program to parse ISO 8583 messages, using a
configuration file specified on the command line.

To use this program, set up your classpath to include SLF4J and j8583, then invoke the
`com.solab.iso8583.util.SimpleParser` class, passing it the full path (or URL) to your j8583
configuration file. For example:

```sh
java -cp lib/slf4j-api-2.0.18.jar:lib/slf4j-simple-2.0.18.jar:lib/j8583-1.26.2.jar \
    com.solab.iso8583.util.SimpleParser /tmp/j8583-config.xml
```

If you don't pass any argument, the program tries to configure the `MessageFactory` from a
default configuration file found on the classpath (see `ConfigParser.configureFromDefault()`).

You can then paste an ISO 8583 message encoded as text, without any ISO header, and the program
will parse it, showing the message type and, for each field present in the message, its number,
type, length and value.

---

← Previous: [Groovy and Scala compatibility](polyglot-compatibility.md) | 🏠 [Documentation home](README.md)
