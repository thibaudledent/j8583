package com.solab.iso8583;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.TimeZone;

/**
 * Tests formatting of certain IsoTypes.
 *
 * @author Enrique Zamudio
 */
public class TestFormats {

    private final Date date = new Date(96867296000L);

    @Test
    public void testDateFormats() {
        TimeZone defaultTz = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("GMT-0600"));
        try {
            Assert.assertEquals("0125213456", IsoType.DATE10.format(date, null));
            Assert.assertEquals("0125", IsoType.DATE4.format(date, null));
            Assert.assertEquals("7301", IsoType.DATE_EXP.format(date, null));
            Assert.assertEquals("213456", IsoType.TIME.format(date, null));
            Assert.assertEquals("730125213456", IsoType.DATE12.format(date, null));
            Assert.assertEquals("19730125213456", IsoType.DATE14.format(date, null));
        } finally {
            TimeZone.setDefault(defaultTz);
        }

        //Now with GMT
        TimeZone gmt = TimeZone.getTimeZone("GMT");
        Assert.assertEquals("0126033456", IsoType.DATE10.format(date, gmt));
        Assert.assertEquals("0126", IsoType.DATE4.format(date, gmt));
        Assert.assertEquals("7301", IsoType.DATE_EXP.format(date, gmt));
        Assert.assertEquals("033456", IsoType.TIME.format(date, gmt));
        Assert.assertEquals("730126033456", IsoType.DATE12.format(date, gmt));
        Assert.assertEquals("19730126033456", IsoType.DATE14.format(date, gmt));
        //And now with GMT+1
        gmt = TimeZone.getTimeZone("GMT+0100");
        Assert.assertEquals("0126043456", IsoType.DATE10.format(date, gmt));
        Assert.assertEquals("0126", IsoType.DATE4.format(date, gmt));
        Assert.assertEquals("7301", IsoType.DATE_EXP.format(date, gmt));
        Assert.assertEquals("043456", IsoType.TIME.format(date, gmt));
        Assert.assertEquals("730126043456", IsoType.DATE12.format(date, gmt));
        Assert.assertEquals("19730126043456", IsoType.DATE14.format(date, gmt));
    }

    @Test
    public void testNumericFormats() {
        Assert.assertEquals("000123", IsoType.NUMERIC.format(123, 6));
        Assert.assertEquals("00hola", IsoType.NUMERIC.format("hola", 6));
        Assert.assertEquals("000001234500", IsoType.AMOUNT.format(12345, 0));
        Assert.assertEquals("000001234567", IsoType.AMOUNT.format(new BigDecimal("12345.67"), 0));
        Assert.assertEquals("000000123456", IsoType.AMOUNT.format("1234.56", 0));
    }

    @Test
    public void testStringFormats() {
        Assert.assertEquals("hol", IsoType.ALPHA.format("hola", 3));
        Assert.assertEquals("hola", IsoType.ALPHA.format("hola", 4));
        Assert.assertEquals("hola  ", IsoType.ALPHA.format("hola", 6));
        Assert.assertEquals("hola", IsoType.LLVAR.format("hola", 0));
        Assert.assertEquals("hola", IsoType.LLLVAR.format("hola", 0));
        Assert.assertEquals("HOLA", IsoType.LLLLVAR.format("HOLA", 0));
    }

}
