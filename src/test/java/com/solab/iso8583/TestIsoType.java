package com.solab.iso8583;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

class TestIsoType {

    @Test
    void shouldNotFormatDateWithWeekOfYear() throws ParseException {
        // Given
        Date date = new SimpleDateFormat("yyyy/MM/dd").parse("2015/12/31");

        // When
        String formatedDate = IsoType.DATE14.format(date, TimeZone.getDefault());

        // Then
        Assertions.assertEquals("20151231000000", formatedDate);
    }

}
