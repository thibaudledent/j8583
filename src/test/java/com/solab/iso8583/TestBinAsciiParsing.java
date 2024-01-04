package com.solab.iso8583;

import com.solab.iso8583.parse.ConfigParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

/** author Dave King, djking@gmail.com */
class TestBinAsciiParsing {

    @Test
    void binAscii100Message() throws Exception{
        MessageFactory<IsoMessage> mf = ConfigParser.createFromClasspathConfig("bin_ascii.conf.xml");
        mf.setBinaryHeader(true);

        byte[] data = loadData("bin_ascii_100.bin");
        IsoMessage msg = mf.parseMessage(data,4);
        Assertions.assertEquals("8264430055",msg.getField(2).getValue(), "Client ID");
        Assertions.assertEquals("000000000050084090210",msg.getField(61).getValue(), "POS Data");
    }

    @Test
    void binAscii810Message() throws Exception{
        MessageFactory<IsoMessage> mf = ConfigParser.createFromClasspathConfig("bin_ascii.conf.xml");
        mf.setBinaryHeader(true);

        byte[] data = loadData("bin_ascii_810.bin");
        IsoMessage msg = mf.parseMessage(data, 0);
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        c.clear();
        c.set(year, Calendar.MARCH, 22, 22, 0, 1);

        long expected = c.getTime().getTime();
        long actual = ((Date)(msg.getField(7).getValue())).getTime();
        if (expected > actual) {
            c.add(Calendar.YEAR, -1);
            expected = c.getTime().getTime();
        }

        Assertions.assertEquals(expected,actual, "DateTime");
        Assertions.assertEquals("001",msg.getField(70).getValue(), "810 msg type");
    }

    public static byte[] loadData(String s) throws IOException {
        try (InputStream ins = new BufferedInputStream (TestBinAsciiParsing.class.getClassLoader().getResourceAsStream(s))) {
            if (ins == null) {
                throw new IllegalArgumentException(s + " not found");
            }
            byte[] data = new byte[0];
            byte[] buffer = new byte[1024];
            int r;

            while ((r = ins.read(buffer, 0, buffer.length)) > -1) {
                data = append(data, buffer, r);
            }
            return data;
        }
    }

    private static byte[] append(byte[] b1, byte[] b2, int b2Len){
        byte[] data = new byte[b1.length + b2Len];
        if(b1.length > 0){
            System.arraycopy(b1, 0, data, 0, b1.length);
        }
        System.arraycopy(b2, 0, data, b1.length, b2Len);
        return data;
    }
}
