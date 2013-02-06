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
import java.util.Iterator;
import java.util.List;
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
	public ArrayList<Account> servers;
	public String REMEMBER_ME;
	// handles the login process
	private class Policy implements CookiePolicy {
		@Override
		public boolean shouldAccept(URI arg0, HttpCookie arg1) {
			//System.out.println(arg1.getName()+"="+arg1.getValue()+" "+arg1.getDomain());
			arg1.setDomain(".lordofultima.com");
			return true;
		}
	}
	public void restore_cookie(String cookie) {
		HttpCookie httpcookie = new HttpCookie("REMEMBER_ME_COOKIE",cookie);
		httpcookie.setDomain("www.lordofultima.com");
		httpcookie.setPath("/");
		httpcookie.setVersion(0);
		try {
			mCookieManager.getCookieStore().add(new URI("http://www.lordofultima.com/"),httpcookie);
			Log.v(TAG, "cookie restored?");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			Log.wtf(TAG, "hard-coded uri not valid", e);
		}
	}
	public LouSession() {
		mCookieManager = new CookieManager(null,new Policy());
		CookieHandler.setDefault(mCookieManager);
		try {
			base = new URL("http://lordofultima.com");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public result startLogin(String username,String password) {
		try {
			URL login = new URL("https://www.lordofultima.com/en/user/login");
			// initial login page
			HttpsURLConnection conn = (HttpsURLConnection) login.openConnection();
			conn.setReadTimeout(40000);
			conn.setConnectTimeout(15000);
			conn.setRequestMethod("POST");
			HttpsURLConnection.setFollowRedirects(false);
			HttpURLConnection.setFollowRedirects(false);
			String data = "mail="+URLEncoder.encode(username,"UTF-8")+
				"&password="+URLEncoder.encode(password,"UTF-8")+
				"&remember_me=on";
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
				Log.e(TAG,"error1");
				Log.e(TAG,buf.toString());
				return null;
			}
			
			String url2 = conn.getHeaderField("Location");
			boolean repeat = true;
			HttpURLConnection conn2;
			do  {
				Log.v(TAG,"following redir "+url2);
				if (url2.equals("/en/game?")) url2 = "http://www.lordofultima.com/en/game?";
				conn2 = this.doRequest(url2);
				if (conn2.getResponseCode() == 200) {
					repeat = false;
				} else if (conn2.getResponseCode() == 302) {
					url2 = conn2.getHeaderField("Location");
					Log.v(TAG,"302'd to "+url2);
				}
			} while (repeat);

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
	HttpURLConnection doRequest(String url) throws IOException  {
		Log.v(TAG,"Url2:"+url);
		URL secondurl = new URL(base,url);
		HttpURLConnection conn2 = (HttpURLConnection)secondurl.openConnection();
		conn2.setReadTimeout(20000);
		conn2.setConnectTimeout(15000);
		conn2.setRequestMethod("GET");
		conn2.setDoOutput(true);
		conn2.connect();
		return conn2;
	}
	private void parse_result(final result output, InputStream is) throws IOException, SAXException {
		XMLReader xmlReader = XMLReaderFactory.createXMLReader ("org.ccil.cowan.tagsoup.Parser");
		final ArrayList<Account> servers = new ArrayList<Account>();
		final ArrayList<NewServer> newServers = new ArrayList<NewServer>();
		final Pattern actioncheck = Pattern.compile("^http://prodgame(\\d+).lordofultima.com/(\\d+)/index.aspx$");
		final Pattern findid = Pattern.compile("\\d+");
		ContentHandler handler = new DefaultHandler() {
			Account acct;
			NewServer newServer;
			boolean in_server_list = false,in_register_list = false;
			public void startElement(String uri,String localName,String qName, Attributes attributes) throws SAXException {
				String classVal = attributes.getValue("class");
				if (localName.equals("ul") && (classVal != null) && classVal.equals("server-list")) {
					in_server_list = true;
				} else if (localName.equals("form")) {
					String action = attributes.getValue("action");
					String id = attributes.getValue("id");
					Log.v(TAG,String.format("found a form %s %s",action,id));
					Matcher m = actioncheck.matcher(action);
					if (action.equals("/en/user/logout")) {
						output.worked = true;
					} else if (m.find()) {
						acct.serverid = m.group(1);
						acct.pathid = m.group(2);
					} else if ((id != null) && id.equals("servers_new")) {
						Log.v(TAG,"begining of reg list");
						in_register_list = true;
					} else Log.v(TAG,"unknown action "+action);
				} else if (in_server_list) {
					if (localName.equals("li")) {
						String id = attributes.getValue("id");
						Log.v(TAG,"class:"+classVal+" id:"+id);
						acct = new Account();
						acct.world = id; // FIXME
						Matcher m = findid.matcher(id);
						m.find();
						acct.worldid = Integer.parseInt(m.group());
						if (classVal.equals("offline menu_bubble")) {
							acct.offline = true;
						} else {
							acct.offline = false;
						}
					} else if (localName.equals("div")) {
						Log.v(TAG,"div class="+classVal);
					} else if (localName.equals("input")) {
						String name = attributes.getValue("name");
						if (acct != null && name != null && name.equals("sessionId")) {
							acct.sessionid = attributes.getValue("value");
						} else if (acct != null && attributes.getValue("type").equals("submit")) {
							//acct.world = attributes.getValue("value");
						}
					}
				} else if (in_register_list) {
					if (localName.equals("option")) {
						newServer = new NewServer();
						newServer.server = attributes.getValue("value");
						newServer.clazz = attributes.getValue("class");
						newServer.id = attributes.getValue("id");
					}
				}
			}
			public void endElement (String uri, String localName, String qName) {
				if (in_server_list) {
					if (localName.equals("ul")) in_server_list = false;
					else if (localName.equals("li")) {
						Log.v(TAG,"li done, adding acct "+acct);
						servers.add(acct);
						acct = null;
					}
				}
				if (in_register_list) {
					if (localName.equals("option")) {
						newServers.add(newServer);
						newServer = null;
					} else if (localName.equals("form")) {
						Log.v(TAG,"end of reg list");
						in_register_list = false;
					}
				}
			}
			public void characters(char[]text, int start, int size) {
				if (in_register_list && (newServer != null)) {
					newServer.name = new String(text,start,size).trim();
				}
				//String buf = new String(text,start,size);
				//Log.v(TAG,"text: "+buf);
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
					if (cookies.get(x).getName().equals("REMEMBER_ME_COOKIE")) {
						REMEMBER_ME = cookies.get(x).getValue();
					}
				}
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.servers = servers;
			//for (NewServer s : newServers) {
			//	Log.v(TAG,String.format("%s %s %s", s.server,s.id,s.name));
			//}
			return;
		}
		output.error = true;
		output.errmsg = "logout link not found";
		CookieStore store = mCookieManager.getCookieStore();
		Iterator<HttpCookie> i = store.getCookies().iterator();
		while (i.hasNext()) {
			HttpCookie c = i.next();
			System.out.println("cookie dump"+c.toString());
		}
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
			URL check = new URL("https://www.lordofultima.com/en/welcome");
			HttpsURLConnection.setFollowRedirects(false);
			HttpURLConnection.setFollowRedirects(false);
			HttpsURLConnection conn = (HttpsURLConnection) check
					.openConnection();
			conn.setReadTimeout(30000);
			conn.setConnectTimeout(15000);
			conn.setRequestMethod("GET");
			conn.setDoOutput(false);
			conn.setDoInput(true);
			conn.connect();
			if (conn.getResponseCode() != 302) {
				Log.v(TAG, "was expecting 302");
				return null;
				// FIXME
			}
			String secondurl = conn.getHeaderField("Location");
			Log.v(TAG, "second url:" + secondurl);
			if (secondurl.equals("/en/welcome?"))
				secondurl = "http://www.lordofultima.com/en/welcome?";// FIXME
	
			boolean repeat = true;
			HttpURLConnection conn2;
			do {
				conn2 = this.doRequest(secondurl);
				if (conn2.getResponseCode() == 200) {
					repeat = false;
				} else if (conn2.getResponseCode() == 302) {
					secondurl = conn2.getHeaderField("Location");
					Log.v(TAG, "302'd to " + secondurl);
				}
			} while (repeat);
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
}
