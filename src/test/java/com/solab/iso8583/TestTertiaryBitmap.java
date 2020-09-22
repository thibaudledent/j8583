package com.solab.iso8583;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

public class TestTertiaryBitmap {

    static int MESSAGE_WITH_NORMAL_FIELD_65 = 0x100;
    static int MESSAGE_WITH_MISSING_FIELD_GUIDES_FOR_EXTENDED_FIELDS = 0x101;
    static int MESSAGE_TYPE_SIMPLE_TERTIARY_ASCII = 0x200;
    static int MESSAGE_TYPE_SIMPLE_TERTIARY_BIN = 0x201;
    MessageFactory<IsoMessage> messageFactory;

    @Before
    public void setup() throws IOException {
        messageFactory = new MessageFactory<>();
        messageFactory.setConfigPath("tertiarybitmap.xml");
    }

    @Test
    public void shouldInterpretField65AsOrdinaryFieldWhenBitmapInactive() throws UnsupportedEncodingException, ParseException {
        // Given
        messageFactory.setUseTertiaryBitmap(false);

        // When
        IsoMessage messageWithoutTertiaryBitmap = messageFactory.newMessage(MESSAGE_WITH_NORMAL_FIELD_65);

        // Then
        Assert.assertEquals("Custom Field 65", messageWithoutTertiaryBitmap.getField(65).getValue());

        // verifying deserialization/serialization consistency...
        // When
        byte[] serializedMessage = messageWithoutTertiaryBitmap.writeData();
        IsoMessage reParsedMessage = messageFactory.parseMessage(serializedMessage, 0);

        // Then
        Assert.assertEquals("Custom Field 65", reParsedMessage.getField(65).getValue());
    }

    @Test
    public void shouldThrowExceptionWhenExtendedFieldsNotPresent() throws UnsupportedEncodingException {
        // Given
        messageFactory.setUseTertiaryBitmap(true);
        messageFactory.setUseBinaryMessages(true);
        byte[] serializedMessageWithField65 = messageFactory.newMessage(MESSAGE_WITH_MISSING_FIELD_GUIDES_FOR_EXTENDED_FIELDS).writeData();

        // When
        try {
            IsoMessage reParsedMessage = messageFactory.parseMessage(serializedMessageWithField65, 0);
            Assert.fail();
        } catch (ParseException e){
            // Then (checking the exact non-parsable fields (129,192) is not feasable, as this info is only logged. You can manually verify the output though)
            Assert.assertEquals("ISO8583 MessageFactory cannot parse fields", e.getMessage());
        }
    }

    @Test
    public void shouldProperlyDeserializeExtendedFieldsAscii() throws UnsupportedEncodingException, ParseException {
        // Given
        messageFactory.setUseBinaryMessages(false);
        messageFactory.setUseTertiaryBitmap(true);

        // When
        IsoMessage messageWithExtendedFields = messageFactory.newMessage(MESSAGE_TYPE_SIMPLE_TERTIARY_ASCII);

        // Then
        Assert.assertEquals("2222", messageWithExtendedFields.getField(2).getValue());
        Assert.assertEquals("0123456789ABC", messageWithExtendedFields.getObjectValue(64));
        Assert.assertEquals("129", messageWithExtendedFields.getField(129).getValue());
        Assert.assertEquals("192", messageWithExtendedFields.getField(192).getValue());

        // verifying deserialization/serialization consistency...
        // When
        byte[] serializedMessage = messageWithExtendedFields.writeData();
        IsoMessage reParsedMessage = messageFactory.parseMessage(serializedMessage, 0);

        // Then
        Assert.assertEquals("2222", reParsedMessage.getField(2).getValue());
        Assert.assertEquals("0123456789ABC", reParsedMessage.getObjectValue(64));
        Assert.assertEquals("8000000000000001", DatatypeConverter.printHexBinary(reParsedMessage.getObjectValue(65)));
        Assert.assertEquals("129", reParsedMessage.getField(129).getValue());
        Assert.assertEquals("192", reParsedMessage.getField(192).getValue());
    }

    @Test
    public void shouldProperlyDeserializeExtendedFieldsBin() throws UnsupportedEncodingException, ParseException {
        // Given
        messageFactory.setCharacterEncoding("UTF-8");
        messageFactory.setUseBinaryMessages(true);
        messageFactory.setUseTertiaryBitmap(true);

        // When
        IsoMessage messageWithExtendedFields = messageFactory.newMessage(MESSAGE_TYPE_SIMPLE_TERTIARY_BIN);

        // Then
        Assert.assertEquals("2222", messageWithExtendedFields.getField(2).getValue());
        Assert.assertEquals("0123456789ABC", messageWithExtendedFields.getObjectValue(64));
        Assert.assertEquals("129", messageWithExtendedFields.getField(129).getValue());
        Assert.assertEquals("192", messageWithExtendedFields.getField(192).getValue());

        // verifying deserialization/serialization consistency...
        // When
        byte[] serializedMessage = messageWithExtendedFields.writeData();
        IsoMessage reParsedMessage = messageFactory.parseMessage(serializedMessage, 0);

        // Then
        Assert.assertEquals((long) 2222, reParsedMessage.getField(2).getValue());
        Assert.assertEquals("0123456789ABC", reParsedMessage.getObjectValue(64));
        Assert.assertEquals("8000000000000001", DatatypeConverter.printHexBinary(reParsedMessage.getObjectValue(65)));
        Assert.assertEquals("129", reParsedMessage.getField(129).getValue());
        Assert.assertEquals("192", reParsedMessage.getField(192).getValue());
    }

}
