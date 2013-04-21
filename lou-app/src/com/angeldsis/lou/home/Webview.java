package com.angeldsis.lou.home;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.angeldsis.lou.AccountWrap;
import com.angeldsis.lou.LoggingIn;
import com.angeldsis.lou.R;
import com.angeldsis.lou.SessionKeeper;
import com.angeldsis.lou.SessionKeeper.CookieCallback;
import com.angeldsis.louapi.LouSession;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Webview extends Fragment implements CookieCallback {
	private static final String TAG = "Webview";
	WebView v;
	LoadPage loadpage;
	private TextView lastUrl;
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		LinearLayout l = new LinearLayout(getActivity());
		l.setOrientation(LinearLayout.VERTICAL);
		
		lastUrl = new TextView(getActivity());
		v = new WebView(getActivity());
		v.setWebViewClient(new client());
		v.getSettings().setJavaScriptEnabled(true);
		Bundle b = getArguments();
		if (b != null) {
			Log.v(TAG,"arguments found!");
			String username = b.getString("username");
			String pw = b.getString("password");
			String url = "https://www.lordofultima.com/j_security_check";
			String payload = "j_username="+username+"&j_password="+pw;
			v.postUrl(url, payload.getBytes());
		} else v.loadUrl("https://www.lordofultima.com/mobile/");
		
		l.addView(lastUrl);
		l.addView(v);
		Exception e = new Exception();
		Log.v(TAG,"new webview",e);
		return l;
	}
	public class client extends WebViewClient {
		public boolean shouldOverrideUrlLoading (WebView view, String url) {
			lastUrl.setText(url);
			//try {
				Log.v(TAG,"shouldOverrideUrlLoading "+url);
				if (url.equals("http://www.lordofultima.com/mobile/play/change")) {
					Log.v(TAG,"overriding");
					android.webkit.CookieManager cookies = android.webkit.CookieManager.getInstance();
					//File path = getActivity().getDatabasePath("webviewCookiesChromium.db");
					//Log.v(TAG,"db path: "+path.getAbsolutePath());
					//SQLiteDatabase db = SQLiteDatabase.openDatabase(path.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
					//String[] vals = {"www.lordofultima.com"};
					//Cursor rows = db.rawQuery("SELECT name,value FROM cookies WHERE host_key = ?",vals);
					//CookieManager mCookieManager = (CookieManager) CookieHandler.getDefault();
					//CookieStore store = mCookieManager.getCookieStore(); // FIXME NullPointerException
					//URI uri = new URI("http://www.lordofultima.com/");
					String c = cookies.getCookie("http://www.lordofultima.com/");
					Log.v(TAG,"c:"+c);
					String c2[] = c.split(";");
					int x;
					String JSESSIONID=null,AWSELB=null;
					for (x=0; x < c2.length; x++) {
						String c3[] = c2[x].split("=");
						String name = c3[0].trim();
						String value = c3[1];
						Log.v(TAG,String.format("'%s'='%s'",name,value));
						if ("JSESSIONID".equals(name)) JSESSIONID = value;
						else if ("AWSELB".equals(name)) AWSELB = value;
					}
					SessionKeeper.restore_cookie(JSESSIONID+";"+AWSELB);
					SharedPreferences.Editor trans = getActivity().getSharedPreferences("main", Context.MODE_PRIVATE).edit();
					trans.putString("cookie", JSESSIONID+";"+AWSELB);
					trans.commit();
					//db.close();
					//loadpage = new LoadPage();
					//loadpage.execute("http://www.lordofultima.com/game/world/change");
					SessionKeeper.checkCookie(Webview.this);
					return true;
				}
			//} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			//}
			return false;
		}
		@Override public void onReceivedError (WebView view, int errorCode, String description, String failingUrl) {
			Log.v(TAG,String.format("onReceivedError(%s,%d,%s,%s",view,errorCode,description,failingUrl));
		}
	}
	@Override public void onDestroy() {
		v.destroy();
		super.onDestroy();
	}
	private class LoadPage extends AsyncTask<String,Object,result> {
		@Override
		protected result doInBackground(String... arg0) {
			try {
				URL url = new URL(arg0[0]);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				HttpURLConnection.setFollowRedirects(false);
				conn.connect();
				int code = conn.getResponseCode();
				Log.v(TAG,"status code "+code);
				if (code != 200) {
					Log.v(TAG,"location is "+conn.getHeaderField("Location"));
					return null;
				}
				char[] buffer = new char[1024];
				int size;
				StringBuilder buf = new StringBuilder();
				InputStreamReader reply1 = new InputStreamReader(conn.getInputStream());
				while ((size = reply1.read(buffer, 0, 1024)) != -1) {
					buf.append(buffer,0,size);
				}
				String html = buf.toString();
				buf = null;
				Log.e(TAG,"error1 "+html);
				Pattern sessionid = Pattern.compile("[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}");
				Matcher m = sessionid.matcher(html);
				if (!m.find()) {
					Log.e(TAG,"sessionid not found");
					return null; // FIXME
				}
				result r = new result();
				r.sessionid = m.group(0);
				Log.v(TAG,"sessionid:"+r.sessionid);
				
				url = new URL("http://prodcdngame.lordofultima.com/Farm/service.svc/ajaxEndpoint/1/session/"+
						r.sessionid+"/worlds");
				conn = (HttpURLConnection) url.openConnection();
				conn.connect();
				code = conn.getResponseCode();
				Log.v(TAG,"status code "+code);
				if (code != 200) {
					Log.v(TAG,"location is "+conn.getHeaderField("Location"));
					return null;
				}
				buffer = new char[1024];
				buf = new StringBuilder();
				reply1 = new InputStreamReader(conn.getInputStream());
				while ((size = reply1.read(buffer, 0, 1024)) != -1) {
					buf.append(buffer,0,size);
				}
				html = buf.toString();
				buf = null;
				Log.e(TAG,"error2 "+html);
				//final Pattern actioncheck = Pattern.compile("http://prodgame(\\d+).lordofultima.com/(\\d+)/index.aspx");
				//m = actioncheck.matcher(html);
				//if (!m.find()) return null; // FIXME
				//r.serverid = m.group(1);
				//r.pathid = m.group(2);
				//return r;
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		protected void onPostExecute(result r) {
			if (r == null) {
				//v.loadUrl("http://www.lordofultima.com");
				return;
			}
			AccountWrap a = new AccountWrap();
			a.serverid = r.serverid;
			a.pathid = r.pathid;
			a.sessionid = r.sessionid;
			a.world = "fixme";
			a.worldid = 86;
			Intent login = new Intent(getActivity(), LoggingIn.class);
			login.putExtras((new AccountWrap(a)).toBundle());
			startActivity(login);
		}
	}
	static class result {
		public String sessionid;
		public String serverid;
		public String pathid;
	}
	@Override public void done(com.angeldsis.louapi.LouSession.result r) {
		Log.v(TAG,"done?");
		if (r.worked) {
			Log.v(TAG,"worked");
			FragmentTransaction trans = getActivity().getSupportFragmentManager().beginTransaction();
			trans.replace(R.id.main_frame, new ServerList());
			trans.commit();
		} else {
			Log.v(TAG,"failed");
			v.loadUrl("https://www.lordofultima.com/mobile/");
		}
	}
}
