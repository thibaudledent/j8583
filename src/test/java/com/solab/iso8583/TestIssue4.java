package com.solab.iso8583;

import com.solab.iso8583.parse.ConfigParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.ParseException;

/**
 * Tests for chochos/j8583#4.
 *
 * @author Enrique Zamudio
 *         Date: 18/01/13 10:13
 */
class TestIssue4 {

    @Test
    void testTextBitmap() throws IOException, ParseException {
        MessageFactory<IsoMessage> tmf = new MessageFactory<>();
        ConfigParser.configureFromClasspathConfig(tmf, "issue4.xml");
        IsoMessage tm = tmf.newMessage(0x800);
        final ByteBuffer bb = tm.writeToBuffer(2);
        Assertions.assertEquals(70, bb.array().length, "Wrong message length for new TXT");
        Assertions.assertEquals(68, bb.getShort());

        MessageFactory<IsoMessage> tmfp = new MessageFactory<>();
        ConfigParser.configureFromClasspathConfig(tmfp, "issue4.xml");
        byte[] buf2 = new byte[bb.remaining()];
        bb.get(buf2);
        tm = tmfp.parseMessage(buf2, 0);
        final ByteBuffer bbp = tm.writeToBuffer(2);
        Assertions.assertArrayEquals(bb.array(), bbp.array(), "Parsed-reencoded TXT differs from original");
    }

    @Test
    void testBinaryBitmap() throws IOException, ParseException {
        MessageFactory<IsoMessage> mf = new MessageFactory<>();
        ConfigParser.configureFromClasspathConfig(mf, "issue4.xml");
        IsoMessage bm = mf.getMessageTemplate(0x800);
        bm.setBinaryBitmap(true);
        final ByteBuffer bb = bm.writeToBuffer(2);
        Assertions.assertEquals(62, bb.array().length, "Wrong message length for new BIN");
        Assertions.assertEquals(60, bb.getShort());

        MessageFactory<IsoMessage> mfp = new MessageFactory<>();
        mfp.setUseBinaryBitmap(true);
        ConfigParser.configureFromClasspathConfig(mfp, "issue4.xml");
        byte[] buf2 = new byte[bb.remaining()];
        bb.get(buf2);
        bm = mfp.parseMessage(buf2, 0);
        Assertions.assertTrue(bm.isBinaryBitmap(), "Parsed message should have binary bitmap flag set");
        Assertions.assertFalse(bm.isBinaryHeader() || bm.isBinaryFields());
        final ByteBuffer bbp = bm.writeToBuffer(2);
        Assertions.assertArrayEquals(bb.array(), bbp.array(), "Parsed-reencoded BIN differs from original");
    }

}
