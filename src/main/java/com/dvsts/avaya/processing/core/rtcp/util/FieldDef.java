package com.dvsts.avaya.processing.core.rtcp.util;

import java.io.Serializable;

/**
 * Represents a database table's field - its name, size (length in chars) and type.
 *
 * The implementation is complicated through the addition of table name and type2 fields
 * which appear to have been added exclusively for SQL query support.
 *
 * NOTE: Dangerous to change the signature of this class in ANY way because it is serialized.
 *
 * {@link net.conet.list.GXProxyServer#getFieldsFor}
 */
public class FieldDef implements Serializable {

    static final long	serialVersionUID	= 1L;
    public String		_tableName;
    public String		fieldName;
    public int			size;
    public int			type				= 0;
    public DATATYPES	_type2				= DATATYPES.STRING;

    public static int	VARCHAR				= 1;
    public static int	LONG				= 2;
    private boolean		isIDField			= false;

    public FieldDef(String fieldName, int size) {
        this.fieldName = fieldName;
        this.size = size;
    }

    public FieldDef(String fieldName, int size, boolean idField) {
        this.fieldName = fieldName;
        this.size = size;
        isIDField = idField;
    }

    public FieldDef(String fieldName, int size, int dbtype) {
        this.fieldName = fieldName;
        this.size = size;
        this.type = dbtype;
    }

    /**
     *
     * NOTE:
     *
     * This constructor is for initializing _type2 member, which refers
     * corresponding Java Data type. Since all data are stored in database in
     * type of string, the original data type is missing. _type2 provides type
     * info and possible type mapping between sql 'type' (existing member) and
     * Java types for late usage.
     *
     * The _type2 is initialized with DATATYPES.STRING.
     *
     *
     * IMPORTANT: The _JType is just a type reference. It is added to avoid any
     * changes related to data process. How to realize data typing is a later
     * solution.
     *
     *
     * @param fieldName
     * @param size
     * @param jtype
     */
    public FieldDef(String fieldName, int size, DATATYPES jtype) {
        this(fieldName, size);
        _type2 = jtype;
    }

    /**
     * this constructor will possibly be used late.
     *
     * @param fieldName
     * @param size
     * @param type
     * @param jtype
     */
    public FieldDef(String fieldName, int size, int dbtype, DATATYPES jtype) {
        this(fieldName, size);
        this.type = dbtype;
        _type2 = jtype;
    }

    /**
     * this constructor contains complete field definition elements, and should
     * be used everywhere.
     *
     * @param tableName
     * @param fieldName
     * @param size
     * @param type
     * @param jtype
     */
    public FieldDef(String tableName, String fieldName, int size, DATATYPES jtype) {
        this(fieldName, size);
        _type2 = jtype;
        _tableName = tableName;
    }

    public boolean isIDfield() {
        return isIDField;
    }

    // full name
    public String toString() {
        if (_tableName != null && !_tableName.equals(""))
            return _tableName + "." + fieldName;
        else
            return fieldName;
    }

    public DATATYPES get_type2() {
        return _type2;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String get_tableName() {
        return _tableName;
    }

    public void set_tableName(String name) {
        _tableName = name;
    }
}