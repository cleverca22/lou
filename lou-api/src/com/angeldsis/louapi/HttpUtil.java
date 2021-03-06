package com.angeldsis.louapi;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.angeldsis.louapi.LouSession.SessionState;

public interface HttpUtil {
	void dumpCookies();
	void logout();
	public class HttpReply {
		public int code;
		public InputStream stream;
		public String location;
		public int contentLength;
		public Exception e;
		public HttpReply(Exception e) {
			this.e = e;
		}
		public HttpReply() {
		}
	}
	HttpReply postUrl(String url, String data);
	HttpReply getUrl(String url);
	String encode(String str) throws UnsupportedEncodingException;
	HttpReply postUrl(String url, byte[] raw_data) throws TimeoutError, DnsError;
	void restoreState(SessionState state);
	void syncCookieState(SessionState state);
}
