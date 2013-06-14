package com.angeldsis.louapi;

import java.net.UnknownHostException;

public class DnsError extends Exception {
	public DnsError(Exception e) {
		super(e);
	}
}
