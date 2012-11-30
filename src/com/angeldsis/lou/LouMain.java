package com.angeldsis.lou;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class LouMain extends FragmentActivity {
	public static final String TAG = "LouMain";
	MyAdapter mAdapter;
	ViewPager mPager;
	String cookie = null;
	protected ArrayList<Account> accounts;
	int state;
	public CookieManager mCookieManager;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);
		mCookieManager = new CookieManager();
		CookieHandler.setDefault(mCookieManager);
        state = 0;
        stateEngine();
    }
    void stateEngine() {
    	switch (state) {
    	case 0: // initial startup, check cookie to see if it exists
        	cookie = this.getSharedPreferences("main", MODE_PRIVATE).getString("cookie", null);
        	if (cookie == null) { // not logged in, open login page
        		Intent doLogin = new Intent(this,louLogin.class);
        		startActivity(doLogin);
        		return;
        	} else {
        		Log.v(TAG,"found cookie "+cookie);
        		HttpCookie httpcookie = new HttpCookie("REMEMBER_ME_COOKIE",cookie);
        		httpcookie.setDomain("www.lordofultima.com");
        		httpcookie.setPath("/");
        		httpcookie.setVersion(0);
				try {
					mCookieManager.getCookieStore().add(new URI("http://www.lordofultima.com/"), httpcookie);
					Log.v(TAG,"cookie restored?");
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					Log.wtf(TAG, "hard-coded uri not valid",e);
				}
        	}
        	state = 1;
    	case 1:{ // cookie exists, check that its valid and get worlds list
    		Log.v(TAG,"state 1");
    		checkCookie checker = (checkCookie) new checkCookie(this).execute(cookie);
    		state = 2;
    		break;
    	}
    	case 2:
    		Log.v(TAG,"state 2");
    		break;
    	case 3: // accounts is populated, show main UI
    		Log.v(TAG,"state 3, changing layout");
    		setContentView(R.layout.main);
    		mAdapter = new MyAdapter(getSupportFragmentManager(),accounts);
    		mPager = (ViewPager)findViewById(R.id.pager);
    		mPager.setAdapter(mAdapter);
    	}
    }
    class Account {
	String world;
	String pathid;
	String serverid;
	String action;
	protected String sessionid;
    }
    class MyAdapter extends FragmentPagerAdapter {
    	ArrayList<Account> accounts;
		public MyAdapter(FragmentManager fm,ArrayList<Account> accounts2) {
			super(fm);
			this.accounts = accounts2;
			Display dsp = getWindowManager().getDefaultDisplay();
			int rot = dsp.getOrientation();
			Log.i(TAG,"rot:"+rot);
		}
		@Override
		public int getCount() {
			return accounts.size();
		}
		public float getPageWidth(int position) {
			return 0.5f;
		}
		@Override
		public Fragment getItem(int arg0) {
			return ArrayListFragment.newInstance(arg0);
		}
    };
    public static class ArrayListFragment extends Fragment {
    	private Account account;
    	static Fragment newInstance(int acctOffset) {
    		ArrayListFragment frag = new ArrayListFragment();
    		Bundle args = new Bundle();
    		args.putInt("acctOffset", acctOffset);
    		frag.setArguments(args);
    		return frag;
    	}
    	public void onCreate(Bundle savedInstanceState) {
    		super.onCreate(savedInstanceState);
    		int acctOffset = getArguments() != null ? getArguments().getInt("acctOffset") : -1;
    		this.account = ((LouMain)this.getActivity()).accounts.get(acctOffset);
    	}
    	public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v = inflater.inflate(R.layout.world_login,container,false);
			TextView tv = (TextView) v.findViewById(R.id.worldId);
			tv.setText(""+account.world);
			Log.v(TAG,"onCreateView "+account.world);
			return v;
    	}
    }
    protected void onStart() {
    	super.onStart();
    	Log.v(TAG,"onStart");
    	stateEngine();
    }
    private class result {
    	boolean logged_in;
    	ArrayList<Account> servers;
    }
	private class checkCookie extends AsyncTask<String,Integer,result> {
		LouMain parent;
		public checkCookie(LouMain louMain) {
			parent = louMain;
		}
		@Override
		protected result doInBackground(String... params) {
			try {
				Log.v(TAG,"checking cookie....");
				URL check = new URL("https://www.lordofultima.com/en/welcome");
				HttpsURLConnection conn = (HttpsURLConnection) check.openConnection();
				conn.setReadTimeout(30000);
				conn.setConnectTimeout(15000);
				conn.setRequestMethod("GET");
				conn.setDoOutput(false);
				conn.setDoInput(false);
				conn.connect();
				if (conn.getResponseCode() != 302) {
					Log.v(TAG,"was expecting 302");
					return null;
					// FIXME
				}
				URL redir = new URL(conn.getHeaderField("Location"));
				HttpURLConnection conn2 = (HttpURLConnection) redir.openConnection();
				conn2.setReadTimeout(30000);
				conn2.setConnectTimeout(15000);
				conn2.setRequestMethod("GET");
				conn2.setDoOutput(false);
				conn2.setDoInput(true);
				conn2.connect();
				final result output = new result();
				output.logged_in = false;
				XMLReader xmlReader = XMLReaderFactory.createXMLReader ("org.ccil.cowan.tagsoup.Parser");
				final ArrayList<Account> servers = new ArrayList<Account>();
				final Pattern actioncheck = Pattern.compile("^http://prodgame(\\d+).lordofultima.com/(\\d+)/index.aspx$");
				ContentHandler handler = new DefaultHandler() {
					Account acct;
					public void startElement(String uri,String localName,String qName,
							Attributes attributes) throws SAXException {
						if (localName.equals("form")) {
							Log.v(TAG,"found a form");
							String action = attributes.getValue("action");
							Matcher m = actioncheck.matcher(action);
							if (action.equals("/en/user/logout")) {
								output.logged_in = true;
							} else if (m.find()) {
								acct = new Account();
								acct.serverid = m.group(1);
								acct.pathid = m.group(2);
							} else Log.v(TAG,"action "+action);
						} else if (localName.equals("input")) {
							String name = attributes.getValue("name"); 
							if (acct != null && name != null && name.equals("sessionId")) {
								acct.sessionid = attributes.getValue("value");
							} else if (acct != null && attributes.getValue("type").equals("submit")) {
								acct.world = attributes.getValue("value");
								servers.add(acct);
								acct = null;
							}
						}
					}
				};
				xmlReader.setContentHandler(handler);
				Log.v(TAG,"parsing");
				xmlReader.parse(new InputSource(conn2.getInputStream()));
				Log.v(TAG,"cookie "+conn2.getHeaderField("Location"));
				Log.v(TAG,"code "+conn2.getResponseCode());
				output.servers = servers;
				return output;
			} catch (UnknownHostException e) {
				Log.e(TAG,"dns error",e);
			} catch (ProtocolException e) {
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
		protected void onPostExecute(result r) {
			parent.handleResults(r);
		}
	}
	public void handleResults(result r) {
		// TODO Auto-generated method stub
		if (r.logged_in) {
			accounts = r.servers;
			state = 3;
			stateEngine();
		} else {
			SharedPreferences.Editor trans = getSharedPreferences("main",MODE_PRIVATE).edit();
			trans.putString("cookie", null);
			trans.commit();
			state = 0;
			stateEngine();
		}
	}
}
