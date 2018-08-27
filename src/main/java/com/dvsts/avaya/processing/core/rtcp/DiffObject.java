package com.dvsts.avaya.processing.core.rtcp;

public class DiffObject {

	public enum DifferenceType {
		MODIFIED,
		ADDED,
		REMOVED
	}

	private DifferenceType	type;
	private Object			object;

	public DiffObject(DifferenceType type, Object object) {
		this.type = type;
		this.object = object;
	}

	public DifferenceType getType() {
		return type;
	}

	public Object getObject() {
		return object;
	}
}
