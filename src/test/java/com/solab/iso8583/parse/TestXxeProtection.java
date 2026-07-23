package com.solab.iso8583.parse;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.time.Duration;

/**
 * Verifies that ConfigParser does not resolve external entities or external DTDs
 * declared in a configuration XML document (XXE, CWE-611).
 */
class TestXxeProtection {

    /** An external general entity pointing at a local file must never be resolved
     * into a template field's value, even though the entity is legitimately declared
     * in the DOCTYPE internal subset. */
    @Test
    void testExternalGeneralEntityIsNotResolved() throws IOException {
        final File canary = File.createTempFile("j8583-xxe-canary", ".txt");
        canary.deleteOnExit();
        Files.writeString(canary.toPath(), "SECRET-XXE-CANARY-VALUE");

        final String xml = "<?xml version=\"1.0\"?>\n"
                + "<!DOCTYPE j8583-config [ <!ENTITY xxe SYSTEM \"" + canary.toURI() + "\"> ]>\n"
                + "<j8583-config>\n"
                + "<template type=\"0200\"><field num=\"3\" type=\"ALPHA\" length=\"30\">&xxe;</field></template>\n"
                + "</j8583-config>";

        final MessageFactory<IsoMessage> mf = ConfigParser.createFromReader(new StringReader(xml));
        final IsoMessage m = mf.newMessage(0x200);
        final String value = m.getObjectValue(3);
        Assertions.assertFalse(value != null && value.contains("SECRET-XXE-CANARY-VALUE"),
                "the external general entity must not have been resolved into the field value");
    }

    /** A DOCTYPE pointing at an external DTD other than j8583's own must not trigger a
     * fetch: the custom EntityResolver must never fall back to default resolution, and
     * parsing should still succeed using the rest of the document. */
    @Test
    void testExternalDtdIsNeverFetched() {
        final String xml = "<?xml version=\"1.0\"?>\n"
                + "<!DOCTYPE j8583-config SYSTEM \"http://192.0.2.1/unreachable-should-not-be-fetched.dtd\">\n"
                + "<j8583-config>\n"
                + "<header type=\"0200\">ISO0150</header>\n"
                + "</j8583-config>";

        final MessageFactory<IsoMessage>[] mf = new MessageFactory[1];
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(5), () ->
                mf[0] = ConfigParser.createFromReader(new StringReader(xml)),
                "parsing must not attempt to fetch the external DTD over the network");
        Assertions.assertEquals("ISO0150", mf[0].getIsoHeader(0x200));
    }
}
