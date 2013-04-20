package com.angeldsis.louapi;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class LouSession {
	public static class NewServer {
		protected String server;
		protected String clazz;
		protected String id;
		protected String name;
	}
	static final String TAG = "LouSession";
	static URL base;
	public CookieManager mCookieManager;
	public ArrayList<ServerInfo> servers;
	public String REMEMBER_ME;
	public long dataage;
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
	public static final String cookiename = "JSESSIONID";
	public void restore_cookie(String cookie) {
		HttpCookie httpcookie = new HttpCookie(cookiename,cookie);
		httpcookie.setDomain("www.lordofultima.com");
		httpcookie.setPath("/");
		httpcookie.setVersion(0);
		try {
			mCookieManager.getCookieStore().removeAll();
			mCookieManager.getCookieStore().add(new URI("http://www.lordofultima.com/"),httpcookie);
			Log.v(TAG, "cookie restored?");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			Log.wtf(TAG, "hard-coded uri not valid", e);
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
	public LouSession() {
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
	public result startLogin(String username,String password) {
		try {
			URL login = new URL("https://www.lordofultima.com/j_security_check");
			// initial login page
			HttpsURLConnection conn = (HttpsURLConnection) login.openConnection();
			conn.setReadTimeout(40000);
			conn.setConnectTimeout(15000);
			conn.setRequestMethod("POST");
			HttpsURLConnection.setFollowRedirects(false);
			HttpURLConnection.setFollowRedirects(false);
			String data = "j_username="+URLEncoder.encode(username,"UTF-8")+
				"&j_password="+URLEncoder.encode(password,"UTF-8");
			conn.setFixedLengthStreamingMode(data.getBytes().length);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setDoOutput(true);
			PrintWriter out = new PrintWriter(conn.getOutputStream());
			out.print(data);
			out.close();
			conn.connect();
			int response = conn.getResponseCode();
			Log.v(TAG,"response code "+response);
			if (response != 302) {
				// FIXME error;
				char[] buffer = new char[1024];
				int size;
				StringBuilder buf = new StringBuilder();
				InputStreamReader reply1 = new InputStreamReader(conn.getInputStream());
				while ((size = reply1.read(buffer, 0, 1024)) != -1) {
					buf.append(buffer,0,size);
				}
				Log.e(TAG,"error3 "+buf.toString());
				return null;
			}
			dumpCookies();
			
			String url2 = conn.getHeaderField("Location");
			Log.v(TAG,"url2:"+url2);
			boolean repeat = true;
			HttpURLConnection conn2;
			do  {
				Log.v(TAG,"following redir "+url2);
				if (url2.equals("/en/game?")) url2 = "http://www.lordofultima.com/en/game?";
				conn2 = this.doRequest(url2);
				response = conn2.getResponseCode();
				Log.v(TAG,"response code "+response);
				if (response == 200) {
					repeat = false;
				} else if (response == 301) {
					url2 = conn2.getHeaderField("Location");
					Log.v(TAG,"302'd to "+url2);
				}
			} while (repeat);

			Log.v(TAG,"Stage 1");
			char[] buffer = new char[1024];
			int size;
			StringBuilder buf = new StringBuilder();
			InputStreamReader reply1 = new InputStreamReader(conn2.getInputStream());
			while ((size = reply1.read(buffer, 0, 1024)) != -1) {
				buf.append(buffer,0,size);
			}
			String html = buf.toString();
			buf = null;
			Pattern sessionid = Pattern.compile("[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}");
			Matcher m = sessionid.matcher(html);
			if (!m.find()) {
				Log.e(TAG,"error4 "+html);
				Log.e(TAG,"sessionid not found");
				return null; // FIXME
			}
			String sessionId = m.group(0);
			Log.v(TAG,"sessionid "+sessionId);

			conn2 = this.doRequest("http://prodcdngame.lordofultima.com/Farm/service.svc/ajaxEndpoint/1/session/"+
						sessionId+"/worlds");
			if (conn2.getResponseCode() == 200) {
			} else {
				Log.e(TAG,String.format("unknown error %s",conn2.getResponseCode()));
				return null; // FIXME
			}
			
			System.out.println("final code "+conn2.getResponseCode());
			final result output = new result();
			output.worked = false;
			parse_result(output, conn2.getInputStream());
			return output;
		} catch (UnknownHostException e) {
			result output = new result();
			output.error = true;
			output.errmsg = "network error";
			output.e = e;
			return output;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			result output = new result();
			output.errmsg = "IO error durring login";
			output.error = true;
			output.e = e;
			return output;
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			result output = new result();
			output.errmsg = "error3";
			output.error = true;
			output.e = e;
			return output;
		}
	}
	private void dumpCookies() {
		CookieStore store = mCookieManager.getCookieStore();
		List<HttpCookie> all = store.getCookies();
		for (HttpCookie c : all) {
			Log.v(TAG,String.format("Cookie Dump: %s=%s domain:%s secure:%s",c.getName(),c.getValue(),c.getDomain(),c.getSecure() ? "true" : "false"));
		}
	}
	HttpURLConnection doRequest(String url) throws IOException  {
		Log.v(TAG,"Url3:"+url);
		URL secondurl = new URL(base,url);
		Log.v(TAG,"Url4: "+secondurl.toString());
		HttpURLConnection conn2 = (HttpURLConnection)secondurl.openConnection();
		conn2.setReadTimeout(20000);
		conn2.setConnectTimeout(15000);
		conn2.setRequestMethod("GET");
		conn2.setDoOutput(false);
		conn2.connect();
		return conn2;
	}
	private void parse_result(final result output, InputStream is) throws IOException, SAXException {
		XMLReader xmlReader = XMLReaderFactory.createXMLReader ("org.ccil.cowan.tagsoup.Parser");
		final ArrayList<ServerInfo> servers = new ArrayList<ServerInfo>();
		final ArrayList<NewServer> newServers = new ArrayList<NewServer>();
		final Pattern actioncheck = Pattern.compile("^http://prodgame(\\d+).lordofultima.com/(\\d+)/index.aspx");
		final Pattern findid = Pattern.compile("\\d+");
		ContentHandler handler = new DefaultHandler() {
			NewServer newServer;
			ServerInfo currentRow;
			int state = -1;
			public void startElement(String uri,String localName,String qName, Attributes attributes) throws SAXException {
				
				if (localName.equals("server")) {
					//Log.v(TAG,"server start");
					currentRow = new ServerInfo();
				} else if (localName.equals("servername")) {
					state = 1;
				} else if (localName.equals("serverStatus")) {
					state = 2;
				} else if (localName.equals("lastLogin")) {
					state = 3;
				} else if (localName.equals("sessionId")) {
					state = 4;
				} else if (localName.equals("serverURL")) {
					state = 5;
				} else {
					//Log.v(TAG,"startElement "+localName);
				}
			}
			public void endElement (String uri, String localName, String qName) {
				if (state != -1) {
					state = -1;
				}
				if (localName.equals("server")) {
					if (currentRow.lastLogin != null) {
						Log.v(TAG,String.format("name:%s login:%s",currentRow.servername,currentRow.lastLogin));
						servers.add(currentRow);
						output.worked = true;
					}
					currentRow = null;
				}
			}
			public void characters(char[]text, int start, int size) {
				Matcher m;
				if (state != -1) {
					String buf = new String(text,start,size);
					switch (state) {
					case 1:
						currentRow.servername = buf;
						m = findid.matcher(buf);
						if (!m.find()) {
							Log.e(TAG,"cant find worldid in world");
							currentRow.worldid = -1;
						} else currentRow.worldid = Integer.parseInt(m.group());
						break;
					case 2:
						if (buf.equals("ONLINE")) currentRow.offline = false;
						else currentRow.offline = true;
						break;
					case 3:
						currentRow.lastLogin = buf;
						break;
					case 4:
						currentRow.sessionId = buf;
						break;
					case 5:
						m = actioncheck.matcher(buf);
						if (!m.find()) Log.e(TAG,"cant find url parts "+buf);
						else {
							currentRow.serverid = m.group(1);
							currentRow.pathid = m.group(2);
						}
					}
				} else if (size > 0) {
					//String buf = new String(text,start,size);
					//Log.v(TAG,"text: "+state+" "+size+" "+buf);
				}
			}
		};
		xmlReader.setContentHandler(handler);
		FilterInputStream wrapper = new FilterInputStream(is) {
			public int read(byte[] buffer, int offset, int count) throws IOException {
				int size = super.read(buffer, offset, count);
				if (size == -1) return size;
				//Log.v(TAG,"sniff:"+new String(buffer,offset,size));
				return size;
			}
		};
		xmlReader.parse(new InputSource(wrapper));
		if (output.worked) {
			List<HttpCookie> cookies;
			try {
				cookies = mCookieManager.getCookieStore().get(new URI("http://www.lordofultima.com/"));
				int x;
				for (x = 0; x < cookies.size(); x++) {
					if (cookies.get(x).getName().equals(cookiename)) {
						REMEMBER_ME = cookies.get(x).getValue();
					}
				}
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.servers = servers;
			dataage = System.currentTimeMillis();
			//for (NewServer s : newServers) {
			//	Log.v(TAG,String.format("%s %s %s", s.server,s.id,s.name));
			//}
			return;
		}
		output.error = true;
		output.errmsg = "logout link not found";
		this.dumpCookies();
		System.out.println("done");
	}
	public class result {
		int code;
		String result;
		public boolean error;
		public boolean worked;
		public String errmsg;
		
		public Exception e;
	}
	void debugDump(InputStream is){
		char[] buffer = new char[1024];
		int size;
		StringBuilder buf = new StringBuilder();
		InputStreamReader reply1 = new InputStreamReader(is);
		try {
			while ((size = reply1.read(buffer, 0, 1024)) != -1) {
				buf.append(buffer,0,size);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(buf.toString());
	}
	public result check_cookie() {
		try {
			URL check = new URL("http://www.lordofultima.com/game/world/change");
			HttpsURLConnection.setFollowRedirects(false);
			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection conn = (HttpURLConnection) check.openConnection();
			conn.setReadTimeout(30000);
			conn.setConnectTimeout(15000);
			conn.setRequestMethod("GET");
			conn.setDoOutput(false);
			conn.setDoInput(true);
			conn.connect();
			if (conn.getResponseCode() == 200) {
			} else if (conn.getResponseCode() == 302) {
				String secondurl = conn.getHeaderField("Location");
				if (secondurl.startsWith("http://www.lordofultima.com/login/auth")) {
					result obj = new result();
					Log.e(TAG,String.format("fail 1 %d %s",conn.getResponseCode(),secondurl));
					obj.worked = false;
					return obj;
				} else {
					Log.e(TAG,"unknown error: "+secondurl);
					return null; // FIXME
				}
			} else {
				Log.e(TAG, "was expecting 302, got "+conn.getResponseCode());
				return null; // FIXME
			}
			//String secondurl = conn.getHeaderField("Location");
			//Log.v(TAG, "second url:" + secondurl);
			//if (secondurl.equals("/en/welcome?"))
				//secondurl = "http://www.lordofultima.com/en/welcome?";// FIXME
			
			// load the /game/world/change page and extract the sessionid
			char[] buffer = new char[1024];
			int size;
			StringBuilder buf = new StringBuilder();
			InputStreamReader reply1 = new InputStreamReader(conn.getInputStream());
			while ((size = reply1.read(buffer, 0, 1024)) != -1) {
				buf.append(buffer,0,size);
			}
			String html = buf.toString();
			buf = null;
			Pattern sessionid = Pattern.compile("[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}");
			Matcher m = sessionid.matcher(html);
			if (!m.find()) {
				Log.e(TAG,"error4 "+html);
				Log.e(TAG,"sessionid not found");
				return null; // FIXME
			}
			String sessionId = m.group(0);
			Log.v(TAG,"sessionid "+sessionId);

			HttpURLConnection conn2;
			conn2 = this.doRequest("http://prodcdngame.lordofultima.com/Farm/service.svc/ajaxEndpoint/1/session/"+
						sessionId+"/worlds");
			if (conn2.getResponseCode() == 200) {
			} else {
				Log.e(TAG,String.format("unknown error %s",conn2.getResponseCode()));
				return null; // FIXME
			}
			
			System.out.println("final code "+conn2.getResponseCode());
			final result output = new result();
			output.worked = false;
			parse_result(output, conn2.getInputStream());
			return output;
		} catch (UnknownHostException e) {
			System.out.println("dns error"+ e);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public void logout() {
		mCookieManager.getCookieStore().removeAll();
		servers = null;
	}
}
