package com.solab.iso8583;

import com.solab.iso8583.parse.ConfigParser;
import org.junit.Assert;
import org.junit.Test;

import jakarta.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.text.ParseException;

public class TestBinaryForceStringEncodingLengthLlvar {

    @Test
    public void shouldSerializeAndDeserializeWithForceStringEncodingBinaryLLvar() throws IOException, ParseException {
        // Given
        final String expectedHexMessage = "1100600005800000000131363132333435363738393031313132313300000031323334353630303230313233343536373839303030303030303030303031323831323334353637383132333435363738313233343536373831323334353637383132333435363738313233343536373831323334353637383132333435363738313233343536373831323334353637383132333435363738313233343536373831323334353637383132333435363738313233343536373831323334353637381111111111111111";
        final MessageFactory mf = ConfigParser.createDefault();
        mf.setUseBinaryBitmap(true);
        mf.setUseBinaryMessages(true);
        mf.setForceStringEncoding(true);

        final IsoMessage isoMessage1 = mf.newMessage(0x1100);

        isoMessage1.setField(2, new IsoValue<>(IsoType.LLVAR, "1234567890111213"));
        isoMessage1.setField(3, new IsoValue<>(IsoType.NUMERIC, "000000", "000000".length()));
        isoMessage1.setField(22, new IsoValue<>(IsoType.ALPHA, "123456", "123456".length()));
        isoMessage1.setField(24, new IsoValue<>(IsoType.LLLLVAR, "12345678900000000000"));
        isoMessage1.setField(25, new IsoValue<>(IsoType.LLLVAR, "12345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678"));
        isoMessage1.setField(64, new IsoValue<>(IsoType.BINARY, "1111111111111111", 8));

        // When - Serialize
        final byte[] message1 = isoMessage1.writeData();

        // Then
        Assert.assertEquals(expectedHexMessage, DatatypeConverter.printHexBinary(message1));

        // When - Deserialize
        mf.setConfigPath("llvar.xml");
        final IsoMessage isoMessage2 = mf.parseMessage(message1, 0);

        // Then
        Assert.assertEquals("LLVAR", isoMessage2.getField(2).getType().name());
        Assert.assertEquals("1234567890111213", isoMessage2.getField(2).toString());
        Assert.assertEquals("000000", isoMessage2.getField(3).toString());
        Assert.assertEquals("123456", isoMessage2.getField(22).toString());
        Assert.assertEquals("LLLLVAR", isoMessage2.getField(24).getType().name());
        Assert.assertEquals("12345678900000000000", isoMessage2.getField(24).toString());
        Assert.assertEquals("LLLVAR", isoMessage2.getField(25).getType().name());
        Assert.assertEquals("12345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678123456781234567812345678", isoMessage2.getField(25).toString());
        Assert.assertEquals("1111111111111111", isoMessage2.getField(64).toString());

        // When - Serialize again
        final byte[] message2 = isoMessage2.writeData();

        // Then
        Assert.assertEquals(expectedHexMessage, DatatypeConverter.printHexBinary(message2));
    }

    @Test
    public void shouldSerializeAndDeserializeWithForceStringEncodingBinaryLLvarAndOddLength() throws IOException, ParseException {
        // Given
        final String expectedHexMessage = "110060000580000000013137313233343536373839303132333435363700000031323334353630303231313132333435363738393030303030303030303030313039303132333435363738393132333435363738393132333435363738393132333435363738393132333435363738393132333435363738393132333435363738393132333435363738393132333435363738393132333435363738393132333435363738393132333435363738391111111111111111";
        final MessageFactory mf = ConfigParser.createDefault();
        mf.setUseBinaryBitmap(true);
        mf.setUseBinaryMessages(true);
        mf.setForceStringEncoding(true);

        final IsoMessage isoMessage1 = mf.newMessage(0x1100);

        // For the LL fields, we use odd lengths in this test
        isoMessage1.setField(2, new IsoValue<>(IsoType.LLVAR, "12345678901234567"));
        isoMessage1.setField(3, new IsoValue<>(IsoType.NUMERIC, "000000", "000000".length()));
        isoMessage1.setField(22, new IsoValue<>(IsoType.ALPHA, "123456", "123456".length()));
        isoMessage1.setField(24, new IsoValue<>(IsoType.LLLLVAR, "112345678900000000000"));
        isoMessage1.setField(25, new IsoValue<>(IsoType.LLLVAR, "0123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789"));
        isoMessage1.setField(64, new IsoValue<>(IsoType.BINARY, "1111111111111111", 8));

        // When - Serialize
        final byte[] message1 = isoMessage1.writeData();

        // Then
        Assert.assertEquals(expectedHexMessage, DatatypeConverter.printHexBinary(message1));

        // When - Deserialize
        mf.setConfigPath("llvar.xml");
        final IsoMessage isoMessage2 = mf.parseMessage(message1, 0);

        // Then
        Assert.assertEquals("LLVAR", isoMessage2.getField(2).getType().name());
        Assert.assertEquals("12345678901234567", isoMessage2.getField(2).toString());
        Assert.assertEquals("000000", isoMessage2.getField(3).toString());
        Assert.assertEquals("123456", isoMessage2.getField(22).toString());
        Assert.assertEquals("LLLLVAR", isoMessage2.getField(24).getType().name());
        Assert.assertEquals("112345678900000000000", isoMessage2.getField(24).toString());
        Assert.assertEquals("LLLVAR", isoMessage2.getField(25).getType().name());
        Assert.assertEquals("0123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789123456789", isoMessage2.getField(25).toString());
        Assert.assertEquals("1111111111111111", isoMessage2.getField(64).toString());

        // When - Serialize again
        final byte[] message2 = isoMessage2.writeData();

        // Then
        Assert.assertEquals(expectedHexMessage, DatatypeConverter.printHexBinary(message2));
    }

    @Test
    public void shouldReturnOriginalLLVarValue() throws IOException {
        // Given
        final MessageFactory mf = ConfigParser.createDefault();
        mf.setUseBinaryBitmap(true);
        mf.setUseBinaryMessages(true);
        mf.setForceStringEncoding(true);

        final IsoMessage isoMessage = mf.newMessage(0x1100);
        isoMessage.setField(2, new IsoValue<>(IsoType.LLVAR, "012345"));

        // When
        final IsoValue<Object> field = isoMessage.getField(2);

        // Then
        Assert.assertEquals("012345", field.toString());
    }

}
