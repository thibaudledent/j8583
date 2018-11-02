package com.solab.iso8583.parse;

/**
 * Custom class to parse fields of type LLLBCDBIN with BCD length.
 */
public class BcdLengthLllbinParseInfo extends LllbinParseInfo {

    @Override
    protected int getLengthForBinaryParsing(byte[] buf, int pos) {
        return super.getLengthForBinaryParsing(buf, pos) / 2;
    }

}
