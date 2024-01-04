package com.solab.iso8583;

import com.solab.iso8583.parse.NumericParseInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/** Test that parsing invalid messages is properly handled.
 *
 * @author Enrique Zamudio
 */
class TestParsing {

	private MessageFactory<IsoMessage> mf;

	@BeforeEach
	void init() throws IOException {
		mf = new MessageFactory<>();
		mf.setCharacterEncoding("UTF-8");
		mf.setCustomField(48, new CustomField48());
		mf.setConfigPath("config.xml");
	}

	@Test
	void testEmpty() {
		Assertions.assertThrows(ParseException.class, () -> mf.parseMessage(new byte[0], 0));
	}

	@Test
	void testShort() {
		Assertions.assertThrows(ParseException.class, () -> mf.parseMessage(new byte[20], 8));
	}

	@Test
	void testShortBin() {
		mf.setUseBinaryMessages(true);
		Assertions.assertThrows(ParseException.class, () -> mf.parseMessage(new byte[10], 1));
	}

	@Test
	void testShortSecondaryBitmap() {
		Assertions.assertThrows(ParseException.class, () -> mf.parseMessage("02008000000000000000".getBytes(), 0));
	}

	@Test
	void testShortSecondaryBitmapBin() {
		mf.setUseBinaryMessages(true);
		Assertions.assertThrows(ParseException.class, () -> mf.parseMessage(new byte[]{ 2, 0, (byte)128, 0, 0, 0, 0, 0, 0, 0 }, 0));
	}

	@Test
	void testNoFields() {
		Assertions.assertThrows(ParseException.class, () -> mf.parseMessage("0210B23A80012EA080180000000014000004".getBytes(), 0));
	}

	@Test
	void testNoFieldsBin() {
		mf.setUseBinaryMessages(true);
		Assertions.assertThrows(ParseException.class, () -> mf.parseMessage(new byte[]{2, 0x10, (byte)0xB2, 0x3A, (byte)0x80, 1, 0x2E, (byte)0xA0, (byte)0x80, 0x18, 0, 0, 0, 0, 0x14, 0, 0, 4}, 0));
	}

	@Test
	void testIncompleteFixedField() {
		Assertions.assertThrows(ParseException.class, () -> mf.parseMessage("0210B23A80012EA08018000000001400000465000".getBytes(), 0));
	}

	@Test
	void testIncompleteFixedFieldBin() {
		mf.setUseBinaryMessages(true);
		Assertions.assertThrows(ParseException.class, () -> mf.parseMessage(new byte[]{2, 0x10, (byte)0xB2, 0x3A, (byte)0x80, 1, 0x2E, (byte)0xA0, (byte)0x80, 0x18, 0, 0, 0, 0, 0x14, 0, 0, 4, 0x65, 0}, 0));
	}

	@Test
	void testIncompleteVarFieldHeader()  {
		Assertions.assertThrows(ParseException.class, () -> mf.parseMessage("0210B23A80012EA08018000000001400000465000000000000300004281305474687711259460428042808115".getBytes(), 0));
	}

	@Test
	void testIncompleteVarFieldHeaderBin()  {
		mf.setUseBinaryMessages(true);
		Assertions.assertThrows(ParseException.class, () -> mf.parseMessage(new byte[]{2, 0x10, (byte)0xB2, 0x3A, (byte)0x80, 1, 0x2E, (byte)0xA0, (byte)0x80, 0x18, 0, 0, 0, 0, 0x14, 0, 0, 4, 0x65, 0, 0, 0, 0, 0, 0, 0x30, 0, 0x04, 0x28, 0x13, 0x05, 0x47, 0x46, (byte)0x87, 0x71, 0x12, 0x59, 0x46, 0x04, 0x28, 0x04, 0x28, 0x08, 0x11}, 0));
	}

	@Test
	void testIncompleteVarFieldData()  {
		Assertions.assertThrows(ParseException.class, () -> mf.parseMessage("0210B23A80012EA0801800000000140000046500000000000030000428130547468771125946042804280811051234".getBytes(), 0));
	}

