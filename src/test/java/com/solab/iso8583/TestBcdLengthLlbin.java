package com.solab.iso8583;

import com.solab.iso8583.parse.ConfigParser;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.text.ParseException;

public class TestBcdLengthLlbin {

    @Test
    public void shouldSerializeAndDeserializeWithBcdBin() throws IOException, ParseException {
        // Given
        final MessageFactory mf = ConfigParser.createDefault();
        mf.setUseBinaryBitmap(true);
        mf.setUseBinaryMessages(true);

        final IsoMessage msg = mf.newMessage(0x1100);

        msg.setField(2, new IsoValue(IsoType.LLBCDBIN, "1234567890111213"));
        msg.setField(3, new IsoValue(IsoType.NUMERIC, "000000", "000000".length()));
        msg.setField(22, new IsoValue(IsoType.ALPHA, "123456", "123456".length()));
        msg.setField(24, new IsoValue(IsoType.LLLLBCDBIN, "12345678900000000000"));
        msg.setField(64, new IsoValue(IsoType.BINARY, "0101010101010101", 8));

        // When - Serialization
        final byte[] message = msg.writeData();

        // Then
        Assert.assertEquals("110060000500000000011612345678901112130000003132333435360020123456789000000000000101010101010101", DatatypeConverter.printHexBinary(message));

        // When - Deserialization
        mf.setConfigPath("llbcdbin.xml");
        final IsoMessage isoMessage = mf.parseMessage(message, 0);

        // Then
        Assert.assertEquals("1234567890111213", isoMessage.getField(2).toString());
        Assert.assertEquals("000000", isoMessage.getField(3).toString());
        Assert.assertEquals("123456", isoMessage.getField(22).toString());
        Assert.assertEquals("12345678900000000000", isoMessage.getField(24).toString());
        Assert.assertEquals("0101010101010101", isoMessage.getField(64).toString());
    }

}
