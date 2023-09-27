package com.solab.iso8583.parse;

import com.solab.iso8583.CustomField;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.util.HexCodec;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;

public class RawBinaryParseInfo extends FieldParseInfo {


    public RawBinaryParseInfo(int len) {
        super(IsoType.RAW_BINARY, len);
    }

    @Override
    public <T> IsoValue<?> parse(int field, byte[] buf, int pos, CustomField<T> custom) throws ParseException, UnsupportedEncodingException {
        return parseBinary(field, buf, pos, custom);
    }

    @Override
    public <T> IsoValue<?> parseBinary(int field, byte[] buf, int pos, CustomField<T> custom) throws ParseException, UnsupportedEncodingException {
        if (pos < 0) {
            throw new ParseException(String.format("Invalid RAW_BINARY field %d position %d",
                    field, pos), pos);
        }
        if (pos + length > buf.length) {
            throw new ParseException(String.format(
                    "Insufficient data for RAW_BINARY field %d of length %d, pos %d",
                    field, length, pos), pos);
        }
        byte[] _v = new byte[length];
        System.arraycopy(buf, pos, _v, 0, length);
        if (custom == null) {
            return new IsoValue<>(type, _v, length, null);
        } else {
            T dec = custom.decodeField(HexCodec.hexEncode(_v, 0, _v.length));
            return dec == null ? new IsoValue<>(type, _v, length, null) :
                    new IsoValue<>(type, dec, length, custom);
        }
    }
}
