package com.solab.iso8583;

import com.solab.iso8583.util.HexCodec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;

/** Test binary message encoding and binary fields. */
class TestBinaries {

	private MessageFactory<IsoMessage> mfactAscii = new MessageFactory<>();
	private MessageFactory<IsoMessage> mfactBin = new MessageFactory<>();

	@BeforeEach
	void setup() throws IOException {
		mfactAscii.setCharacterEncoding("UTF-8");
		mfactAscii.setConfigPath("config.xml");
		mfactAscii.setAssignDate(true);
		mfactBin.setCharacterEncoding("UTF-8");
		mfactBin.setConfigPath("config.xml");
		mfactBin.setAssignDate(true);
		mfactBin.setUseBinaryMessages(true);
	}

	void testParsed(IsoMessage m) {
		Assertions.assertEquals(m.getType(), 0x600);
		Assertions.assertEquals(new BigDecimal("1234.00"), m.getObjectValue(4));
		Assertions.assertTrue(m.hasField(7), "No field 7!");
		Assertions.assertEquals("000123", m.getField(11).toString(), "Wrong trace");
		byte[] buf = m.getObjectValue(41);
		byte[] exp = new byte[]{ (byte)0xab, (byte)0xcd, (byte)0xef, 0, 0, 0, 0, 0};
		Assertions.assertEquals(8, buf.length, "Field 41 wrong length");
		Assertions.assertArrayEquals(exp, buf, "Field 41 wrong value");
		buf = m.getObjectValue(42);
		exp = new byte[]{ (byte)0x0a, (byte)0xbc, (byte)0xde, 0 };
		Assertions.assertEquals(4, buf.length, "field 42 wrong length");
		Assertions.assertArrayEquals(exp, buf, "Field 42 wrong value");
		Assertions.assertTrue(((String)m.getObjectValue(43)).startsWith("Field of length 40"));
		buf = m.getObjectValue(62);
		exp = new byte[]{ 1, (byte)0x23, (byte)0x45, (byte)0x67, (byte)0x89, (byte)0xab, (byte)0xcd, (byte)0xef,
				0x62, 1, (byte)0x23, (byte)0x45, (byte)0x67, (byte)0x89, (byte)0xab, (byte)0xcd };
		Assertions.assertArrayEquals(exp, buf);
		buf = m.getObjectValue(64);
        exp[8] = 0x64;
		Assertions.assertArrayEquals(exp, buf);
		buf = m.getObjectValue(63);
		exp = new byte[]{ 0, (byte)0x12, (byte)0x34, (byte)0x56, (byte)0x78, (byte)0x63 };
		Assertions.assertArrayEquals(exp, buf);
		buf = m.getObjectValue(65);
        exp[5] = 0x65;
		Assertions.assertArrayEquals(exp, buf);
	}

	@Test
	void testMessages() throws ParseException, UnsupportedEncodingException {
		//Create a message with both factories
		IsoMessage ascii = mfactAscii.newMessage(0x600);
		IsoMessage bin = mfactBin.newMessage(0x600);
        Assertions.assertFalse(ascii.isBinaryHeader() || ascii.isBinaryFields() || ascii.isBinaryBitmap());
        Assertions.assertTrue(bin.isBinaryHeader() && bin.isBinaryFields());
		//HEXencode the binary message, headers should be similar to the ASCII version
        final byte[] _v = bin.writeData();
		String hexBin = HexCodec.hexEncode(_v, 0, _v.length);
		String hexAscii = new String(ascii.writeData()).toUpperCase();
		Assertions.assertEquals("0600", hexBin.substring(0, 4));
		//Should be the same up to the field 42 (first 80 chars)
		Assertions.assertEquals(hexAscii.substring(0, 88), hexBin.substring(0, 88));
        Assertions.assertEquals(ascii.getObjectValue(43), new String(_v, 44, 40).trim());
		//Parse both messages
		byte[] asciiBuf = ascii.writeData();
		IsoMessage ascii2 = mfactAscii.parseMessage(asciiBuf, 0);
		testParsed(ascii2);
		Assertions.assertEquals(ascii.getObjectValue(7).toString(), ascii2.getObjectValue(7).toString());
		IsoMessage bin2 = mfactBin.parseMessage(bin.writeData(), 0);
		//Compare values, should be the same
		testParsed(bin2);
		Assertions.assertEquals(bin.getObjectValue(7).toString(), bin2.getObjectValue(7).toString());
        //Test the debug string
        ascii.setValue(60, "XXX", IsoType.LLVAR, 0);
        bin.setValue(60, "XXX", IsoType.LLVAR, 0);
        Assertions.assertEquals(ascii.debugString(), bin.debugString(), "Debug strings differ");
        Assertions.assertTrue(ascii.debugString().contains("03XXX"), "LLVAR fields wrong");
	}

