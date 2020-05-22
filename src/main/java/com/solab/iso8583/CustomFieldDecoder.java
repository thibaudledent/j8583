package com.solab.iso8583;

/**
 * A functional interface to decode a value from a field (string or binary data) into some
 * other data type.
 *
 * @author Enrique Zamudio
 * Date: 2019-02-08 11:21
 */
public interface CustomFieldDecoder<DataType> {

    DataType decodeField(String value);
}
