package com.solab.iso8583;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * Extended test for LLLLVar field
 *
 * @author Peter Margetiak
 */
class TestLlllPatch {

    private MessageFactory<IsoMessage> mfact = new MessageFactory<>();

    private static List<Integer> lengths() {
        return Arrays.asList(5, 50, 500, 5000);
    }

    @BeforeEach
    void setup() throws IOException {
        mfact.setConfigPath("issue50.xml");
        mfact.setAssignDate(false);
    }

    @ParameterizedTest()
    @MethodSource("lengths")
    void testParsingLength(final int fieldLength) throws Exception {
        // prepare
        String llllvar = makeLLLLVar(fieldLength);
        StringBuilder sb = new StringBuilder();
        sb.append("01004000000000000000")
                .append(String.format("%04d", fieldLength)).append(llllvar);

        // parse
        IsoMessage m = mfact.parseMessage(sb.toString().getBytes(), 0);
        Assertions.assertNotNull(m);
        String f2 = m.getObjectValue(2);
        Assertions.assertEquals(llllvar, f2);
        Assertions.assertEquals(fieldLength, f2.length());
        //Encode
        m = mfact.newMessage(0x100);
        m.setIsoHeader(null);
        m.setValue(2, llllvar, IsoType.LLLLVAR, 0);
        Assertions.assertEquals(sb.toString(), m.debugString());
    }

    @Test
    void testSerialiseParseSmall() throws Exception {
        testSerialiseParse(88);
    }

    @Test
    void testSerialiseParseMedium() throws Exception {
        testSerialiseParse(258);
    }

    @Test
    void testSerialiseParseLarge() throws Exception {
        testSerialiseParse(9919);
    }

    private void testSerialiseParse(final int length) throws Exception {
        // prepare
        String LLLLVar = makeLLLLVar(length);
        IsoMessage m = mfact.newMessage(0x100);
        m.setValue(2, LLLLVar, IsoType.LLLLVAR, 0);
        m.setBinary(true);

        // write
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        m.write(bout, 2);
        bout.close();

        // read
        byte[] buf = new byte[2];
        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        bin.read(buf);
        Assertions.assertNotEquals(buf, new byte[2]);

        int len = ((buf[0] & 0xff) << 8) | (buf[1] & 0xff);
        buf = new byte[len];
        bin.read(buf);
        bin.close();

        // parse
        mfact.setUseBinaryMessages(true);
        m = mfact.parseMessage(buf, mfact.getIsoHeader(0x100).length());
        Assertions.assertNotNull(m);
        Assertions.assertEquals(LLLLVar, m.getObjectValue(2));
    }

    private String makeLLLLVar(final int length) {
        final char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = 'a';
        }

        return new String(chars);
    }
}
