/*
j8583 A Java implementation of the ISO8583 protocol
Copyright (C) 2011 Enrique Zamudio Lopez

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
*/
package com.solab.iso8583.parse;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;

import com.solab.iso8583.CustomBinaryField;
import com.solab.iso8583.CustomField;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.util.Bcd;
import com.solab.iso8583.util.HexCodec;

/**
 * This class is used to parse fields of type LLLBIN.
 *
 * @author Enrique Zamudio
 */
public class LllbinParseInfo extends FieldParseInfo {

    /**
     * Instantiates a new Lllbin parse info.
     *
     * @param t   the t
     * @param len the len
     */
    public LllbinParseInfo(IsoType t, int len) {
        super(t, len);
    }

    /**
     * Instantiates a new Lllbin parse info.
     */
    public LllbinParseInfo() {
		super(IsoType.LLLBIN, 0);
	}

	@Override
	public <T> IsoValue<?> parse(final int field, final byte[] buf,
                             final int pos, final CustomField<T> custom)
            throws ParseException, UnsupportedEncodingException {
		if (pos < 0) {
			throw new ParseException(String.format("Invalid LLLBIN field %d pos %d",
                    field, pos), pos);
		} else if (pos+3 > buf.length) {
			throw new ParseException(String.format("Insufficient LLLBIN header field %d",
                    field), pos);
		}
		final int l = decodeLength(buf, pos, 3);
		if (l < 0) {
			throw new ParseException(String.format("Invalid LLLBIN length %d field %d pos %d",
                    l, field, pos), pos);
		} else if (l+pos+3 > buf.length) {
			throw new ParseException(String.format(
                    "Insufficient data for LLLBIN field %d, pos %d len %d",
                    field, pos, l), pos);
		}
		byte[] binval = l == 0 ? new byte[0] : HexCodec.hexDecode(new String(buf, pos + 3, l));
		if (custom == null) {
			return new IsoValue<>(type, binval, binval.length, null);
        } else if (custom instanceof CustomBinaryField) {
            try {
                T dec = ((CustomBinaryField<T>)custom).decodeBinaryField(
                    buf, pos + 3, l);
                return dec == null ? new IsoValue<>(type, binval, binval.length, null) :
                        new IsoValue<>(type, dec, 0, custom);
            } catch (IndexOutOfBoundsException ex) {
                throw new ParseException(String.format(
                        "Insufficient data for LLLBIN field %d, pos %d len %d",
                        field, pos, l), pos);
            }
		} else {
            try {
                T dec = custom.decodeField(
                    l == 0 ? "" : new String(buf, pos + 3, l));
                return dec == null ? new IsoValue<>(type, binval, binval.length, null) :
                        new IsoValue<>(type, dec, l, custom);
            } catch (IndexOutOfBoundsException ex) {
                throw new ParseException(String.format(
                        "Insufficient data for LLLBIN field %d, pos %d len %d",
                        field, pos, l), pos);
            }
		}
	}

	@Override
	public <T> IsoValue<?> parseBinary(final int field, final byte[] buf,
                                   final int pos, final CustomField<T> custom)
            throws ParseException {
		if (pos < 0) {
			throw new ParseException(String.format("Invalid bin LLLBIN field %d pos %d",
                    field, pos), pos);
		} else if (pos+2 > buf.length) {
            throw new ParseException(String.format("Insufficient LLLBIN header field %d",
                             field), pos);
		}
		final int l = getLengthForBinaryParsing(buf, pos);
		if (l < 0) {
            throw new ParseException(String.format("Invalid LLLBIN length %d field %d pos %d",
                             l, field, pos), pos);
		}
		if (l+pos+2 > buf.length) {
			throw new ParseException(String.format(
                    "Insufficient data for bin LLLBIN field %d, pos %d requires %d, only %d available",
                    field, pos, l, buf.length-pos+1), pos);
		}
		byte[] _v = new byte[l];
		System.arraycopy(buf, pos+2, _v, 0, l);
		if (custom == null) {
            int len = getFieldLength(buf, pos);
            return new IsoValue<>(type, _v, len, forceHexadecimalLength);
        } else if (custom instanceof CustomBinaryField) {
            try {
                T dec = ((CustomBinaryField<T>)custom).decodeBinaryField(
                    buf, pos + 2, l);
                return dec == null ? new IsoValue<>(type, _v, _v.length, forceHexadecimalLength, null) :
                        new IsoValue<>(type, dec, l, forceHexadecimalLength, custom);
            } catch (IndexOutOfBoundsException ex) {
                throw new ParseException(String.format(
                        "Insufficient data for LLLBIN field %d, pos %d", field, pos), pos);
            }
		} else {
            T dec = custom.decodeField(HexCodec.hexEncode(_v, 0, _v.length));
            return dec == null ? new IsoValue<>(type, _v, null) :
                    new IsoValue<>(type, dec, custom);
		}
	}

    /**
     * Gets length for binary parsing.
     *
     * @param buf the buf
     * @param pos the pos
     * @return the length for binary parsing
     */
    protected int getLengthForBinaryParsing(final byte[] buf, final int pos) {
		return getFieldLength(buf, pos);
	}

	private int getFieldLength(final byte[] buf, final int pos) {
        return forceHexadecimalLength ?
                ((buf[pos] & 0xff) << 8) | (buf[pos + 1] & 0xff)
                :
                ((buf[pos] & 0x0f) * 100) + Bcd.parseBcdLength(buf[pos + 1]);
    }

}
