package com.solab.iso8583.parse;

import com.solab.iso8583.IsoType;

/**
 * Custom parser for fields of type {@link IsoType#LLLLBINLENGTHNUM}.
 */
public class BinLengthLlllNumParseInfo extends LlllbinParseInfo {

    /**
     * Instantiates a new Bin length llll num parse info.
     */
    public BinLengthLlllNumParseInfo() {
        super(IsoType.LLLLBINLENGTHNUM, 0);
    }

    @Override
    protected int getLengthForBinaryParsing(final byte[] buf, final int pos) {
        int intLength = getFieldLength(buf, pos);
        return intLength / 2 + intLength % 2;
    }

    @Override
    protected int getFieldLength(byte[] buf, int pos) {
        return ((buf[pos] & 0xff) << 8) | (buf[pos + 1] & 0xff);
    }

}
