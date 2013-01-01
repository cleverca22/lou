package com.angeldsis.louapi;

public interface HttpRequest {
	void PostURL(String url, String data,Callback cb);
	class HttpReply {
		public int code;
		public String body;
	}
	abstract class Callback {
		public abstract void done(HttpReply reply);
	}
}
