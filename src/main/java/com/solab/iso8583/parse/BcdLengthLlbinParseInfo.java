package com.solab.iso8583.parse;

/**
 * Custom class to parse fields of type LLBCDBIN with BCD length.
 */
public class BcdLengthLlbinParseInfo extends LlbinParseInfo {

    public BcdLengthLlbinParseInfo() {
        super();
    }

    @Override
    protected int getLengthForBinaryParsing(final byte b) {
        return super.getLengthForBinaryParsing(b) / 2;
    }
}
