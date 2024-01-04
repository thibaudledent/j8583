package com.solab.iso8583;

import jakarta.xml.bind.DatatypeConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.ParseException;

class TestNonRegression {
    @Test
    void testNonRegression1() throws IOException, ParseException {
        MessageFactory<IsoMessage> messageFactory = new MessageFactory<>();
        messageFactory.setConfigPath("nonregression1.xml");
        messageFactory.setUseBinaryMessages(true);

        byte[] binaryMessage = DatatypeConverter.parseHexBinary("0100723C448108E39B2010522660990002629000000000000000010010261250330000001250331026200445110911000B0100000000013030303030303030303030303030303030303030373938303739303120202020202020414E5346444D5C5C20202020202020202020202020202020202020202020202020202020202046523C3330303131333130313133333034323030363936313430303030303030303030303030313937303830303030303030304130303830303030303030311600010AFFFF000301024543434600020601FFFF0003010978080C084927B16C412C00000401010000004900820200000095050000000000009A03200217009C01009F02060000000012179F03060000000000009F3303E000C89F34030000009F3501229F3602003D9F3704C65E2CECDF81010342000301040011010000130101002001010022013100270F37393830373930313030303030303000280101003707202110261250330038060000000001005F2D02656E3501010216710102012102000110020106000000000000020204079807900203020001020706000000000100020B07A0000000422010");

        IsoMessage iso2 = messageFactory.parseMessage(binaryMessage, 0);
        String expectedField47Value = "300113101133042006961400000000000001970800000000A00800000001";
        String decodedField47Value = iso2.getField(47).toString();
        String expectedField48Value = "00010AFFFF000301024543434600020601FFFF000301";
        String decodedField48Value = iso2.getField(48).toString();
        String expectedField49Value = "978";
        String decodedField49Value = iso2.getField(49).toString();
        String expectedField52Value = "0C084927B16C412C";
        String decodedField52Value = iso2.getField(52).toString();
        String expectedField53Value = "0000040101000000";
        String decodedField53Value = iso2.getField(53).toString();

        Assertions.assertEquals(expectedField47Value, decodedField47Value);
        Assertions.assertEquals(expectedField48Value, decodedField48Value);
        Assertions.assertEquals(expectedField49Value, decodedField49Value);
        Assertions.assertEquals(expectedField52Value, decodedField52Value);
        Assertions.assertEquals(expectedField53Value, decodedField53Value);
    }
}
