package com.solab.iso8583.parse;

/**
 * Custom class to parse fields of type LLLBCDBIN with BCD length.
 */
public class BcdLengthLllbinParseInfo extends LllbinParseInfo {

    @Override
    protected int getLengthForBinaryParsing(byte[] buf, int pos) {
        final int length = super.getLengthForBinaryParsing(buf, pos);
        return length % 2 == 0 ? length / 2 : (length / 2) + 1;
    }

}
