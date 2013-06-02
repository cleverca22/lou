package com.angeldsis.louapi;

import java.net.SocketTimeoutException;

public class TimeoutError extends Exception {
	public TimeoutError(String string, SocketTimeoutException e) {
		super(string,e);
	}
}
