package com.dvsts.avaya.processing.core.rtcp.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VarBind implements Serializable, Comparable<Object> {

	public static final long	serialVersionUID	= 3476420591205672698L;

	// TODO: These variables need to be private
	public String				field;
	public String				value;

	public VarBind(String field, String value) {
		this.field = field;
		this.value = value;
	}

	@Override
	public int compareTo(Object o) {
		VarBind other = (VarBind) o;
		return field.compareTo(other.field);
	}

	public String getField() {
		return field;
	}

	public String getValue() {
		return value;
	}

	public void setField(String newName) {
		field = newName;
	}

	public void setValue(String newValue) {
		value = newValue;
	}

	@Override
	public String toString() {
		return field + " = " + value;
	}

	/**
	 * Converts a map of key value strings in to a list of VarBinds.
	 *
	 * @param parameters
	 *            The map to convert.
	 * @return A list of VarBind
	 */
	public static final VarBind[] toArray(Map<String, String> parameters) {
		VarBind[] vars = new VarBind[parameters.size()];
		int i = 0;

		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			vars[i++] = new VarBind(entry.getKey(), entry.getValue());
		}

		return vars;
	}

	/**
	 * Convert a list of VarBind in to a map.
	 * Any duplicate key's in the VarBinds will be lost leaving the last matching VarBind in the map.
	 *
	 * @param binds
	 *            The list of VarBind's to convert.
	 * @return The list in map form.
	 */
	public static final Map<String, String> toMap(List<VarBind> binds) {
		Map<String, String> map = new HashMap<String, String>(binds.size());
		for (VarBind bind : binds) {
			map.put(bind.getField(), bind.getValue());
		}
		return map;
	}

	/**
	 * Convert an array of VarBind in to a map.
	 * Any duplicate key's in the VarBinds will be lost leaving the last matching VarBind in the map.
	 *
	 * @param binds
	 *            The list of VarBind's to convert.
	 * @return The list in map form.
	 */
	public static final Map<String, String> toMap(VarBind[] binds) {
		Map<String, String> map = new HashMap<String, String>(binds.length);
		for (VarBind bind : binds) {
			map.put(bind.getField(), bind.getValue());
		}
		return map;
	}
}