/*
j8583 A Java implementation of the ISO8583 protocol
Copyright (C) 2007 Enrique Zamudio Lopez

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
package com.solab.iso8583;

import com.solab.iso8583.util.Bcd;
import com.solab.iso8583.util.HexCodec;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.TimeZone;

import static com.solab.iso8583.IsoType.RAW_BINARY;
import static com.solab.iso8583.IsoType.VARIABLE_LENGTH_VAR_TYPES;

/**
 * Represents a value that is stored in a field inside an ISO8583 message.
 * It can format the value when the message is generated.
 * Some values have a fixed length, other values require a length to be specified
 * so that the value can be padded to the specified length. LLVAR and LLLVAR
 * values do not need a length specification because the length is calculated
 * from the stored value.
 *
 * @param <T> the type parameter
 * @author Enrique Zamudio
 */
public class IsoValue<T> {

    private final IsoType type;
    private final T value;
    private final CustomFieldEncoder<T> encoder;
    private int length;
    private String encoding;
    private TimeZone tz;
    private boolean variableLengthFieldsInHex;

    public IsoValue(IsoValue<T> source) {
        this.type = source.getType();
        this.value = source.getValue();
        this.encoder = source.getEncoder();
        this.length = source.getLength();
        this.encoding = source.getCharacterEncoding();
        this.tz = source.getTimeZone();
        this.variableLengthFieldsInHex = source.isVariableLengthFieldsInHex();
    }

    /**
     * Instantiates a new Iso value.
     *
     * @param t     the t
     * @param value the value
     */
    public IsoValue(IsoType t, T value) {
        this(t, value, null);
    }

    /**
     * Creates a new instance that stores the specified value as the specified type.
     * Useful for storing LLVAR or LLLVAR types, as well as fixed-length value types
     * like DATE10, DATE4, AMOUNT, etc.
     *
     * @param t      the ISO type.
     * @param value  The value to be stored.
     * @param custom An optional CustomFieldEncoder for the value.
     */
    public IsoValue(IsoType t, T value, CustomFieldEncoder<T> custom) {
        if (t.needsLength()) {
            throw new IllegalArgumentException("Fixed-value types must use constructor that specifies length");
        }
        encoder = custom;
        type = t;
        this.value = value;
        if (VARIABLE_LENGTH_VAR_TYPES.contains(type)) {
            if (custom == null) {
                length = value.toString().length();
            } else {
                String enc = custom.encodeField(value);
                if (enc == null) {
                    enc = value == null ? "" : value.toString();
                }
                length = enc.length();
            }
            validateDecimalVariableLength();
        } else if (type == IsoType.LLBIN || type == IsoType.LLLBIN || type == IsoType.LLLLBIN || type == IsoType.LLBINLENGTHBIN || type == IsoType.LLLLBINLENGTHBIN) {
            if (custom == null) {
                if (value instanceof byte[] bytesValue) {
                    length = (bytesValue).length;
                } else if (type == IsoType.LLLLBINLENGTHBIN) {
                    length = value.toString().length() / 2 + (value.toString().length() % 2);
                } else {
                    length = value.toString().length() / 2 + (value.toString().length() % 2);
                }
            } else if (custom instanceof CustomBinaryField) {
                length = ((CustomBinaryField<T>) custom).encodeBinaryField(value).length;
            } else {
                String enc = custom.encodeField(value);
                if (enc == null) {
                    enc = value == null ? "" : value.toString();
                }
                length = enc.length();
            }
            validateDecimalVariableLength();
        } else if (type == IsoType.LLBCDBIN || type == IsoType.LLLBCDBIN || type == IsoType.LLLLBCDBIN || type == IsoType.LLBINLENGTHNUM || type == IsoType.LLLLBINLENGTHNUM || type == IsoType.LLBINLENGTHALPHANUM || type == IsoType.LLLLBINLENGTHALPHANUM || type == IsoType.LLBCDLENGTHALPHANUM) {
            if (value instanceof byte[] bytesValue) {
                length = (bytesValue).length * 2;
            } else {
                length = value.toString().length();
            }
            validateDecimalVariableLength();
        } else {
            length = type.getLength();
        }
    }

    /**
     * Instantiates a new Iso value.
     *
     * @param t   the t
     * @param val the val
     * @param len the len
     */
    public IsoValue(IsoType t, T val, int len) {
        this(t, val, len, null);
    }

    /**
     * Instantiates a new Iso value.
     *
     * @param t    the t
     * @param val  the val
     * @param len  the len
     * @param hexa the hexa
     */
    public IsoValue(IsoType t, T val, int len, boolean hexa) {
        this(t, val, len, hexa, null);
    }

