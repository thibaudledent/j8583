package com.solab.iso8583;

import com.solab.iso8583.parse.ConfigParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

/** These are very simple tests for creating and manipulating messages.
 *
 * @author Enrique Zamudio
 */
public class TestIsoMessage {

	private MessageFactory<IsoMessage> mf;

	@Before
	public void init() throws IOException {
		mf = new MessageFactory<>();
		mf.setCharacterEncoding("UTF-8");
		mf.setCustomField(48, new CustomField48());
		mf.setConfigPath("config.xml");
	}

	/** Creates a new message and checks that it has all the fields included in the config. */
	@Test
	public void testCreation() {
		IsoMessage iso = mf.newMessage(0x200);
        Assert.assertEquals(0x200, iso.getType());
        Assert.assertTrue(iso.hasEveryField(3, 32, 35, 43, 48, 49, 60, 61, 100, 102));
        Assert.assertEquals(IsoType.NUMERIC, iso.getField(3).getType());
        Assert.assertEquals("650000", iso.getObjectValue(3));
        Assert.assertEquals(IsoType.LLVAR, iso.getField(32).getType());
        Assert.assertEquals(IsoType.LLVAR, iso.getField(35).getType());
        Assert.assertEquals(IsoType.ALPHA, iso.getField(43).getType());
        Assert.assertEquals(40, ((String) iso.getObjectValue(43)).length());
        Assert.assertEquals(IsoType.LLLVAR, iso.getField(48).getType());
        Assert.assertTrue(iso.getObjectValue(48) instanceof CustomField48);
        Assert.assertEquals(IsoType.ALPHA, iso.getField(49).getType());
        Assert.assertEquals(IsoType.LLLVAR, iso.getField(60).getType());
        Assert.assertEquals(IsoType.LLLVAR, iso.getField(61).getType());
        Assert.assertEquals(IsoType.LLVAR, iso.getField(100).getType());
        Assert.assertEquals(IsoType.LLVAR, iso.getField(102).getType());
		for (int i = 4; i < 32; i++) {
            Assert.assertFalse("ISO should not contain " + i, iso.hasField(i));
		}
		for (int i = 36; i < 43; i++) {
            Assert.assertFalse("ISO should not contain " + i, iso.hasField(i));
		}
		for (int i = 50; i < 60; i++) {
            Assert.assertFalse("ISO should not contain " + i, iso.hasField(i));
		}
		for (int i = 62; i < 100; i++) {
            Assert.assertFalse("ISO should not contain " + i, iso.hasField(i));
		}
		for (int i = 103; i < 128; i++) {
            Assert.assertFalse("ISO should not contain " + i, iso.hasField(i));
		}
	}

	@Test
	public void testEncoding() throws Exception {
		IsoMessage m1 = mf.newMessage(0x200);
		byte[] buf = m1.writeData();
		IsoMessage m2 = mf.parseMessage(buf, mf.getIsoHeader(0x200).length());
        Assert.assertEquals(m2.getType(), m1.getType());
		for (int i = 2; i < 128; i++) {
			//Either both have the field or neither have it
			if (m1.hasField(i) && m2.hasField(i)) {
				Assert.assertEquals(m1.getField(i).getType(), m2.getField(i).getType());
                Object objectValue = m1.getObjectValue(i);
                Object actual = m2.getObjectValue(i);
                Assert.assertEquals(objectValue, actual);
			} else {
                Assert.assertFalse(m1.hasField(i));
                Assert.assertFalse(m2.hasField(i));
			}
		}
	}

	/** Parses a message from a file and checks the fields. */
	@Test
	public void testParsing() throws IOException, ParseException {
		InputStream ins = getClass().getResourceAsStream("/parse1.txt");
		final byte[] buf = new byte[400];
		int pos = 0;
		while (ins.available() > 0) {
			buf[pos++] = (byte)ins.read();
		}
		ins.close();
		IsoMessage iso = mf.parseMessage(buf, mf.getIsoHeader(0x210).length());
		Assert.assertEquals(0x210, iso.getType());
		byte[] b2 = iso.writeData();
		
		//Remove leftover newline and stuff from the original buffer
		byte[] b3 = new byte[b2.length];
		System.arraycopy(buf, 0, b3, 0, b3.length);
		Assert.assertArrayEquals(b3, b2);

        //Test it contains the correct fields
        final List<Integer> fields = Arrays.asList(3, 4, 7, 11, 12, 13, 15, 17, 32, 35, 37, 38, 39, 41, 43, 49, 60, 61, 100, 102, 126);
        testFields(iso, fields);
        //Again, but now with forced encoding
        mf.setForceStringEncoding(true);
        iso = mf.parseMessage(buf, mf.getIsoHeader(0x210).length());
        Assert.assertEquals(0x210, iso.getType());
        testFields(iso, fields);
	}

