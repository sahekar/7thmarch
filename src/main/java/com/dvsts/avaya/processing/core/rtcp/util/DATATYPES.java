package com.dvsts.avaya.processing.core.rtcp.util;

/**
 * Enumeration of data types used exclusively by the SQL query builder.
 *
 * NOTE: Dangerous to change the signature of this class in ANY way because it is serialized.
 *
 * {@link net.conet.client.sql.QueryBuilderUI}
 * {@link net.conet.list.GXProxyServer#getFieldsFor}
 */
public enum DATATYPES {
    BYTE(101),
    SHORT(102),
    INTEGER(103),
    LONG(104),
    FLOAT(105),
    DOUBLE(106),
    CHAR(107),
    STRING(108),
    BOOLEAN(109),
    DATE(110),
    TIME(111);

    public final int	order;

    private DATATYPES(int order) {
        this.order = order;
    }

    public int toDigital() {
        return order;
    }

    public static DATATYPES digital2type(int order) {
        return digital2type(order, DATATYPES.STRING);
    }

    public static DATATYPES digital2type(int order, DATATYPES defaultValue) {
        DATATYPES dataType = defaultValue;

        for (DATATYPES value : values()) {
            if (order == value.order) {
                dataType = value;
                break;
            }
        }

        return dataType;
    }
}
