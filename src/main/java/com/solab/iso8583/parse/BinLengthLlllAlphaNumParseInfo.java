package com.solab.iso8583.parse;

import com.solab.iso8583.IsoType;

/**
 * Custom parser for fields of type {@link IsoType#LLLLBINLENGTHALPHANUM}.
 */
public class BinLengthLlllAlphaNumParseInfo extends LlllvarParseInfo {

    /**
     * Instantiates a new Bin length llll alpha num parse info.
     */

    public BinLengthLlllAlphaNumParseInfo() {
        super(IsoType.LLLLBINLENGTHALPHANUM, 0);
    }

}