	@Test
	public void testTemplating() {
		IsoMessage iso1 = mf.newMessage(0x200);
		IsoMessage iso2 = mf.newMessage(0x200);
        Assert.assertNotSame(iso1, iso2);
        Assert.assertSame(iso1.getObjectValue(3), iso2.getObjectValue(3));
        Assert.assertNotSame(iso1.getField(3), iso2.getField(3));
        Assert.assertNotSame(iso1.getField(48), iso2.getField(48));
		CustomField48 cf48_1 = iso1.getObjectValue(48);
		int origv = cf48_1.getValue2();
		cf48_1.setValue2(origv + 1000);
		CustomField48 cf48_2 = iso2.getObjectValue(48);
        Assert.assertSame(cf48_1, cf48_2);
        Assert.assertEquals(cf48_2.getValue2(), origv + 1000);
	}

    @Test(expected = IllegalArgumentException.class)
    public void testSimpleFieldSetter() {
        IsoMessage iso = mf.newMessage(0x200);
        IsoValue<String> f3 = iso.getField(3);
        iso.updateValue(3, "999999");
        Assert.assertEquals("999999", iso.getObjectValue(3));
        IsoValue<String> nf3 = iso.getField(3);
        Assert.assertNotSame(f3, nf3);
        Assert.assertEquals(f3.getType(), nf3.getType());
        Assert.assertEquals(f3.getLength(), nf3.getLength());
        Assert.assertSame(f3.getEncoder(), nf3.getEncoder());
        iso.updateValue(4, "INVALID!");
        throw new RuntimeException("Update failed!");
    }

    @Test
    public void shouldWriteAndParseIBM1047Message() throws IOException, ParseException {
        // Given
        final MessageFactory mf = ConfigParser.createFromClasspathConfig("issue10.xml");
        mf.setUseBinaryBitmap(true);
        mf.setCharacterEncoding("Cp1047"); // IBM1047 encoding
        mf.setForceStringEncoding(true);

        final IsoMessage toWrite = mf.newMessage(0x1804);
        toWrite.setField(7, new IsoValue<>(IsoType.NUMERIC, "0204111422", 10));
        toWrite.setField(11, new IsoValue<>(IsoType.NUMERIC, "771930", 6));
        toWrite.setField(12, new IsoValue<>(IsoType.NUMERIC, "190204121422", 12));
        toWrite.setField(24, new IsoValue<>(IsoType.NUMERIC, "860", 3));
        toWrite.setField(37, new IsoValue<>(IsoType.ALPHA, "900312771930", 12));
        toWrite.setField(93, new IsoValue<>(IsoType.LLVAR, "50191150446"));
        toWrite.setField(94, new IsoValue<>(IsoType.LLVAR, "50191150020"));

        // When - writing
        final byte[] bytes = toWrite.writeData();

        // Then
        final String hexBinary = DatatypeConverter.printHexBinary(bytes);
        System.out.println(hexBinary);
        final String expected = "F1F8F0F482300100080000000000000C00000000F0F2F0F4F1F1F1F4F2F2F7F7F1F9F3F0F1F9F0F2F0F4F1F2F1F4F2F2F8F6F0F9F0F0F3F1F2F7F7F1F9F3F0F1F1F5F0F1F9F1F1F5F0F4F4F6F1F1F5F0F1F9F1F1F5F0F0F2F0";
        Assert.assertEquals(formatWithSpace(expected), formatWithSpace(hexBinary));
        // When - parsing
        final IsoMessage parseMessage = mf.parseMessage(bytes, 0);
        final byte[] parseBytes = parseMessage.writeData();

        // Then
        final String hexParseBinary = DatatypeConverter.printHexBinary(parseBytes);
        Assert.assertEquals(formatWithSpace(expected), formatWithSpace(hexParseBinary));
    }

    private static String formatWithSpace(final String toFormat) {
        return toFormat.replaceAll("..", "$0 ");
    }

    private void testFields(IsoMessage m, List<Integer> fields) {
        for (int i = 2; i < 128; i++) {
            if (fields.contains(i)) {
                Assert.assertTrue(m.hasField(i));
            } else {
                Assert.assertFalse(m.hasField(i));
            }
        }
    }
}
