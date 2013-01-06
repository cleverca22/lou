package com.angeldsis.louapi;

import java.net.UnknownHostException;

public interface HttpRequest {
	void PostURL(String url, String data,Callback cb);
	class HttpReply {
		public int code;
		public String body;
	}
	abstract class Callback {
		public abstract void done(HttpReply reply);
		public abstract void error(UnknownHostException e);
	}
}
