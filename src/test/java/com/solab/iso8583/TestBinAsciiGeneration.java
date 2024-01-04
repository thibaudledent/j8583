package com.solab.iso8583;

import com.solab.iso8583.parse.ConfigParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

class TestBinAsciiGeneration {
    @Test
    void generateBinAscii800() throws Exception{
        MessageFactory<IsoMessage> mf = ConfigParser.createDefault();
        IsoMessage msg = mf.newMessage(0x800);
        Assertions.assertFalse(msg.isBinaryHeader(), "Bin Header should default to false");
        Assertions.assertFalse(msg.isBinaryFields(), "Bin Fields should default to false");

        msg.setBinaryHeader(true);

        msg.setField(7,new IsoValue<>(IsoType.DATE10, "0322220001"));
        msg.setField(11,new IsoValue<>(IsoType.NUMERIC, 562040,6));
        msg.setField(37,new IsoValue<>(IsoType.ALPHA, 562040,12));
        msg.setField(53,new IsoValue<>(IsoType.LLVAR,  "0001000000"));
        msg.setField(70,new IsoValue<>(IsoType.NUMERIC, 1,3));

        byte[] actual = msg.writeData();

        byte[] expected = TestBinAsciiParsing.loadData("bin_ascii_800.bin");

        Assertions.assertArrayEquals(expected,actual);
    }

}
