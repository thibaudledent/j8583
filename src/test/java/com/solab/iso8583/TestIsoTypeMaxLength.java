package com.solab.iso8583;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;

class TestIsoTypeMaxLength {

    private static Collection<Object[]> data() {
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
            {IsoType.LLLLBINLENGTHALPHANUM, "LLLLBINLENGTHALPHANUM can only hold values up to 255 chars", 256},
            {IsoType.LLBINLENGTHBIN, "LLBINLENGTHBIN can only hold values up to 510 chars", 511},
            {IsoType.LLLLBINLENGTHBIN, "LLLLBINLENGTHBIN can only hold values up to 65535 chars", 65536},
            {IsoType.LLBCDLENGTHALPHANUM, "LLBCDLENGTHALPHANUM can only hold values up to 99 chars", 100},
        });
    }

    @ParameterizedTest(name = "type={0}, expected={1}, lengthThatWillFail={2}")
    @MethodSource("data")
    void shouldThrowIllegalArgumentExceptionWhenUsingLengthInConstructor(final IsoType type, final String expected, final int lengthThatWillFail) {
        try {
            // When
            new IsoValue<>(type, "value", lengthThatWillFail);
            Assertions.fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            // Then
            Assertions.assertEquals(expected, e.getMessage());
        }
    }

    @ParameterizedTest(name = "type={0}, expected={1}, lengthThatWillFail={2}")
    @MethodSource("data")
    void shouldThrowIllegalArgumentExceptionWithoutUsingLengthInConstructor(final IsoType type, final String expected, final int lengthThatWillFail) {
        final String LONG_VALUE = "value".repeat(lengthThatWillFail);
        try {
            // When
            new IsoValue<>(type, LONG_VALUE);
            Assertions.fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException e) {
            // Then
            Assertions.assertEquals(expected, e.getMessage());
        }
    }

}
