package com.solab.iso8583;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.TimeZone;

/**
 * Tests formatting of certain IsoTypes.
 *
 * @author Enrique Zamudio
 */
class TestFormats {

    private final Date date = new Date(96867296000L);

    @Test
    void testDateFormats() {
        TimeZone defaultTz = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("GMT-0600"));
        try {
            Assertions.assertEquals("0125213456", IsoType.DATE10.format(date, null));
            Assertions.assertEquals("0125", IsoType.DATE4.format(date, null));
            Assertions.assertEquals("7301", IsoType.DATE_EXP.format(date, null));
            Assertions.assertEquals("213456", IsoType.TIME.format(date, null));
            Assertions.assertEquals("730125213456", IsoType.DATE12.format(date, null));
            Assertions.assertEquals("19730125213456", IsoType.DATE14.format(date, null));
        } finally {
            TimeZone.setDefault(defaultTz);
        }

        //Now with GMT
        TimeZone gmt = TimeZone.getTimeZone("GMT");
        Assertions.assertEquals("0126033456", IsoType.DATE10.format(date, gmt));
        Assertions.assertEquals("0126", IsoType.DATE4.format(date, gmt));
        Assertions.assertEquals("7301", IsoType.DATE_EXP.format(date, gmt));
        Assertions.assertEquals("033456", IsoType.TIME.format(date, gmt));
        Assertions.assertEquals("730126033456", IsoType.DATE12.format(date, gmt));
        Assertions.assertEquals("19730126033456", IsoType.DATE14.format(date, gmt));
        //And now with GMT+1
        gmt = TimeZone.getTimeZone("GMT+0100");
        Assertions.assertEquals("0126043456", IsoType.DATE10.format(date, gmt));
        Assertions.assertEquals("0126", IsoType.DATE4.format(date, gmt));
        Assertions.assertEquals("7301", IsoType.DATE_EXP.format(date, gmt));
        Assertions.assertEquals("043456", IsoType.TIME.format(date, gmt));
        Assertions.assertEquals("730126043456", IsoType.DATE12.format(date, gmt));
        Assertions.assertEquals("19730126043456", IsoType.DATE14.format(date, gmt));
    }

    @Test
    void testNumericFormats() {
        Assertions.assertEquals("000123", IsoType.NUMERIC.format(123, 6));
        Assertions.assertEquals("00hola", IsoType.NUMERIC.format("hola", 6));
        Assertions.assertEquals("000001234500", IsoType.AMOUNT.format(12345, 0));
        Assertions.assertEquals("000001234567", IsoType.AMOUNT.format(new BigDecimal("12345.67"), 0));
        Assertions.assertEquals("000000123456", IsoType.AMOUNT.format("1234.56", 0));
    }

    @Test
    void testStringFormats() {
        Assertions.assertEquals("hol", IsoType.ALPHA.format("hola", 3));
        Assertions.assertEquals("hola", IsoType.ALPHA.format("hola", 4));
        Assertions.assertEquals("hola  ", IsoType.ALPHA.format("hola", 6));
        Assertions.assertEquals("hola", IsoType.LLVAR.format("hola", 0));
        Assertions.assertEquals("hola", IsoType.LLLVAR.format("hola", 0));
        Assertions.assertEquals("HOLA", IsoType.LLLLVAR.format("HOLA", 0));
    }

}