    @Test
    void testBinaryBitmap() throws UnsupportedEncodingException {
        IsoMessage iso1 = mfactAscii.newMessage(0x200);
        IsoMessage iso2 = mfactAscii.newMessage(0x200);
        iso1.setBinaryBitmap(true);
        byte[] data1 = iso1.writeData();
        byte[] data2 = iso2.writeData();
        //First message should be shorter by exactly 16 bytes
        Assertions.assertEquals(data2.length-16, data1.length);
        //compare hex-encoded bitmap from one against the other
        byte[] sub1 = new byte[8];
        System.arraycopy(data1, 16, sub1, 0, 8);
        String sub2 = new String(data2, 16, 16, iso2.getCharacterEncoding());
        Assertions.assertEquals(sub2, HexCodec.hexEncode(sub1, 0, sub1.length));
    }

    @Test
	void test61() throws ParseException, UnsupportedEncodingException {
		BigInteger bignum = new BigInteger("1234512345123451234");
		IsoMessage iso1 = mfactBin.newMessage(0x201);
		iso1.setValue(3, bignum, IsoType.NUMERIC, 19);
        iso1.setField(7, null);
        byte[] buf = iso1.writeData();
        System.out.println(HexCodec.hexEncode(buf, 0, buf.length));
		IsoMessage iso2 = mfactBin.parseMessage(buf, 0);
		Assertions.assertEquals(bignum, iso2.getObjectValue(3));
        bignum = new BigInteger("1234512345123451234522");
        iso1 = mfactBin.newMessage(0x202);
        iso1.setValue(3, bignum, IsoType.NUMERIC, 22);
        iso1.setField(7, null);
        buf = iso1.writeData();
        System.out.println(HexCodec.hexEncode(buf, 0, buf.length));
		iso2 = mfactBin.parseMessage(buf, 0);
		Assertions.assertEquals(bignum, iso2.getObjectValue(3));
    }

    @Test
    void testLLBCDBINWithoutZero() throws IOException, ParseException {
        MessageFactory<IsoMessage> messageFactory = new MessageFactory<>();
        messageFactory.setCharacterEncoding("UTF-8");
        messageFactory.setConfigPath("config.xml");
        messageFactory.setUseBinaryMessages(true);

        IsoMessage iso1 = messageFactory.newMessage(0x281);
        iso1.setField(3, new IsoValue<>(IsoType.LLBCDBIN, "12345"));
        byte[] buf = iso1.writeData();

        IsoMessage iso2 = messageFactory.parseMessage(buf, 0);
        String value = iso2.getField(3).toString();
        Assertions.assertEquals("12345", value);

        iso1.setBinary(false);
        buf = iso1.writeData();
        messageFactory.setUseBinaryMessages(false);
        iso2 = messageFactory.parseMessage(buf, 0);
        value = iso2.getField(3).toString();
        Assertions.assertEquals("012345", value);
    }

    @Test
    void testLLBCDBINWithZero() throws IOException, ParseException {
        MessageFactory<IsoMessage> messageFactory = new MessageFactory<>();
        messageFactory.setCharacterEncoding("UTF-8");
        messageFactory.setConfigPath("config.xml");
        messageFactory.setUseBinaryMessages(true);

        IsoMessage iso1 = messageFactory.newMessage(0x281);
        iso1.setField(3, new IsoValue<>(IsoType.LLBCDBIN, "012345"));
        byte[] buf = iso1.writeData();
        
        IsoMessage iso2 = messageFactory.parseMessage(buf, 0);
        String value = iso2.getField(3).toString();
        Assertions.assertEquals("012345", value);

        iso1.setBinary(false);
        buf = iso1.writeData();
        messageFactory.setUseBinaryMessages(false);
        iso2 = messageFactory.parseMessage(buf, 0);
        value = iso2.getField(3).toString();
        Assertions.assertEquals("012345", value);
    }

    @Test
    void testLLLBCDBINWithoutZero() throws IOException, ParseException {
        MessageFactory<IsoMessage> messageFactory = new MessageFactory<>();
        messageFactory.setCharacterEncoding("UTF-8");
        messageFactory.setConfigPath("config.xml");
        messageFactory.setUseBinaryMessages(true);

        IsoMessage iso1 = messageFactory.newMessage(0x282);
        iso1.setField(3, new IsoValue<>(IsoType.LLLBCDBIN, "123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD"));
        byte[] buf = iso1.writeData();

        IsoMessage iso2 = messageFactory.parseMessage(buf, 0);
        String value = iso2.getField(3).toString();
        Assertions.assertEquals("123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD", value);

        iso1.setBinary(false);
        buf = iso1.writeData();
        messageFactory.setUseBinaryMessages(false);
        iso2 = messageFactory.parseMessage(buf, 0);
        value = iso2.getField(3).toString();
        //In ASCII mode the leading 0 can't be truncated
        Assertions.assertEquals("0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD", value);
    }

