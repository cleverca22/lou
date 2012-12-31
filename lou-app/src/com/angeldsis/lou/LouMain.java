package com.angeldsis.lou;

import java.io.FilterInputStream;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
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
	protected ArrayList<AccountWrap> accounts;
	int state;
	public CookieManager mCookieManager;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		//Debug.startMethodTracing();
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
				this.finish();
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
			//Debug.stopMethodTracing();
		state = 4;
		break;
	case 4:
		Log.v(TAG,"state 4, already settled");
    	}
    }
    class MyAdapter extends FragmentPagerAdapter {
    	ArrayList<AccountWrap> accounts;
		public MyAdapter(FragmentManager fm,ArrayList<AccountWrap> accounts2) {
			super(fm);
			this.accounts = accounts2;
			Display dsp = getWindowManager().getDefaultDisplay();
			int rot = dsp.getOrientation();
			//Log.i(TAG,"rot:"+rot);
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
    public static class ArrayListFragment extends Fragment implements View.OnClickListener {
    	private AccountWrap account;
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
		if (account.offline) {
			View v = inflater.inflate(R.layout.world_offline, container,false);
			TextView tv = (TextView) v.findViewById(R.id.worldId);
			tv.setText(account.world);
			return v;
		} else {
			View v = inflater.inflate(R.layout.world_login,container,false);
				TextView tv = (TextView) v.findViewById(R.id.worldId);
				tv.setText(account.world);
				Log.v(TAG,"onCreateView "+account.world);
				View button = v.findViewById(R.id.login);
				button.setOnClickListener(this);
				return v;
		}
	}
		@Override
		public void onClick(View v) {
			((LouMain)this.getActivity()).world_login(account);
		}
    }
    protected void onStart() {
    	super.onStart();
    	Log.v(TAG,"onStart");
    	stateEngine();
    }
	public void world_login(AccountWrap account) {
		Intent login = new Intent(this,LouSessionMain.class);
//		AudioTrack click = new AudioTrack(AudioManager.STREAM_MUSIC,44100,AudioTrack.CHANNEL_OUT_MONO,ENCODING_PCM_16BIT,20096,MODE_STATIC);
//		byte[] data;
//		click.write(data,0,20096);
		login.putExtras(account.toBundle());
		startActivity(login);
		Log.v(TAG,"doing login on world "+account.world);
	}
	private class result {
    	boolean logged_in;
    	ArrayList<AccountWrap> servers;
		IOException exception;
		boolean error;
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
				String secondurl = conn.getHeaderField("Location");
				Log.v(TAG,"second url:"+secondurl);
				if (secondurl.equals("/en/welcome?")) secondurl = "http://www.lordofultima.com/en/welcome?";// FIXME

				boolean repeat = true;
				HttpURLConnection conn2;
				do  {
					conn2 = this.doRequest(secondurl);
					if (conn2.getResponseCode() == 200) {
						repeat = false;
					} else if (conn2.getResponseCode() == 302) {
						secondurl = conn2.getHeaderField("Location");
						Log.v(TAG,"302'd to "+secondurl);
					}
				} while (repeat);
								
				final result output = new result();
				output.logged_in = false;
				XMLReader xmlReader = XMLReaderFactory.createXMLReader ("org.ccil.cowan.tagsoup.Parser");
				final ArrayList<AccountWrap> servers = new ArrayList<AccountWrap>();
				final Pattern actioncheck = Pattern.compile("^http://prodgame(\\d+).lordofultima.com/(\\d+)/index.aspx$");
				ContentHandler handler = new DefaultHandler() {
					AccountWrap acct;
					boolean in_server_list = false;
					public void startElement(String uri,String localName,String qName,
							Attributes attributes) throws SAXException {
						String classVal = attributes.getValue("class");
						if (localName.equals("ul") && (classVal != null) && classVal.equals("server-list")) {
							in_server_list = true;
						} else if (localName.equals("form")) {
							//Log.v(TAG,"found a form");
							String action = attributes.getValue("action");
							Matcher m = actioncheck.matcher(action);
							if (action.equals("/en/user/logout")) {
								output.logged_in = true;
							} else if (m.find()) {
								acct.serverid = m.group(1);
								acct.pathid = m.group(2);
							} else Log.v(TAG,"action "+action);
						} else if (in_server_list) {
							if (localName.equals("li")) {
								String id = attributes.getValue("id");
								//Log.v(TAG,"class:"+classVal+" id:"+id);
								acct = new AccountWrap();
								acct.world = id; // FIXME
								if (classVal.equals("offline menu_bubble")) {
									acct.offline = true;
								} else {
									acct.offline = false;
								}
							} else if (localName.equals("div")) {
								//Log.v(TAG,"div class="+classVal);
							} else if (localName.equals("input")) {
								String name = attributes.getValue("name");
								if (acct != null && name != null && name.equals("sessionId")) {
									acct.sessionid = attributes.getValue("value");
								} else if (acct != null && attributes.getValue("type").equals("submit")) {
									//acct.world = attributes.getValue("value");
								}
							}
						}
					}
					public void endElement (String uri, String localName, String qName) {
						if (in_server_list) {
							if (localName.equals("ul")) in_server_list = false;
							else if (localName.equals("li")) {
								//Log.v(TAG,"li done, adding acct "+acct);
								servers.add(acct);
								acct = null;
							}
						}
					}
				};
				xmlReader.setContentHandler(handler);
				Log.v(TAG,"parsing");
				FilterInputStream wrapper = new FilterInputStream(conn2.getInputStream()) {
					public int read(byte[] buffer, int offset, int count) throws IOException {
						int size = super.read(buffer, offset, count);
						if (size == -1) return size;
						// FIXME, fully disable
						//Log.v(TAG,"sniff:"+new String(buffer,offset,size));
						return size;
					}
				};
				xmlReader.parse(new InputSource(wrapper));
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
				result output = new result();
				output.error = true;
				output.exception = e;
				return output;
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		protected void onPostExecute(result r) {
			parent.handleResults(r);
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
	}
	public void handleResults(result r) {
		// TODO Auto-generated method stub
		if (r.logged_in) {
			accounts = r.servers;
			state = 3;
			stateEngine();
		} else if (r.error) {
			Log.e(TAG,"error doing request",r.exception);
			finish();
		} else {
			SharedPreferences.Editor trans = getSharedPreferences("main",MODE_PRIVATE).edit();
			trans.putString("cookie", null);
			trans.commit();
			state = 0;
			stateEngine();
		}
	}
}