	@Test
	void testIncompleteVarFieldDataBin()  {
		mf.setUseBinaryMessages(true);
		Assertions.assertThrows(ParseException.class, () -> mf.parseMessage(new byte[]{2, 0x10, (byte)0xB2, 0x3A, (byte)0x80, 1, 0x2E, (byte)0xA0, (byte)0x80, 0x18, 0, 0, 0, 0, 0x14, 0, 0, 4, 0x65, 0, 0, 0, 0, 0, 0, 0x30, 0, 0x04, 0x28, 0x13, 0x05, 0x47, 0x46, (byte)0x87, 0x71, 0x12, 0x59, 0x46, 0x04, 0x28, 0x04, 0x28, 0x08, 0x11, 0x05, 0x12, 0x34}, 0));
	}

    @Test
    void testBinaryNumberParsing() throws ParseException {
        NumericParseInfo npi = new NumericParseInfo(6);
        IsoValue<Number> val = npi.parseBinary(0, new byte[]{0x12, 0x34, 0x56}, 0, null);
        Assertions.assertEquals(123456, val.getValue().intValue());
    }

    @Test
    void testDates() throws ParseException, UnsupportedEncodingException {
		com.solab.iso8583.parse.DateTimeParseInfo.setDefaultTimeZone(TimeZone.getTimeZone("GMT-0700"));
		Calendar cal = new GregorianCalendar();

        IsoMessage m = mf.parseMessage("060002000000000000000125213456".getBytes(), 0);
        Assertions.assertNotNull(m);
        Date f = m.getObjectValue(7);
        Assertions.assertNotNull(f);

		cal.setTimeZone(TimeZone.getTimeZone("GMT-0700"));
		cal.setTime(f);
        Assertions.assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        Assertions.assertEquals(25, cal.get(Calendar.DATE), "Date");
        Assertions.assertEquals(21, cal.get(Calendar.HOUR_OF_DAY), "Hour of Day");
		Assertions.assertEquals("060002000000000000000125213456", m.debugString(), "debug string should match");

		com.solab.iso8583.parse.DateTimeParseInfo.setDefaultTimeZone(null);
		TimeZone utcTz = TimeZone.getTimeZone("UTC");
		mf.setTimezoneForParseGuide(0x600, 7, utcTz);


		m = mf.parseMessage("060002000000000000000125213456".getBytes(), 0);
        f = m.getObjectValue(7);
		cal.setTimeZone(TimeZone.getTimeZone("GMT-0600"));
		cal.setTime(f);

		Assertions.assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        Assertions.assertEquals(25, cal.get(Calendar.DATE));
        Assertions.assertEquals(15, cal.get(Calendar.HOUR_OF_DAY), "Hour of day mismatch");
        Assertions.assertEquals(TimeZone.getTimeZone("UTC"), m.getField(7).getTimeZone());
        mf.setTimezoneForParseGuide(0x600, 7, TimeZone.getTimeZone("GMT+0100"));
        m = mf.parseMessage("060002000000000000000125213456".getBytes(), 0);
        f = m.getObjectValue(7);
        cal.setTime(f);
        Assertions.assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
        Assertions.assertEquals(25, cal.get(Calendar.DATE));
        Assertions.assertEquals(14, cal.get(Calendar.HOUR_OF_DAY), "Hour of day mismatch");
        Assertions.assertEquals(TimeZone.getTimeZone("GMT+0100"), m.getField(7).getTimeZone());
    }

    @Test
    void testTimezoneInResponse() throws ParseException, IOException {
        mf.setTimezoneForParseGuide(0x600, 7, TimeZone.getTimeZone("UTC"));
        IsoMessage m = mf.parseMessage("060002000000000000000125213456".getBytes(), 0);
        Assertions.assertEquals(TimeZone.getTimeZone("UTC"), m.getField(7).getTimeZone());
        IsoMessage r = mf.createResponse(m);
        Assertions.assertEquals(0x610, r.getType());
        Assertions.assertTrue(r.hasField(7));
        Assertions.assertTrue(m.getField(7) != r.getField(7));
        Assertions.assertEquals(TimeZone.getTimeZone("UTC"), r.getField(7).getTimeZone());
    }
}
