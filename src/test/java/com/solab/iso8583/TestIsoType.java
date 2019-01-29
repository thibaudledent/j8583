package com.solab.iso8583;

import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TestIsoType {

    @Test
    public void shouldNotFormatDateWithWeekOfYear() throws ParseException {
        // Given
        Date date = new SimpleDateFormat("yyyy/MM/dd").parse("2015/12/31");

        // When
        String formatedDate = IsoType.DATE14.format(date, TimeZone.getDefault());

        // Then
        Assert.assertEquals("20151231000000", formatedDate);
    }

}
