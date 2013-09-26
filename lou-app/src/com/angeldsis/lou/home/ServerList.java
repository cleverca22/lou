package com.angeldsis.lou.home;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import com.angeldsis.lou.AccountWrap;
import com.angeldsis.lou.ActionbarHandler;
import com.angeldsis.lou.LoggingIn;
import com.angeldsis.lou.R;
import com.angeldsis.lou.SessionKeeper;
import com.angeldsis.lou.SessionKeeper.Session;
import com.angeldsis.louapi.Account;
import com.angeldsis.louapi.LouSession;
import com.angeldsis.louapi.LouState;
import com.angeldsis.louapi.RPC;
import com.angeldsis.louapi.RPC.SubRequestDone;
import com.angeldsis.louapi.ServerInfo;
import com.angeldsis.louapi.data.SubRequest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.TextView;

public class ServerList extends Fragment implements OnClickListener {
	public static class Result {
		protected int latest;
	}
	private static final String TAG = "ServerList";
	boolean checked = false;
	private AsyncTask<Void, Void, Result> desync;
	private PackageInfo self;
	private ViewGroup updateDiv;
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		if (SessionKeeper.session2 == null) {
			getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Loading()).commit();
			Log.v(TAG,"returning empty list");
			return null;
		}
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.server_list, container,false);
		TextView version = (TextView)root.findViewById(R.id.app_version);
		PackageManager pm = getActivity().getPackageManager();
		try {
			self = pm.getPackageInfo("com.angeldsis.lou", 0);
			version.setText(self.versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			version.setText("error");
		}
		updateDiv = (ViewGroup) root.findViewById(R.id.updateDiv);
		Button doupdate = (Button) root.findViewById(R.id.doUpate);
		doupdate.setOnClickListener(this);
		return root;
	}
	public void onResume() {
		super.onResume();
		redoList();
		if (!checked) {
			desync = new AsyncTask<Void,Void,Result> () {
				@Override protected Result doInBackground(Void... params) {
					try {
						URL url = new URL("http://klingon.angeldsis.com/apks/latestversion");
						HttpURLConnection conn = (HttpURLConnection) url.openConnection();
						InputStreamReader irs = new InputStreamReader(conn.getInputStream());
						char[] buffer = new char[100];
						int size = irs.read(buffer);
						buffer[size] = 0;
						Result r = new Result();
						r.latest = Integer.parseInt(new String(buffer).trim());
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
				protected void onPostExecute(Result r) {
					if (r == null) return;
					if (self.versionCode < r.latest) {
						updateDiv.setVisibility(View.VISIBLE);
					}
					checked = true;
				}
			};
			desync.execute();
		}
	}
	// FIXME, find a better solution?
	private static class Active {
		public Active(Session s) {
			// FIXME, detect if its a sub or not
			pathid = s.acct.pathid;
			playerid = s.state.self.getId();
		}
		int pathid;
		int playerid;
		/**
		 * checks the array to see if it matches pathid/playerid
		 * @param active
		 * @param pathid
		 * @param playerid -1 means to match any non-sub account
		 * @return
		 */
		public static boolean contains(ArrayList<Active> active, int pathid, int playerid) {
			Iterator<Active> i = active.iterator();
			while (i.hasNext()) {
				Active a = i.next();
				if (a.pathid == pathid) {
					// FIXME, check to see if its non-sub
					if (playerid == -1) return true;
					else if (playerid == a.playerid) return true;
				}
			}
			return false;
		}
	}
	private void redoList() {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		ViewGroup root = (ViewGroup) getView();
		ArrayList<Active> active = new ArrayList<Active>();
		
		LouSession sess = SessionKeeper.session2;
		TextView age = (TextView) root.findViewById(R.id.age);
		age.setText(""+(System.currentTimeMillis() - sess.state.dataage)/1000);

		ViewGroup list2 = (ViewGroup) root.findViewById(R.id.livelist);
		list2.removeAllViews();
		SessionKeeper k = SessionKeeper.getInstance();
		if (k != null) {
			//Log.v(TAG,"found keeper");
			Iterator<Session> i2 = k.sessions.iterator();
			//Log.v(TAG,"list:"+k.sessions.size());
			// for each active session
			while (i2.hasNext()) {
				final Session s = i2.next();
				ViewGroup row = (ViewGroup) inflater.inflate(R.layout.one_server, list2,false);
				TextView t = (TextView) row.findViewWithTag("server_name");
				if (s.loggingIn) {
					t.setText("still logging in");
				} else {
					if (s.state.self == null) throw new IllegalStateException("unexpected null, loggingin:"+s.loggingIn);
					t.setText(s.acct.world+" "+s.state.self.getName());
					Log.v(TAG,"source worldid:"+s.acct.worldid+" "+s.state.self.getName()+"("+s.state.self.getId()+")");
				}
				Button b = (Button) row.findViewWithTag("button");
				//Log.v(TAG,b.toString());
				b.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Log.v(TAG,"onClick("+s.acct.world+")");
						Intent login = new Intent(getActivity(), LoggingIn.class);
						login.putExtras(s.acct.toBundle());
						startActivity(login);
						Log.v(TAG, "doing login on world " + s.acct.world);
					}
				});
				list2.addView(row);
				
				active.add(new Active(s));
			}
		}
		ArrayList<ServerInfo> accounts = sess.state.servers;
		Log.v(TAG,"found "+accounts.size());
		ViewGroup top = (ViewGroup) root.findViewById(R.id.list);
		top.removeAllViews();
		Iterator<ServerInfo> i = SessionKeeper.session2.state.servers.iterator();
		while (i.hasNext()) {
			final ServerInfo a = i.next();
			
			// FIXME, provide the real playerid?
			if (Active.contains(active,a.pathid,-1)) {
			} else if (a.offline) {
				ViewGroup row = (ViewGroup) inflater.inflate(R.layout.offline_server, top,false);
				TextView t = (TextView) row.findViewById(R.id.servername);
				t.setText(a.servername);
				top.addView(row);
			} else {
				ViewGroup row = (ViewGroup) inflater.inflate(R.layout.one_server, top,false);
				TextView t = (TextView) row.findViewWithTag("server_name");
				t.setText(a.servername);
				Button b = (Button) row.findViewWithTag("button");
				b.setOnClickListener(new OnClickListener() {
					@Override public void onClick(View v) {
						Log.v(TAG,"onClick("+a.servername+")");
						Intent login = new Intent(getActivity(), LoggingIn.class);
						AccountWrap a2 = new AccountWrap(a);
						login.putExtras(a2.toBundle());
						startActivity(login);
						Log.v(TAG, "doing login on world " + a.servername);
					}
				});
				top.addView(row);
			}
		}
		if (k != null) {
			showSubRequests(active,inflater);
		}
	}
	private void showSubRequests(ArrayList<Active> active, LayoutInflater inflater) {
		TextView t;
		Button b;
		ViewGroup root = (ViewGroup) getView();
		ViewGroup subs_list = (ViewGroup) root.findViewById(R.id.subs_list);
		subs_list.removeAllViews();
		SessionKeeper k = SessionKeeper.getInstance();
		Iterator<Session> i2 = k.sessions.iterator();
		while (i2.hasNext()) {
			final Session s = i2.next();
			LouState state = s.state;
			ArrayList<SubRequest> subs = state.subs;
			Iterator<SubRequest> i3 = subs.iterator();
			// for each sub within this session
			while (i3.hasNext()) {
				final SubRequest sr = i3.next();
				// FIXME, remove duplicates
				Log.v(TAG,"sr data id:"+sr.id+" "+(sr.role == SubRequest.Role.giver ? "giver":"receiver")+" state:"+sr.state);
				if (sr.state != 2) continue;
				if (sr.role != SubRequest.Role.receiver) continue;
				Log.v(TAG,"looking for:"+sr.giver.getId());
				if (Active.contains(active, s.acct.pathid, sr.giver.getId())) {
					Log.v(TAG,"session open already");
					continue;
				}
				ViewGroup row3 = (ViewGroup) inflater.inflate(R.layout.one_server, subs_list, false);
				t = (TextView) row3.findViewWithTag("server_name");
				t.setText("sub for "+sr.giver.getName()+" on "+s.acct.world);
				b = (Button) row3.findViewWithTag("button");
				b.setOnClickListener(new OnClickListener() {
					@Override public void onClick(View v) {
						GetSubSession gss = new GetSubSession(s,sr);
						gss.fire();
					}
				});
				subs_list.addView(row3);
			}
		}
	}
	private class GetSubSession implements SubRequestDone {
		RPC rpc;
		private SubRequest sr;
		public GetSubSession(Session s, SubRequest sr) {
			rpc = s.rpc;
			this.sr = sr;
		}
		public void fire() {
			rpc.CreateSubstitutionSession(sr, this);
		}
		@Override public void allDone(Account acct2) {
			Log.v(TAG,"got sessionid");
			Intent login = new Intent(getActivity(), LoggingIn.class);
			login.putExtras(((AccountWrap) acct2).toBundle());
			
			startActivity(login);
		}
	}
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.server_list, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	@Override public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refresh: {
			SessionKeeper.session2.state.servers = null;
			FragmentTransaction trans = getActivity().getSupportFragmentManager().beginTransaction();
			trans.replace(R.id.main_frame, new Loading());
			trans.commit();
			return true;
		}
		case R.id.logout:
			Log.v(TAG,"doing logout");
			SharedPreferences.Editor trans = getActivity().getSharedPreferences("main", Context.MODE_PRIVATE).edit();
			trans.remove("cookie");
			trans.commit();
			SessionKeeper.session2.logout();
			getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Loading()).commit();
			CookieSyncManager.createInstance(getActivity());
			android.webkit.CookieManager cookies = android.webkit.CookieManager.getInstance();
			cookies.removeAllCookie();
			return true;
		/*case R.id.update:
			Uri location = Uri.parse("http://andoria.angeldsis.com/apks/LouMain.apk");
			Intent i = new Intent(Intent.ACTION_VIEW,location);
			startActivity(i);
			return true;*/
		}
		return ActionbarHandler.handleMenu(item.getItemId(), getActivity(), null, null);
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.doUpate:
			ActionbarHandler.handleMenu(R.id.update, getActivity(), null, null);
			break;
		}
	}
}
