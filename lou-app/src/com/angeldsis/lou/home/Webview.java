package com.angeldsis.lou.home;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import com.angeldsis.lou.AccountWrap;
import com.angeldsis.lou.LoggingIn;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class Webview extends Fragment {
	private static final String TAG = "Webview";
	WebView v;
	LoadPage loadpage;
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		v = new WebView(getActivity());
		v.setWebViewClient(new client());
		v.getSettings().setJavaScriptEnabled(true);
		v.loadUrl("http://www.lordofultima.com");
		return v;
	}
	public class client extends WebViewClient {
		public boolean shouldOverrideUrlLoading (WebView view, String url) {
			try {
				Log.v(TAG,"shouldOverrideUrlLoading "+url);
				if (url.equals("http://www.lordofultima.com/game/launch/redirect")) {
					File path = getActivity().getDatabasePath("webviewCookiesChromium.db");
					Log.v(TAG,"db path: "+path.getAbsolutePath());
					SQLiteDatabase db = SQLiteDatabase.openDatabase(path.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
					String[] vals = {"www.lordofultima.com"};
					Cursor rows = db.rawQuery("SELECT name,value FROM cookies WHERE host_key = ?",vals);
					CookieManager mCookieManager = (CookieManager) CookieHandler.getDefault();
					CookieStore store = mCookieManager.getCookieStore();
					URI uri = new URI("http://www.lordofultima.com/");
					while (rows.moveToNext()) {
						String name = rows.getString(0);
						String value = rows.getString(1);
						Log.v(TAG,String.format("%s=%s",name,value));
						HttpCookie httpcookie = new HttpCookie(name,value);
						httpcookie.setDomain("www.lordofultima.com");
						httpcookie.setPath("/");
						httpcookie.setVersion(0);
						store.add(uri, httpcookie);
					}
					db.close();
					loadpage = new LoadPage();
					loadpage.execute(url);
					return true;
				}
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
				if (code != 200) return null;
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
				if (!m.find()) return null; // FIXME
				result r = new result();
				r.sessionid = m.group(0);
				final Pattern actioncheck = Pattern.compile("http://prodgame(\\d+).lordofultima.com/(\\d+)/index.aspx");
				m = actioncheck.matcher(html);
				if (!m.find()) return null; // FIXME
				r.serverid = m.group(1);
				r.pathid = m.group(2);
				return r;
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
				v.loadUrl("http://www.lordofultima.com");
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
}