package com.solab.iso8583.parse;

import com.solab.iso8583.IsoType;

/**
 * Custom parser for fields of type {@link IsoType#LLBINLENGTHBIN}.
 */
public class BinLengthLlBinParseInfo extends LlbinParseInfo {

    /**
     * Instantiates a new Bin length ll bin parse info.
     */
    public BinLengthLlBinParseInfo() {
        super(IsoType.LLBINLENGTHBIN, 0);
    }

    @Override
    protected int getFieldLength(byte b) {
        return Byte.toUnsignedInt(b);
    }
}
