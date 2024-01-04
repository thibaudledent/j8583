package com.solab.iso8583.parse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Test that the dates are formatted and parsed correctly.
 * 
 * @author Enrique Zamudio
 */
class TestDates {

	@Test
	void testDate4FutureTolerance() throws ParseException, IOException {
		GregorianCalendar today = new GregorianCalendar();
		Date soon = new Date(today.getTime().getTime() + 50000);
		today.set(GregorianCalendar.HOUR,0);
		today.set(GregorianCalendar.MINUTE,0);
		today.set(GregorianCalendar.SECOND,0);
		today.set(GregorianCalendar.MILLISECOND,0);
		byte[] buf = IsoType.DATE4.format(soon, null).getBytes();
		IsoValue<Date> comp = new Date4ParseInfo().parse(0, buf, 0, null);
		Assertions.assertEquals(comp.getValue(), today.getTime());
		//Now with the binary
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		comp.write(bout, true, false);
		IsoValue<Date> bin = new Date4ParseInfo().parseBinary(0, bout.toByteArray(), 0, null);
		Assertions.assertEquals(comp.getValue().getTime(), bin.getValue().getTime());
	}

	@Test
	void testDate10FutureTolerance() throws ParseException, IOException {
		Date soon = new Date(System.currentTimeMillis() + 50000);
		byte[] buf = IsoType.DATE10.format(soon, null).getBytes();
		IsoValue<Date> comp = new Date10ParseInfo().parse(0, buf, 0, null);
		assert comp.getValue().after(new Date());
		//Now with the binary
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		comp.write(bout, true, false);
		IsoValue<Date> bin = new Date10ParseInfo().parseBinary(0, bout.toByteArray(), 0, null);
		Assertions.assertEquals(comp.getValue().getTime(), bin.getValue().getTime());
	}

	@Test
	void testDate12FutureTolerance() throws ParseException, IOException {
		Date soon = new Date(System.currentTimeMillis() + 50000);
		byte[] buf = IsoType.DATE12.format(soon, null).getBytes();
		IsoValue<Date> comp = new Date12ParseInfo().parse(0, buf, 0, null);
		assert comp.getValue().after(new Date());
		//Now with the binary
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		comp.write(bout, true, false);
		IsoValue<Date> bin = new Date12ParseInfo().parseBinary(0, bout.toByteArray(), 0, null);
		Assertions.assertEquals(comp.getValue().getTime(), bin.getValue().getTime());
	}

	@Test
	void testDate14FutureTolerance() throws ParseException, IOException {
		Date soon = new Date(System.currentTimeMillis() + 50000);
		byte[] buf = IsoType.DATE14.format(soon, null).getBytes();
		IsoValue<Date> comp = new Date14ParseInfo().parse(0, buf, 0, null);
		assert comp.getValue().after(new Date());
		//Now with the binary
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		comp.write(bout, true, false);
		IsoValue<Date> bin = new Date14ParseInfo().parseBinary(0, bout.toByteArray(), 0, null);
		Assertions.assertEquals(comp.getValue().getTime(), bin.getValue().getTime());
	}

    @Test
    void testDate6() throws ParseException, IOException {
	    TimeZone defaultTz =TimeZone.getDefault();
	    TimeZone.setDefault(TimeZone.getTimeZone("GMT-0600"));
	    try {
			Date nye = new Date(1514700000000L);
			IsoValue<Date> f = new IsoValue<>(IsoType.DATE6, nye);
			Assertions.assertEquals("171231", f.toString());
			IsoValue<Date> parsed = new Date6ParseInfo().parse(1, "171231".getBytes(), 0, null);
			Assertions.assertEquals("171231", parsed.toString());
			Assertions.assertEquals(f.getValue(), parsed.getValue());
			byte[] buf = new byte[3];
			buf[0] = 0x17;
			buf[1] = 0x12;
			buf[2] = 0x31;
			parsed = new Date6ParseInfo().parseBinary(2, buf, 0, null);
			Assertions.assertEquals("171231", parsed.toString());
			Assertions.assertEquals(f.getValue(), parsed.getValue());
		}
		finally {
	    	TimeZone.setDefault(defaultTz);
		}
    }

}
