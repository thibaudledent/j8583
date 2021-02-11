package com.solab.iso8583.parse;

import com.solab.iso8583.IsoType;

/**
 * Custom parser for fields of type {@link IsoType#LLBINLENGTHNUM}.
 */
public class BinLengthLlNumParseInfo extends LlbinParseInfo {

    public BinLengthLlNumParseInfo() {
        super(IsoType.LLBINLENGTHNUM, 0);
    }

    @Override
    protected int getLengthForBinaryParsing(byte b) {
        int intLength = Byte.toUnsignedInt(b);
        return intLength / 2 + intLength % 2;
    }

    @Override
    protected int getFieldLength(byte b) {
        return Byte.toUnsignedInt(b);
    }
}
