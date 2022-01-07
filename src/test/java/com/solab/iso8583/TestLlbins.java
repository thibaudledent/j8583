package com.solab.iso8583;

import com.solab.iso8583.parse.ConfigParser;
import com.solab.iso8583.util.HexCodec;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;

public class TestLlbins {

    @Test
    public void lbinsDontPadOddLengthValues() throws IOException, ParseException {
        // Given
        final String expectedHexMessage = "11006D0005800000000108123456789011121300000003012345000301234500030123453132333435360010123456789000000000000064123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781111111111111111";
        final MessageFactory<IsoMessage> mf = ConfigParser.createDefault();
        mf.setUseBinaryBitmap(true);
        mf.setUseBinaryMessages(true);

        final IsoMessage isoMessage1 = mf.newMessage(0x1100);

        isoMessage1.setField(2, new IsoValue<>(IsoType.LLBIN, "1234567890111213"));
        isoMessage1.setField(3, new IsoValue<>(IsoType.NUMERIC, "000000", "000000".length()));
        //Testing that odd-length values are actually left-padded
        isoMessage1.setField(5, new IsoValue<>(IsoType.LLBIN, "12345"));
        isoMessage1.setField(6, new IsoValue<>(IsoType.LLLBIN, "12345"));
        isoMessage1.setField(8, new IsoValue<>(IsoType.LLLLBIN, "12345"));
        isoMessage1.setField(22, new IsoValue<>(IsoType.ALPHA, "123456", "123456".length()));
        isoMessage1.setField(24, new IsoValue<>(IsoType.LLLLBIN, "12345678900000000000"));
        isoMessage1.setField(25, new IsoValue<>(IsoType.LLLBIN, "12345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678"));
        isoMessage1.setField(64, new IsoValue<>(IsoType.BINARY, "1111111111111111", 8));

        // When - Serialize
        final byte[] message1 = isoMessage1.writeData();

        // Then
        Assert.assertEquals(expectedHexMessage, HexCodec.hexEncode(message1, 0, message1.length));

        // When - Deserialize
        mf.setConfigPath("llbin.xml");
        final IsoMessage isoMessage2 = mf.parseMessage(message1, 0);

        // Then
        Assert.assertEquals("LLBIN", isoMessage2.getField(2).getType().name());
        Assert.assertEquals("1234567890111213", isoMessage2.getField(2).toString());
        Assert.assertEquals("000000", isoMessage2.getField(3).toString());
        Assert.assertEquals("012345", isoMessage2.getField(5).toString());
        Assert.assertEquals("012345", isoMessage2.getField(6).toString());
        Assert.assertEquals("012345", isoMessage2.getField(8).toString());
        Assert.assertEquals("123456", isoMessage2.getField(22).toString());
        Assert.assertEquals("LLLLBIN", isoMessage2.getField(24).getType().name());
        Assert.assertEquals("12345678900000000000", isoMessage2.getField(24).toString());
        Assert.assertEquals("LLLBIN", isoMessage2.getField(25).getType().name());
        Assert.assertEquals("12345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678", isoMessage2.getField(25).toString());
        Assert.assertEquals("1111111111111111", isoMessage2.getField(64).toString());

        // When - Serialize again
        final byte[] message2 = isoMessage2.writeData();

        // Then
        Assert.assertEquals(expectedHexMessage, HexCodec.hexEncode(message2, 0, message2.length));
    }
}
