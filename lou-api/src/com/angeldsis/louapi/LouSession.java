package com.angeldsis.louapi;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.angeldsis.louapi.HttpUtil.HttpReply;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class LouSession {
	public static class NewServer {
		protected String server;
		protected String clazz;
		protected String id;
		protected String name;
	}
	public static class SessionState {
		transient public ArrayList<ServerInfo> servers;
		public String JSESSIONID = null;
		public String AWSELB = null;
		public String currentEmail;
		public String sessionId;
		public long dataage;
	}
	public SessionState state;
	static final String TAG = "LouSession";
	private HttpUtil httpUtil;
	public LouSession(HttpUtil httpUtil) {
		this.httpUtil = httpUtil;
		state = new SessionState();
	}
	public boolean restoreState(String cookie) {
		Gson gson = new Gson();
		try {
			state = gson.fromJson(cookie, SessionState.class);
		} catch (JsonSyntaxException e) {
			state = new SessionState();
		}
		httpUtil.restoreState(state);
		return true;
	}
	public String getState() {
		httpUtil.syncCookieState(state);
		Gson gson = new Gson();
		return gson.toJson(state);
	}
	public result startLogin(String username,String password) {
		try {
			String data = "j_username="+httpUtil.encode(username)+
					"&j_password="+httpUtil.encode(password);
			HttpReply reply = httpUtil.postUrl("https://www.lordofultima.com/j_security_check",data);

			int response = reply.code;
			Log.v(TAG,"response code "+response);
			if (response != 302) {
				// FIXME error;
				char[] buffer = new char[1024];
				int size;
				StringBuilder buf = new StringBuilder();
				InputStreamReader reply1 = new InputStreamReader(reply.stream);
				while ((size = reply1.read(buffer, 0, 1024)) != -1) {
					buf.append(buffer,0,size);
				}
				Log.e(TAG,"error3 "+buf.toString());
				return null;
			}
			httpUtil.dumpCookies();
			
			String url2 = reply.location;
			Log.v(TAG,"url2:"+url2);
			boolean repeat = true;
			HttpReply reply2;
			do {
				Log.v(TAG,"following redir "+url2);
				if (url2.equals("/en/game?")) url2 = "http://www.lordofultima.com/en/game?";
				reply2 = httpUtil.getUrl(url2);
				response = reply2.code;
				Log.v(TAG,"response code "+response);
				if (response == 200) {
					repeat = false;
				} else if (response == 301) {
					url2 = reply2.location;
					Log.v(TAG,"302'd to "+url2);
				}
			} while (repeat);

			Log.v(TAG,"Stage 1");
			char[] buffer = new char[1024];
			int size;
			StringBuilder buf = new StringBuilder();
			InputStreamReader reply1 = new InputStreamReader(reply2.stream);
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
				result r = new result();
				r.worked = false;
				r.errmsg = "sessionid not found";
				return r;
			}
			state.sessionId = m.group(0);
			state.dataage = System.currentTimeMillis();
			Log.v(TAG,"sessionid "+state.sessionId);
			
			result output = checkSessionId();
			
			if (output.worked) state.currentEmail = username;
			return output;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			result output = new result();
			output.errmsg = "IO error durring login";
			output.worked = false;
			output.e = e;
			return output;
		}
	}
	private void parse_result(final result output, InputStream is) throws IOException, SAXException {
		XMLReader xmlReader = XMLReaderFactory.createXMLReader ("org.ccil.cowan.tagsoup.Parser");
		final ArrayList<ServerInfo> servers = new ArrayList<ServerInfo>();
		//final ArrayList<NewServer> newServers = new ArrayList<NewServer>();
		final Pattern actioncheck = Pattern.compile("^http://prodgame(\\d+).lordofultima.com/(\\d+)/index.aspx");
		final Pattern findid = Pattern.compile("\\d+");
		ContentHandler handler = new DefaultHandler() {
			//NewServer newServer;
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
						currentRow.servername = buf.trim();
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
							currentRow.pathid = Integer.parseInt(m.group(2));
						}
					}
				} else if (size > 0) {
					//String buf = new String(text,start,size);
					//Log.v(TAG,"text: "+state+" "+size+" "+buf);
				}
			}
		};
		xmlReader.setContentHandler(handler);
		/*
		FilterInputStream wrapper = new FilterInputStream(is) {
			public int read(byte[] buffer, int offset, int count) throws IOException {
				int size = super.read(buffer, offset, count);
				if (size == -1) return size;
				Log.v(TAG,"sniff:"+new String(buffer,offset,size));
				return size;
			}
		};*/
		xmlReader.parse(new InputSource(is)); // swap is with wrapper to debug
		if (output.worked) {
			state.servers = servers;
			//for (NewServer s : newServers) {
			//	Log.v(TAG,String.format("%s %s %s", s.server,s.id,s.name));
			//}
			return;
		}
		output.errmsg = "logout link not found";
		httpUtil.dumpCookies();
		System.out.println("done");
	}
	public static class result {
		int code;
		String result;
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
	public result checkCookie(String username) {
		try {
			Log.v(TAG,"checking cookies");
			HttpReply reply = httpUtil.getUrl("http://www.lordofultima.com/game/world/change");
			if (reply.e != null) {
				result obj = new result();
				obj.worked = false;
				obj.e = reply.e;
				return obj;
			}
			if (reply.code == 200) {
			} else if (reply.code == 302) {
				String secondurl = reply.location;
				if (secondurl.startsWith("https://www.lordofultima.com/login/auth")) {
					result obj = new result();
					Log.e(TAG,String.format("fail 1 %d %s",reply.code,secondurl));
					obj.worked = false;
					state.currentEmail = null;
					return obj;
				} else {
					Log.e(TAG,"unknown error: "+secondurl);
					return null; // FIXME
				}
			} else {
				Log.e(TAG, "was expecting 302, got "+reply.code);
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
			InputStreamReader reply1 = new InputStreamReader(reply.stream);
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
			state.sessionId = m.group(0);
			Log.v(TAG,"sessionid "+state.sessionId);

			result output = checkSessionId();
			if (output.worked) state.currentEmail = username;
			return output;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public void logout() {
		httpUtil.logout();
		state = new SessionState();
	}
	public result checkSessionId() {
		if (state.sessionId == null) {
			Log.v(TAG,"checkSessionId ran with null!");
			return null;
		}
		HttpReply reply2 = httpUtil.getUrl("http://prodgame.lordofultima.com/Farm/service.svc/ajaxEndpoint/1/session/"+
				state.sessionId+"/worlds");
		if (reply2.code == 200) {
		} else {
			Log.e(TAG,String.format("unknown error %s",reply2.code));
			return null; // FIXME
		}
		
		final result output = new result();
		output.worked = false;
		try {
			parse_result(output, reply2.stream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}
	// does some basic tests, to see if its even worth attempting to test the cookies
	public boolean cookiesLookBad() {
		if (state.JSESSIONID == null) return true;
		return false;
	}
}
