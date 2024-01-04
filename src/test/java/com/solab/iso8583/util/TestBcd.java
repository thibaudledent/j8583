package com.solab.iso8583.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

/**
 * BCD encoding tests.
 *
 * @author Enrique Zamudio
 *         Date: 25/11/13 10:43
 */
class TestBcd {

    @Test
    void testEncoding() {
        byte[] buf = new byte[2];
        buf[0] = 1; buf[1]=1;
        Bcd.encode("00", buf);
        Assertions.assertArrayEquals(new byte[]{0,         1}, buf);
        Bcd.encode("79", buf);
        Assertions.assertArrayEquals(new byte[]{0x79,      1}, buf);
        Bcd.encode("80", buf);
        Assertions.assertArrayEquals(new byte[]{(byte)0x80,1}, buf);
        Bcd.encode("99", buf);
        Assertions.assertArrayEquals(new byte[]{(byte)0x99,1}, buf);
        Bcd.encode("100", buf);
        Assertions.assertArrayEquals(new byte[]{1,         0}, buf);
        Bcd.encode("779", buf);
        Assertions.assertArrayEquals(new byte[]{7,      0x79}, buf);
        Bcd.encode("999", buf);
        Assertions.assertArrayEquals(new byte[]{9,(byte)0x99}, buf);
        Bcd.encodeRightPadded("999", buf);
        Assertions.assertArrayEquals(new byte[]{(byte)0x99,(byte)0x9f}, buf);
    }

    @Test
    void testDecoding() {
        byte[] buf = new byte[2];
        Assertions.assertEquals(0, Bcd.decodeToLong(buf, 0, 1));
        Assertions.assertEquals(0, Bcd.decodeToLong(buf, 0, 2));
        Assertions.assertEquals(0, Bcd.decodeToLong(buf, 0, 3));
        Assertions.assertEquals(0, Bcd.decodeToLong(buf, 0, 4));
        buf[0]=0x79;
        Assertions.assertEquals(79, Bcd.decodeToLong(buf, 0, 2));
        buf[0]=(byte)0x80;
        Assertions.assertEquals(80, Bcd.decodeToLong(buf, 0, 2));
        buf[0]=(byte)0x99;
        Assertions.assertEquals(99, Bcd.decodeToLong(buf, 0, 2));
        buf[0]=1;
        Assertions.assertEquals(100, Bcd.decodeToLong(buf,0,4));
        buf[1]=0x79;
        Assertions.assertEquals(179, Bcd.decodeToLong(buf,0,4));
        buf[1]=(byte)0x99;
        Assertions.assertEquals(199, Bcd.decodeToLong(buf,0,4));
        buf[0]=9;
        Assertions.assertEquals(999, Bcd.decodeToLong(buf,0,4));
        Assertions.assertEquals(new BigInteger("999"), Bcd.decodeToBigInteger(buf,0,3));
        buf[0] = (byte)0x99;
        buf[1] = (byte)0x9f;
        Assertions.assertEquals(999, Bcd.decodeRightPaddedToLong(buf,0,4));
        Assertions.assertEquals(new BigInteger("999"), Bcd.decodeRightPaddedToBigInteger(buf,0,4));
    }

}
