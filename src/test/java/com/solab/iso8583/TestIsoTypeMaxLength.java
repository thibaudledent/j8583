package com.solab.iso8583;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class TestIsoTypeMaxLength {

    @Parameterized.Parameters(name = "type={0}, expected={1}, lengthThatWillFail={2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {IsoType.LLVAR, "LLVAR can only hold values up to 99 chars", 100},
            {IsoType.LLLVAR, "LLLVAR can only hold values up to 999 chars", 1000},
            {IsoType.LLLLVAR, "LLLLVAR can only hold values up to 9999 chars", 10000},
            {IsoType.LLBIN, "LLBIN can only hold values up to 99 chars", 100},
            {IsoType.LLLBIN, "LLLBIN can only hold values up to 999 chars", 1000},
            {IsoType.LLLLBIN, "LLLLBIN can only hold values up to 9999 chars", 10000},
            {IsoType.LLBCDBIN, "LLBCDBIN can only hold values up to 50 chars", 51},
            {IsoType.LLLBCDBIN, "LLLBCDBIN can only hold values up to 500 chars", 501},
            {IsoType.LLLLBCDBIN, "LLLLBCDBIN can only hold values up to 5000 chars", 5001},
            {IsoType.LLBINLENGTHNUM, "LLBINLENGTHNUM can only hold values up to 255 chars", 256},
            {IsoType.LLBINLENGTHALPHANUM, "LLBINLENGTHALPHANUM can only hold values up to 255 chars", 256},
            {IsoType.LLBINLENGTHBIN, "LLBINLENGTHBIN can only hold values up to 255 chars", 256},
            {IsoType.LLLLBINLENGTHBIN, "LLLLBINLENGTHBIN can only hold values up to 65535 chars", 65536},
        });
    }

    private final IsoType type;
    private final String expected;
    private final int lengthThatWillFail;

    public TestIsoTypeMaxLength(final IsoType type, final String expected, final int lengthThatWillFail) {
        this.type = type;
        this.expected = expected;
        this.lengthThatWillFail = lengthThatWillFail;
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenUsingLengthInConstructor() {
        try {
            // When
            new IsoValue<>(type, "value", lengthThatWillFail);
            Assert.fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            // Then
            Assert.assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhithoutUsingLengthInConstructor() {
        try {
            final String LONG_VALUE = "value".repeat(lengthThatWillFail);
            // When
            new IsoValue<>(type, LONG_VALUE);
            Assert.fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            // Then
            Assert.assertEquals(expected, e.getMessage());
        }
    }

}
