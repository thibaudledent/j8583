package com.solab.iso8583;

/**
 * Defines the behavior of a custom field encoder, which will convert a value of some
 * data type to a String.
 *
 * @author Enrique Zamudio
 * Date: 2019-02-08 11:20
 */
public interface CustomFieldEncoder<DataType> {

    String encodeField(DataType value);
}
