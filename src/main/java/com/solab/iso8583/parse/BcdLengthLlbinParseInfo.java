package com.solab.iso8583.parse;

/**
 * Custom class to parse fields of type LLBCDBIN with BCD length.
 */
public class BcdLengthLlbinParseInfo extends LlbinParseInfo {

    @Override
    protected int getLengthForBinaryParsing(byte b) {
        return super.getLengthForBinaryParsing(b) / 2;
    }

}
