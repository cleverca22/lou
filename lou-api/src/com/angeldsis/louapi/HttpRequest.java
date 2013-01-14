package com.angeldsis.louapi;

import java.io.InputStream;

public interface HttpRequest {
	void PostURL(String url, String data,Callback cb);
	class HttpReply {
		public int code;
		public InputStream stream;
		public int size;
		public Exception e;
	}
	abstract class Callback {
		public abstract void done(HttpReply reply);
	}
}
