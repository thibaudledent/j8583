package com.solab.iso8583.parse;

/**
 * Custom class to parse fields of type LLBCDBIN with BCD length.
 */
public class BcdLengthLlbinParseInfo extends LlbinParseInfo {

    @Override
    protected int getLengthForBinaryParsing(byte b) {
        final int length = super.getLengthForBinaryParsing(b);
        return length % 2 == 0 ? length / 2 : (length / 2) + 1;
    }

}
