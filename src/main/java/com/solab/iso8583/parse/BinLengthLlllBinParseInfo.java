package com.solab.iso8583.parse;

import com.solab.iso8583.IsoType;

/**
 * Custom parser for fields of type {@link IsoType#LLLLBINLENGTHBIN}.
 */
public class BinLengthLlllBinParseInfo extends LlllbinParseInfo {

    /**
     * Instantiates a new Bcd length llllbin parse info.
     */
    public BinLengthLlllBinParseInfo() {
        super(IsoType.LLLLBINLENGTHBIN, 0);
    }

    @Override
    protected int getLengthForBinaryParsing(final byte[] buf, final int pos) {
        return getFieldLength(buf, pos);
    }

    @Override
    protected int getFieldLength(byte[] buf, int pos) {
        return ((buf[pos] & 0xff) << 8) | (buf[pos + 1] & 0xff);
    }

}