    /**
     * Instantiates a new Iso value.
     *
     * @param t      the t
     * @param val    the val
     * @param len    the len
     * @param custom the custom
     */
    public IsoValue(IsoType t, T val, int len, CustomFieldEncoder<T> custom) {
        this(t, val, len, false, custom);
    }

    /**
     * Creates a new instance that stores the specified value as the specified type.
     * Useful for storing fixed-length value types.
     *
     * @param t      The ISO8583 type for this field.
     * @param val    The value to store in the field.
     * @param len    The length for the value.
     * @param hexa   Flag if length is encoded as hexadecimal value
     * @param custom An optional CustomFieldEncoder for the value.
     */
    public IsoValue(IsoType t, T val, int len, boolean hexa, CustomFieldEncoder<T> custom) {
        type = t;
        value = val;
        length = len;
        variableLengthFieldsInHex = hexa;
        encoder = custom;
        if (length == 0 && t.needsLength()) {
            throw new IllegalArgumentException(String.format("Length must be greater than zero for type %s (value '%s')", t, val));
        } else if (VARIABLE_LENGTH_VAR_TYPES.contains(t)) {
            if (len == 0) {
                length = custom == null ? val.toString().length() : custom.encodeField(value).length();
            }
            validateDecimalVariableLength();
        } else if (t == IsoType.LLBIN || t == IsoType.LLLBIN || t == IsoType.LLLLBIN || type == IsoType.LLBINLENGTHNUM || type == IsoType.LLBINLENGTHALPHANUM ||  type == IsoType.LLLLBINLENGTHALPHANUM ||type == IsoType.LLBINLENGTHBIN || type == IsoType.LLLLBINLENGTHNUM || type == IsoType.LLLLBINLENGTHBIN || type == IsoType.LLBCDLENGTHALPHANUM) {
            if (len == 0) {
                if (custom == null) {
                    length = ((byte[]) val).length;
                } else if (custom instanceof CustomBinaryField) {
                    length = ((CustomBinaryField<T>) custom).encodeBinaryField(value).length;
                } else {
                    length = custom.encodeField(value).length();
                }
                length = custom == null ? ((byte[]) val).length : custom.encodeField(value).length();
            }
            validateDecimalVariableLength();
        } else if (t == IsoType.LLBCDBIN || t == IsoType.LLLBCDBIN || t == IsoType.LLLLBCDBIN) {
            if (len == 0) {
                if (value instanceof byte[] bytesValue) {
                    length = (bytesValue).length * 2;
                } else {
                    length = value.toString().length();
                }
            }
            validateDecimalVariableLength();
        }
    }

    /**
     * Returns the ISO type to which the value must be formatted.
     *
     * @return the type
     */
    public IsoType getType() {
        return type;
    }

    /**
     * Returns the length of the stored value, of the length of the formatted value
     * in case of NUMERIC or ALPHA. It doesn't include the field length header in case
     * of LLVAR or LLLVAR.
     *
     * @return the length
     */
    public int getLength() {
        return length;
    }

    /**
     * Returns the stored value without any conversion or formatting.
     *
     * @return the value
     */
    public T getValue() {
        return value;
    }

    /**
     * Sets character encoding.
     *
     * @param value the value
     */
    public void setCharacterEncoding(String value) {
        encoding = value;
    }

    /**
     * Gets character encoding.
     *
     * @return the character encoding
     */
    public String getCharacterEncoding() {
        return encoding;
    }

    /**
     * Sets the timezone, useful for date fields.
     *
     * @param value the value
     */
    public void setTimeZone(TimeZone value) {
        tz = value;
    }

    /**
     * Gets time zone.
     *
     * @return the time zone
     */
    public TimeZone getTimeZone() {
        return tz;
    }

    public boolean isVariableLengthFieldsInHex() {
        return variableLengthFieldsInHex;
    }

