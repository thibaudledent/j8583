package com.solab.iso8583;

import com.solab.iso8583.parse.ConfigParser;
import com.solab.iso8583.util.HexCodec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.ParseException;

class TestBcdLengthLlbin {

    @Test
    void shouldSerializeAndDeserializeWithBcdBin() throws IOException, ParseException {
        // Given
        final String expectedHexMessage = "11006D0005800000000116123456789011121300000005012345000501234500050123453132333435360020123456789000000000000128123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781111111111111111";
        final MessageFactory<IsoMessage> mf = ConfigParser.createDefault();
        mf.setUseBinaryBitmap(true);
        mf.setUseBinaryMessages(true);

        final IsoMessage isoMessage1 = mf.newMessage(0x1100);

        isoMessage1.setField(2, new IsoValue<>(IsoType.LLBCDBIN, "1234567890111213"));
        isoMessage1.setField(3, new IsoValue<>(IsoType.NUMERIC, "000000", "000000".length()));
        //Testing that odd-length values are actually left-padded
        isoMessage1.setField(5, new IsoValue<>(IsoType.LLBCDBIN, "12345"));
        isoMessage1.setField(6, new IsoValue<>(IsoType.LLLBCDBIN, "12345"));
        isoMessage1.setField(8, new IsoValue<>(IsoType.LLLLBCDBIN, "12345"));
        isoMessage1.setField(22, new IsoValue<>(IsoType.ALPHA, "123456", "123456".length()));
        isoMessage1.setField(24, new IsoValue<>(IsoType.LLLLBCDBIN, "12345678900000000000"));
        isoMessage1.setField(25, new IsoValue<>(IsoType.LLLBCDBIN, "12345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678"));
        isoMessage1.setField(64, new IsoValue<>(IsoType.BINARY, "1111111111111111", 8));

        // When - Serialize
        final byte[] message1 = isoMessage1.writeData();

        // Then
        Assertions.assertEquals(expectedHexMessage, HexCodec.hexEncode(message1, 0, message1.length));

        // When - Deserialize
        mf.setConfigPath("llbcdbin.xml");
        final IsoMessage isoMessage2 = mf.parseMessage(message1, 0);

        // Then
        Assertions.assertEquals("LLBCDBIN", isoMessage2.getField(2).getType().name());
        Assertions.assertEquals("1234567890111213", isoMessage2.getField(2).toString());
        Assertions.assertEquals("000000", isoMessage2.getField(3).toString());
        Assertions.assertEquals("12345", isoMessage2.getField(5).toString());
        Assertions.assertEquals("12345", isoMessage2.getField(6).toString());
        Assertions.assertEquals("12345", isoMessage2.getField(8).toString());
        Assertions.assertEquals("123456", isoMessage2.getField(22).toString());
        Assertions.assertEquals("LLLLBCDBIN", isoMessage2.getField(24).getType().name());
        Assertions.assertEquals("12345678900000000000", isoMessage2.getField(24).toString());
        Assertions.assertEquals("LLLBCDBIN", isoMessage2.getField(25).getType().name());
        Assertions.assertEquals("12345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678", isoMessage2.getField(25).toString());
        Assertions.assertEquals("1111111111111111", isoMessage2.getField(64).toString());

        // When - Serialize again
        final byte[] message2 = isoMessage2.writeData();

        // Then
        Assertions.assertEquals(expectedHexMessage, HexCodec.hexEncode(message2, 0, message2.length));
    }

    @Test
    void shouldSerializeAndDeserializeWithBcdBinAndOddLength() throws IOException, ParseException {
        // Given
        final String expectedHexMessage = "1100600005800000000117012345678901234567000000313233343536002101123456789000000000000109001234567891234567891234567891234567891234567891234567891234567891234567891234567891234567891234567891234567891111111111111111";
        final MessageFactory<IsoMessage> mf = ConfigParser.createDefault();
        mf.setUseBinaryBitmap(true);
        mf.setUseBinaryMessages(true);

        final IsoMessage isoMessage1 = mf.newMessage(0x1100);

        // For the LL fields, we use odd lengths in this test
        isoMessage1.setField(2, new IsoValue<>(IsoType.LLBCDBIN, "12345678901234567"));
        isoMessage1.setField(3, new IsoValue<>(IsoType.NUMERIC, "000000", "000000".length()));
        isoMessage1.setField(22, new IsoValue<>(IsoType.ALPHA, "123456", "123456".length()));
        isoMessage1.setField(24, new IsoValue<>(IsoType.LLLLBCDBIN, "112345678900000000000"));
        isoMessage1.setField(25, new IsoValue<>(IsoType.LLLBCDBIN, "0123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789"));
        isoMessage1.setField(64, new IsoValue<>(IsoType.BINARY, "1111111111111111", 8));

        // When - Serialize
        final byte[] message1 = isoMessage1.writeData();

        // Then
        Assertions.assertEquals(expectedHexMessage, HexCodec.hexEncode(message1, 0, message1.length));

        // When - Deserialize
        mf.setConfigPath("llbcdbin.xml");
        final IsoMessage isoMessage2 = mf.parseMessage(message1, 0);

        // Then
        Assertions.assertEquals("LLBCDBIN", isoMessage2.getField(2).getType().name());
        Assertions.assertEquals("12345678901234567", isoMessage2.getField(2).toString());
        Assertions.assertEquals("000000", isoMessage2.getField(3).toString());
        Assertions.assertEquals("123456", isoMessage2.getField(22).toString());
        Assertions.assertEquals("LLLLBCDBIN", isoMessage2.getField(24).getType().name());
        Assertions.assertEquals("112345678900000000000", isoMessage2.getField(24).toString());
        Assertions.assertEquals("LLLBCDBIN", isoMessage2.getField(25).getType().name());
        Assertions.assertEquals("0123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789", isoMessage2.getField(25).toString());
        Assertions.assertEquals("1111111111111111", isoMessage2.getField(64).toString());

        // When - Serialize again
        final byte[] message2 = isoMessage2.writeData();

        // Then
        Assertions.assertEquals(expectedHexMessage, HexCodec.hexEncode(message2, 0, message2.length));
    }

    @Test
    void shouldReturnOriginalLLBCDBINValue() throws IOException, ParseException {
        // Given
        final MessageFactory<IsoMessage> mf = ConfigParser.createDefault();
        mf.setUseBinaryBitmap(true);
        mf.setUseBinaryMessages(true);

        final IsoMessage isoMessage = mf.newMessage(0x1100);
        isoMessage.setField(2, new IsoValue<>(IsoType.LLBCDBIN, "012345"));

        byte[] buf = isoMessage.writeData();
        mf.setConfigPath("llbcdbin.xml");
        // When
        final IsoMessage m2 = mf.parseMessage(buf, 0);
        final IsoValue<Object> field = m2.getField(2);

        // Then
        Assertions.assertEquals("012345", field.toString());
    }

}
