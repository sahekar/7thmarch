package com.dvsts.avaya.processing.core.rtcp;

import java.util.Arrays;

public final class CurrentStackTrace extends Exception {
	public CurrentStackTrace() {
		// remove enclosing method from stack trace
		StackTraceElement[] trace = getStackTrace();
		if (trace != null) {
			trace = Arrays.copyOfRange(trace, 1, trace.length, trace.getClass());
			setStackTrace(trace);
		}
	}
}
