package com.angeldsis.louapi;

public class TimeoutError extends Exception {
	public TimeoutError(String string, Exception e) {
		super(string,e);
	}
}