    /**
     * Returns the formatted value as a String. The formatting depends on the type of the
     * receiver.
     */
    public String toString() {
        if (value == null) {
            return "ISOValue<null>";
        }
        if (type == IsoType.NUMERIC || type == IsoType.AMOUNT) {
            if (type == IsoType.AMOUNT) {
                if (value instanceof BigDecimal bigDecimal) {
                    return type.format(bigDecimal, 12);
                } else {
                    return type.format(value.toString(), 12);
                }
            } else if (value instanceof BigInteger) {
                return type.format(encoder == null ? value.toString() : encoder.encodeField(value), length);
            } else if (value instanceof Number number) {
                return type.format((number).longValue(), length);
            } else {
                return type.format(encoder == null ? value.toString() : encoder.encodeField(value), length);
            }
        } else if (type == IsoType.ALPHA) {
            return type.format(encoder == null ? value.toString() : encoder.encodeField(value), length);
        } else if (VARIABLE_LENGTH_VAR_TYPES.contains(type)) {
            return getStringEncoded();
        } else if (value instanceof Date date) {
            return type.format(date, tz);
        } else if (type == IsoType.BINARY || type == IsoType.RAW_BINARY || type == IsoType.LLBINLENGTHBIN || type == IsoType.LLLLBINLENGTHBIN) {
            if (value instanceof byte[] bytesValue) {
                return type.format(encoder == null ? HexCodec.hexEncode(bytesValue, 0, bytesValue.length) : encoder.encodeField(value), length * 2);
            } else {
                return type.format(encoder == null ? value.toString() : encoder.encodeField(value), length * 2);
            }
        } else if (type == IsoType.LLBIN || type == IsoType.LLLBIN || type == IsoType.LLLLBIN) {
            if (value instanceof byte[] bytesValue) {
                return encoder == null ? HexCodec.hexEncode(bytesValue, 0, bytesValue.length) : encoder.encodeField(value);
            } else {
                final String _s = getStringEncoded();
                return (_s.length() % 2 == 1) ? String.format("0%s", _s) : _s;
            }
        } else if (type == IsoType.LLBCDBIN || type == IsoType.LLLBCDBIN || type == IsoType.LLBCDLENGTHALPHANUM || type == IsoType.LLLLBCDBIN || type == IsoType.LLBINLENGTHNUM || type == IsoType.LLLLBINLENGTHNUM || type == IsoType.LLBINLENGTHALPHANUM || type == IsoType.LLLLBINLENGTHALPHANUM) {
            if (value instanceof byte[] bytesValue) {
                final String val = encoder == null ? HexCodec.hexEncode(bytesValue, 0, bytesValue.length) : encoder.encodeField(value);
                if (length == val.length() - 1) {
                    return val.substring(1);
                }
                return val;
            } else {
                return getStringEncoded();
            }
        }
        return getStringEncoded();
    }

    private String getStringEncoded() {
        return encoder == null ? value.toString() : encoder.encodeField(value);
    }

    /**
     * Returns true of the other object is also an IsoValue and has the same type and length,
     * and if other.getValue().equals(getValue()) returns true.
     */
    public boolean equals(Object other) {
        if (!(other instanceof IsoValue<?> comp)) {
            return false;
        }
        return (comp.getType() == getType() && comp.getValue().equals(getValue())
                && comp.getLength() == getLength());
    }

    @Override
    public int hashCode() {
        return value == null ? 0 : toString().hashCode();
    }

    /**
     * Returns the CustomFieldEncoder for this value.
     *
     * @return the encoder
     */
    public CustomFieldEncoder<T> getEncoder() {
        return encoder;
    }

    /**
     * Write length header.
     *
     * @param l                   the l
     * @param outs                the outs
     * @param type                the type
     * @param binary              the binary
     * @param forceStringEncoding the force string encoding
     * @throws IOException the io exception
     */
    protected void writeLengthHeader(final int l, final OutputStream outs, final IsoType type,
                                     final boolean binary, final boolean forceStringEncoding)
            throws IOException {
        final int digits;
        if (type == IsoType.LLLLBIN || type == IsoType.LLLLVAR || type == IsoType.LLLLBCDBIN) {
            digits = 4;
        } else if (type == IsoType.LLLBIN || type == IsoType.LLLVAR || type == IsoType.LLLBCDBIN) {
            digits = 3;
        } else {
            digits = 2;
        }

        if (type == IsoType.LLBINLENGTHNUM || type == IsoType.LLBINLENGTHBIN || type == IsoType.LLBINLENGTHALPHANUM) {
            outs.write((byte) l);
        } else if (type == IsoType.LLLLBINLENGTHNUM || type == IsoType.LLLLBINLENGTHBIN || type == IsoType.LLLLBINLENGTHALPHANUM) {
            final byte firstByte = (byte) (l & 0xFF);
            final byte secondByte = (byte) ((l >> 8) & 0xFF);
            outs.write(secondByte); // Since writing from left to right
            outs.write(firstByte);
        } else if (binary && !(VARIABLE_LENGTH_VAR_TYPES.contains(type) && forceStringEncoding)) {
            if (digits == 4) {
                outs.write((((l % 10000) / 1000) << 4) | ((l % 1000) / 100));
            } else if (digits == 3) {
                outs.write(l / 100); //00 to 09 automatically in BCD
            }
            //BCD encode the rest of the length
            outs.write((((l % 100) / 10) << 4) | (l % 10));
        } else if (forceStringEncoding) {
            String lhead = Integer.toString(l);
            final int ldiff = digits - lhead.length();
            if (ldiff == 1) {
                lhead = '0' + lhead;
            } else if (ldiff == 2) {
                lhead = "00" + lhead;
            } else if (ldiff == 3) {
                lhead = "000" + lhead;
            }
            outs.write(encoding == null ? lhead.getBytes() : lhead.getBytes(encoding));
        } else {
            //write the length in ASCII
            if (digits == 4) {
                outs.write((l / 1000) + 48);
                outs.write(((l % 1000) / 100) + 48);
            } else if (digits == 3) {
                outs.write((l / 100) + 48);
            }
            if (l >= 10) {
                outs.write(((l % 100) / 10) + 48);
            } else {
                outs.write(48);
            }
            outs.write((l % 10) + 48);
        }
    }

