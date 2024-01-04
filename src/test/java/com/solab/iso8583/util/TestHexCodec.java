package com.solab.iso8583.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestHexCodec {

	public void encodeDecode(String hex) {
		byte[] buf = HexCodec.hexDecode(hex);
		Assertions.assertEquals((hex.length() / 2) + (hex.length() % 2), buf.length);
		String reenc = HexCodec.hexEncode(buf, 0, buf.length);
		if (reenc.startsWith("0") && !hex.startsWith("0")) {
			Assertions.assertEquals(reenc.substring(1), hex);
		} else {
			Assertions.assertEquals(hex, reenc);
		}
	}

	@Test
	void testCodec() {
		byte[] buf = HexCodec.hexDecode("A");
		Assertions.assertEquals(0x0a, buf[0]);
		encodeDecode("A");
		encodeDecode("0123456789ABCDEF");
		buf = HexCodec.hexDecode("0123456789ABCDEF");
		Assertions.assertEquals(1, buf[0]);
		Assertions.assertEquals(0x23, buf[1]);
		Assertions.assertEquals(0x45, buf[2]);
		Assertions.assertEquals(0x67, buf[3]);
		Assertions.assertEquals(0x89, (buf[4] & 0xff));
		Assertions.assertEquals(0xab, (buf[5] & 0xff));
		Assertions.assertEquals(0xcd, (buf[6] & 0xff));
		Assertions.assertEquals(0xef, (buf[7] & 0xff));
		buf = HexCodec.hexDecode("ABC");
		Assertions.assertEquals(0x0a, (buf[0] & 0xff));
		Assertions.assertEquals(0xbc, (buf[1] & 0xff));
		encodeDecode("ABC");
	}

    @Test
    void testPartial() {
        Assertions.assertEquals("FF01", HexCodec.hexEncode(new byte[]{0, (byte)0xff, 1, 2, 3, 4},
                1, 2));
    }

}
