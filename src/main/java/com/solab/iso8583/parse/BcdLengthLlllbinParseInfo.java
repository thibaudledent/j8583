package com.solab.iso8583.parse;

/**
 * Custom class to parse fields of type LLLLBCDBIN with BCD length.
 */
public class BcdLengthLlllbinParseInfo extends LlllbinParseInfo {

    @Override
    protected int getLengthForBinaryParsing(byte[] buf, int pos) {
        return super.getLengthForBinaryParsing(buf, pos) / 2;
    }

}