    /**
     * Writes the formatted value to a stream, with the length header
     * if it's a variable length type.
     *
     * @param outs                The stream to which the value will be written.
     * @param binary              Specifies whether the value should be written in binary or text format.
     * @param forceStringEncoding When using text format, force the encoding of length headers                            for variable-length fields to be done with the proper character encoding. When false,                            the length headers are encoded as ASCII; this used to be the only behavior.
     * @throws IOException the io exception
     */
    public void write(final OutputStream outs, final boolean binary, final boolean forceStringEncoding) throws IOException {
        if (type == IsoType.LLLVAR || type == IsoType.LLVAR || type == IsoType.LLLLVAR || type == IsoType.LLBINLENGTHALPHANUM || type == IsoType.LLLLBINLENGTHALPHANUM || type == IsoType.LLBINLENGTHBIN || type == IsoType.LLLLBINLENGTHBIN || type == IsoType.LLBCDLENGTHALPHANUM) {
            writeLengthHeader(length, outs, type, binary, forceStringEncoding);
        } else if (type == IsoType.LLBIN || type == IsoType.LLLBIN || type == IsoType.LLLLBIN || type == IsoType.LLBINLENGTHNUM || type == IsoType.LLLLBINLENGTHNUM) {
            writeLengthHeader(binary ? length : length * 2, outs, type, binary, forceStringEncoding);
        } else if (type == IsoType.LLBCDBIN || type == IsoType.LLLBCDBIN || type == IsoType.LLLLBCDBIN) {
            writeLengthHeader(length, outs, type, binary, forceStringEncoding);
        } else if (binary) {
            //numeric types in binary are coded like this
            byte[] buf = null;
            if (type == IsoType.NUMERIC) {
                buf = new byte[(length / 2) + (length % 2)];
            } else if (type == IsoType.AMOUNT) {
                buf = new byte[6];
            } else if (type == IsoType.DATE10 || type == IsoType.DATE4 ||
                    type == IsoType.DATE_EXP || type == IsoType.TIME ||
                    type == IsoType.DATE12 || type == IsoType.DATE14) {
                buf = new byte[length / 2];
            }
            //Encode in BCD if it's one of these types
            if (buf != null) {
                Bcd.encode(toString(), buf);
                outs.write(buf);
                return;
            }
        }
        if (type == RAW_BINARY || binary && (type == IsoType.BINARY || IsoType.VARIABLE_LENGTH_BIN_TYPES.contains(type))) {
            final int missing;
            if (value instanceof byte[] bytesValue) {
                outs.write(bytesValue);
                missing = length - (bytesValue).length;
            } else if (encoder instanceof CustomBinaryField) {
                byte[] binval = ((CustomBinaryField<T>) encoder).encodeBinaryField(value);
                outs.write(binval);
                missing = length - binval.length;
            } else {
                byte[] binval = HexCodec.hexDecode(value.toString());
                outs.write(binval);
                missing = length - binval.length;
            }
            if (missing > 0 && (type == IsoType.RAW_BINARY || type == IsoType.BINARY)) {
                for (int i = 0; i < missing; i++) {
                    outs.write(0);
                }
            }
        } else {
            outs.write(encoding == null ? toString().getBytes() : toString().getBytes(encoding));
        }
    }

    /**
     * Validate decimal variable length.
     */
    void validateDecimalVariableLength() {
        switch (type) {
            case LLVAR, LLBIN, LLBCDLENGTHALPHANUM -> validateMaxLength(99);
            case LLLVAR, LLLBIN -> validateMaxLength(999);
            case LLLLVAR, LLLLBIN -> validateMaxLength(9999);
            case LLBCDBIN -> validateMaxLength(50);
            case LLLBCDBIN -> validateMaxLength(500);
            case LLLLBCDBIN -> validateMaxLength(5000);
            case LLLLBINLENGTHBIN -> validateMaxLength(65535);
            case LLBINLENGTHNUM, LLBINLENGTHALPHANUM, LLLLBINLENGTHALPHANUM -> validateMaxLength(255);
            case LLBINLENGTHBIN -> validateMaxLength(510);
        }
    }

    private void validateMaxLength(int maxLength) {
        if (length > maxLength) {
            throw new IllegalArgumentException(type.name() + " can only hold values up to " + maxLength + " chars");
        }
    }

}
