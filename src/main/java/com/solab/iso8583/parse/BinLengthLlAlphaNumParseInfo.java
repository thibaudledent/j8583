package com.solab.iso8583.parse;

import com.solab.iso8583.IsoType;

/**
 * Custom parser for fields of type {@link IsoType#LLBINLENGTHALPHANUM}.
 */
public class BinLengthLlAlphaNumParseInfo extends LlvarParseInfo {

    /**
     * Instantiates a new Bin length ll alpha num parse info.
     */
    public BinLengthLlAlphaNumParseInfo() {
        super(IsoType.LLBINLENGTHALPHANUM, 0);
    }

    @Override
    protected int getFieldLength(final byte b) {
        return Byte.toUnsignedInt(b);
    }
}
