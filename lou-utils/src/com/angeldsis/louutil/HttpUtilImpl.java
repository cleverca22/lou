package com.angeldsis.louutil;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import com.angeldsis.louapi.HttpUtil;
import com.angeldsis.louapi.Log;
import com.angeldsis.louapi.TimeoutError;

public class HttpUtilImpl implements HttpUtil {
	private static HttpUtilImpl self;
	private static final String TAG = "HttpUtilImpl";
	public CookieManager mCookieManager;
	private test mTest;
	// handles the login process
	private class Policy implements CookiePolicy {
		@Override
		public boolean shouldAccept(URI arg0, HttpCookie arg1) {
			//System.out.println(arg1.getName()+"="+arg1.getValue()+" "+arg1.getDomain());
			arg1.setDomain(".lordofultima.com");
			return true;
		}
	}
	// this fudges the cookie headers to fix things
	private static class test extends CookieHandler {
		CookieManager m;
		public test(CookieManager mCookieManager) {
			m = mCookieManager;
		}
		@Override public Map<String, List<String>> get(URI arg0, Map<String, List<String>> arg1) throws IOException {
			return m.get(arg0, arg1);
		}
		@Override public void put(URI arg0, Map<String, List<String>> headersIn) throws IOException {
			Map<String, List<String>> headersOut = new HashMap<String, List<String>>();
			for (String s : headersIn.keySet()) {
				if ((s != null) && (s.equals("Set-Cookie"))) {
					List<String> listIn = new ArrayList<String>(headersIn.get(s));
					//List<String> listOut = new ArrayList<String>(listIn);
					int x;
					for (x=0; x<listIn.size();x++) {
						String s2 = listIn.get(x);
						//Log.v(TAG,"s2: "+s2);
						s2 = s2.replaceAll("; HttpOnly","");
						s2 = s2.replaceAll(";HTTPONLY","");
						//Log.v(TAG,"s2: "+s2);
						listIn.set(x, s2);
						//List<HttpCookie> something = HttpCookie.parse(s2);
						//for (HttpCookie cookie : something) {
							//Log.v(TAG,String.format("cookie!!! %s",cookie.getValue()));
						//}
					}
					headersOut.put(s, listIn);
				} else {
					headersOut.put(s, headersIn.get(s));
				}
			}
			m.put(arg0, headersOut);
		}
	}
	private static URL base;

	private HttpUtilImpl() {
		mCookieManager = new CookieManager(null,new Policy());
		mTest = new test(mCookieManager);
		CookieHandler.setDefault(mTest);
		try {
			base = new URL("http://lordofultima.com");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static HttpUtilImpl getInstance() {
		if (self == null) self = new HttpUtilImpl();
		return self;
	}
	@Override public void restore_cookie(String cookie) {
		String[] parts = cookie.split(";");
		HttpCookie httpcookie1 = new HttpCookie("JSESSIONID",parts[0]);
		httpcookie1.setDomain("www.lordofultima.com");
		httpcookie1.setPath("/");
		httpcookie1.setVersion(0);
		HttpCookie httpcookie2 = null;
		if (parts.length == 2) {
			httpcookie2 = new HttpCookie("AWSELB",parts[1]);
			httpcookie2.setDomain("www.lordofultima.com");
			httpcookie2.setPath("/");
			httpcookie2.setVersion(0);
		}
		try {
			mCookieManager.getCookieStore().removeAll();
			mCookieManager.getCookieStore().add(new URI("http://www.lordofultima.com/"),httpcookie1);
			if (parts.length == 2) {
				mCookieManager.getCookieStore().add(new URI("http://www.lordofultima.com/"),httpcookie2);
			}
			Log.v(TAG, "cookie restored?");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			Log.wtf(TAG, "hard-coded uri not valid", e);
		}
	}
	@Override public String getCookieData() {
		List<HttpCookie> cookies;
		String JSESSIONID = null;
		String AWSELB = null;
		try {
			cookies = mCookieManager.getCookieStore().get(new URI("http://www.lordofultima.com/"));
			int x;
			for (x = 0; x < cookies.size(); x++) {
				if (cookies.get(x).getName().equals("JSESSIONID")) {
					JSESSIONID = cookies.get(x).getValue();
				} else if (cookies.get(x).getName().equals("AWSELB")) {
					AWSELB = cookies.get(x).getValue();
				}
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return JSESSIONID+";"+AWSELB;
	}
	@Override public void dumpCookies() {
		CookieStore store = mCookieManager.getCookieStore();
		List<HttpCookie> all = store.getCookies();
		for (HttpCookie c : all) {
			Log.v(TAG,String.format("Cookie Dump: %s=%s domain:%s secure:%s",c.getName(),c.getValue(),c.getDomain(),c.getSecure() ? "true" : "false"));
		}
	}
	@Override public void logout() {
		mCookieManager.getCookieStore().removeAll();
	}
	@Override public HttpReply postUrl(String url, String data) {
		try {
			URL login = new URL(url);
			HttpsURLConnection conn = (HttpsURLConnection) login.openConnection();
			HttpsURLConnection.setFollowRedirects(false);
			byte[] raw_data = data.getBytes();
			conn.setReadTimeout(40000);
			conn.setConnectTimeout(15000);
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setFixedLengthStreamingMode(raw_data.length);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			return doPost(conn,raw_data);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	@Override public HttpReply getUrl(String url) {
		try {
			Log.v(TAG,"Url3:"+url);
			URL secondurl = new URL(base,url);
			Log.v(TAG,"Url4: "+secondurl.toString());
			HttpURLConnection conn = (HttpURLConnection)secondurl.openConnection();
			conn.setReadTimeout(20000);
			conn.setConnectTimeout(15000);
			conn.setRequestMethod("GET");
			conn.setDoOutput(false);
			conn.connect();
			return makeReply(conn);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	private HttpReply doPost(HttpURLConnection conn, byte[] raw_data) throws IOException {
		HttpsURLConnection.setFollowRedirects(false);
		HttpURLConnection.setFollowRedirects(false);
		OutputStream os = conn.getOutputStream();
		os.write(raw_data);
		os.close();
		conn.connect();
		return makeReply(conn);
	}
	private HttpReply makeReply(HttpURLConnection conn) throws IOException {
		HttpReply reply = new HttpReply();
		reply.code = conn.getResponseCode();
		reply.stream = conn.getInputStream();
		reply.location = conn.getHeaderField("Location");
		reply.contentLength = conn.getContentLength();
		return reply;
	}
	@Override public String encode(String str) throws UnsupportedEncodingException {
		return URLEncoder.encode(str,"UTF-8");
	}
	@Override public HttpReply postUrl(String url, byte[] raw_data) throws TimeoutError {
		try {
			URL login = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) login.openConnection();
			conn.setReadTimeout(60000);
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setFixedLengthStreamingMode(raw_data.length);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.connect();
			return doPost(conn,raw_data);
		} catch (MalformedURLException e) {
			return new HttpReply(e);
		} catch (SocketTimeoutException e) {
			throw new TimeoutError("network timeout",e);
		} catch (IOException e) {
			return new HttpReply(e);
		}
	}
}