    @Test
    void testLLLBCDBINWithZero() throws IOException, ParseException {
        MessageFactory<IsoMessage> messageFactory = new MessageFactory<>();
        messageFactory.setCharacterEncoding("UTF-8");
        messageFactory.setConfigPath("config.xml");
        messageFactory.setUseBinaryMessages(true);

        IsoMessage iso1 = messageFactory.newMessage(0x282);
        iso1.setField(3, new IsoValue<>(IsoType.LLLBCDBIN, "0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD"));
        byte[] buf = iso1.writeData();

        IsoMessage iso2 = messageFactory.parseMessage(buf, 0);
        String value = iso2.getField(3).toString();
        Assertions.assertEquals("0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD", value);

        iso1.setBinary(false);
        buf = iso1.writeData();
        messageFactory.setUseBinaryMessages(false);
        iso2 = messageFactory.parseMessage(buf, 0);
        value = iso2.getField(3).toString();
        Assertions.assertEquals("0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD", value);
    }

    @Test
    void testLLLLBCDBINWithoutZero() throws IOException, ParseException {
        MessageFactory<IsoMessage> messageFactory = new MessageFactory<>();
        messageFactory.setCharacterEncoding("UTF-8");
        messageFactory.setConfigPath("config.xml");
        messageFactory.setUseBinaryMessages(true);

        IsoMessage iso1 = messageFactory.newMessage(0x283);
        iso1.setField(3, new IsoValue<>(IsoType.LLLLBCDBIN, "123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD"));
        byte[] buf = iso1.writeData();

        IsoMessage iso2 = messageFactory.parseMessage(buf, 0);
        String value = iso2.getField(3).toString();
        Assertions.assertEquals("123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD", value);

        iso1.setBinary(false);
        buf = iso1.writeData();
        messageFactory.setUseBinaryMessages(false);
        iso2 = messageFactory.parseMessage(buf, 0);
        value = iso2.getField(3).toString();
        //ASCII mode cannot truncate the leading 0
        Assertions.assertEquals("0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD", value);
    }

    @Test
    void testLLLLBCDBINWithZero() throws IOException, ParseException {
        MessageFactory<IsoMessage> messageFactory = new MessageFactory<>();
        messageFactory.setCharacterEncoding("UTF-8");
        messageFactory.setConfigPath("config.xml");
        messageFactory.setUseBinaryMessages(true);

        IsoMessage iso1 = messageFactory.newMessage(0x283);
        iso1.setField(3, new IsoValue<>(IsoType.LLLLBCDBIN, "0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD"));
        byte[] buf = iso1.writeData();

        IsoMessage iso2 = messageFactory.parseMessage(buf, 0);
        String value = iso2.getField(3).toString();
        Assertions.assertEquals("0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD", value);

        iso1.setBinary(false);
        buf = iso1.writeData();
        messageFactory.setUseBinaryMessages(false);
        iso2 = messageFactory.parseMessage(buf, 0);
        value = iso2.getField(3).toString();
        Assertions.assertEquals("0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD0123456789ABCDEF640123456789ABCD", value);
    }

    @Test
    void testLLBCDAlphaNum() throws IOException, ParseException {
        final String field3Value = "1".repeat(28);
        final String field4Value = "1234";
        MessageFactory<IsoMessage> messageFactory = new MessageFactory<>();
        messageFactory.setConfigPath("config.xml");
        messageFactory.setUseBinaryMessages(true);

        IsoMessage iso1 = messageFactory.newMessage(0x289);
        iso1.setField(3, new IsoValue<>(IsoType.LLBCDLENGTHALPHANUM, field3Value));
        iso1.setField(4, new IsoValue<>(IsoType.LLBINLENGTHBIN, field4Value));
        byte[] binaryMessage = iso1.writeData();

        Assertions.assertEquals("0289" + "3000000000000000" + "28" + "31".repeat(28) + "02" + field4Value, HexCodec.hexEncode(binaryMessage,0, binaryMessage.length));

        IsoMessage iso2 = messageFactory.parseMessage(binaryMessage, 0);
        String decodedField3Value = iso2.getField(3).toString();
        String decodedField4Value = iso2.getField(4).toString();

        Assertions.assertEquals(field3Value, decodedField3Value);
        Assertions.assertEquals(field4Value, decodedField4Value);
    }

    @Test
    void testRawBinaryWithNonBinaryMessage() throws IOException, ParseException {
        MessageFactory messageFactory = new MessageFactory();
        messageFactory.setCharacterEncoding("Cp277");
        messageFactory.setForceStringEncoding(true);
        messageFactory.setConfigPath("config.xml");
        messageFactory.setUseBinaryMessages(false);

        IsoMessage iso1 = messageFactory.newMessage(0x291);
        byte[] rawValue = new byte[]{ 0, (byte)0x12, (byte)0x34, (byte)0x56, (byte)0x78, (byte)0x63, (byte)0x15, (byte)0x25};
        iso1.setField(64, new IsoValue<>(IsoType.RAW_BINARY, rawValue, 8));
        byte[] buf = iso1.writeData();

        // When
        IsoMessage iso2 = messageFactory.parseMessage(buf, 0);

        // Then
        //      The message is retrieved as-is (appended with 0's to match the expected length)
        String value = iso2.getField(64).toString();
        Assertions.assertEquals("0012345678631525", value);
    }
}
