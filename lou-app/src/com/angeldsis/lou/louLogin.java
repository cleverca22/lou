package com.angeldsis.lou;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class louLogin extends Activity {
	static String TAG = "louLogin";
	public CookieManager mCookieManager;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG,"onCreate");
		setContentView(R.layout.login_page);
		mCookieManager = new CookieManager();
		CookieHandler.setDefault(mCookieManager);
		String email = getSharedPreferences("main",MODE_PRIVATE).getString("email", null);
		String password = getSharedPreferences("main",MODE_PRIVATE).getString("password", null);
		if (email != null && password != null) {
			((EditText)findViewById(R.id.username)).setText(email);
			((EditText)findViewById(R.id.password)).setText(password);
			((CheckBox)findViewById(R.id.save_pw)).setChecked(true);
		}
	}
	public void doLogin(View view) throws MalformedURLException {
		CharSequence username = ((EditText)findViewById(R.id.username)).getText();
		CharSequence password = ((EditText)findViewById(R.id.password)).getText();
		loginInfo info = new loginInfo();
		info.username = username.toString();
		info.password = password.toString();
		CheckBox save = (CheckBox) findViewById(R.id.save_pw);
		boolean savePw = save.isChecked();
		if (savePw) {
			SharedPreferences.Editor trans = this.getSharedPreferences("main", MODE_PRIVATE).edit();
			trans.putString("email", info.username);
			trans.putString("password", info.password);
			trans.commit();
		}
		Log.v(TAG,"starting login");
		@SuppressWarnings("unused")
		doLogin async = (doLogin) new doLogin(this).execute(info);
	}
	private class loginInfo {
		String username,password;
	}
	private class result {
		int code;
		String result;
		boolean error,worked;
		String errmsg;

		Exception e;
	}
	private class doLogin extends AsyncTask<loginInfo,Integer,result> {
		louLogin parent;
		public doLogin(louLogin louLogin) {
			// TODO Auto-generated constructor stub
			this.parent = louLogin;
		}
		@Override
		protected result doInBackground(loginInfo... info) {
			try {
				URL login = new URL("https://www.lordofultima.com/en/user/login");
				// initial login page
				HttpsURLConnection conn = (HttpsURLConnection) login.openConnection();
				conn.setReadTimeout(30000);
				conn.setConnectTimeout(15000);
				conn.setRequestMethod("POST");
				HttpsURLConnection.setFollowRedirects(false);
				String data = "mail="+URLEncoder.encode(info[0].username.toString(),"UTF-8")+
						"&password="+URLEncoder.encode(info[0].password.toString(),"UTF-8")+
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
				XMLReader xmlReader = XMLReaderFactory.createXMLReader ("org.ccil.cowan.tagsoup.Parser");
				ContentHandler handler = new DefaultHandler() {
					public void startElement(String uri,String localName,String qName, Attributes attributes) throws SAXException {
						if (localName.equals("form")) {
							Log.v(TAG,"found a form");
							String action = attributes.getValue("action");
							if (action.equals("/en/user/logout")) {
								output.worked = true;
							} else Log.v(TAG,"action is "+action);
						}
					}
					/*public void characters(char[]text, int start, int size) {
						String buf = new String(text,start,size);
						Log.v(TAG,"text: "+buf);
					}*/
				};
				xmlReader.setContentHandler(handler);
				FilterInputStream wrapper = new FilterInputStream(conn2.getInputStream()) {
					public int read(byte[] buffer, int offset, int count) throws IOException {
						int size = super.read(buffer, offset, count);
						if (size == -1) return size;
						//Log.v(TAG,"sniff:"+new String(buffer,offset,size));
						return size;
					}
				};
				xmlReader.parse(new InputSource(wrapper));
				if (output.worked) return output;
				output.error = true;
				output.errmsg = "logout link not found";
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
			URL secondurl = new URL(url);
			HttpURLConnection conn2 = (HttpURLConnection)secondurl.openConnection();
			conn2.setReadTimeout(10000);
			conn2.setConnectTimeout(15000);
			conn2.setRequestMethod("GET");
			conn2.setDoOutput(true);
			conn2.connect();
			return conn2;
		}
		protected void onPostExecute(result r) {
			if (r.error) {
				Log.e(TAG,r.errmsg,r.e);
				// FIXME
				parent.finish();
				return;
			}
			if (r.worked) {
				List<HttpCookie> cookies;
				try {
					cookies = parent.mCookieManager.getCookieStore().get(new URI("http://www.lordofultima.com/"));
					int x;
					for (x = 0; x < cookies.size(); x++) {
						if (cookies.get(x).getName().equals("REMEMBER_ME_COOKIE")) {
							SharedPreferences.Editor trans = getSharedPreferences("main", MODE_PRIVATE).edit();
							trans.putString("cookie", cookies.get(x).getValue());
							trans.commit();
							parent.finish();
							Intent backtomain = new Intent(parent,LouMain.class);
							startActivity(backtomain);
						}
					}
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			}
			if (r.code == 302) {
				//Log.v(TAG,r.result);
			} else {
				// FIXME add error
				Log.v(TAG,"http error");
				Log.v(TAG,r.result);
			}
		}
	}
	public void worldLogin(View v) {
		Button b = (Button) v;
		b.getParent();
	}
}
