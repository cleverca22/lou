package com.angeldsis.lou.home;

import java.util.ArrayList;
import java.util.Iterator;

import com.angeldsis.lou.AccountWrap;
import com.angeldsis.lou.LoggingIn;
import com.angeldsis.lou.R;
import com.angeldsis.lou.SessionKeeper;
import com.angeldsis.louapi.Account;
import com.angeldsis.louapi.LouSession;
import com.angeldsis.louapi.ServerInfo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ServerList extends Fragment {
	private static final String TAG = "ServerList";
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		Log.v(TAG,"onCreateView");
		if (SessionKeeper.session2 == null) {
			getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Loading()).commit();
			Log.v(TAG,"returning empty list");
			return null;
		}
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.server_list, container,false);
		TextView age = (TextView) root.findViewById(R.id.age);
		LouSession sess = SessionKeeper.session2;
		age.setText(""+(System.currentTimeMillis() - sess.dataage)/1000);
		ArrayList<ServerInfo> accounts = sess.servers;
		Log.v(TAG,"found "+accounts.size());
		ViewGroup top = (ViewGroup) root.findViewById(R.id.list);
		Iterator<ServerInfo> i = SessionKeeper.session2.servers.iterator();
		while (i.hasNext()) {
			final ServerInfo a = i.next();
			if (a.offline) {
				ViewGroup row = (ViewGroup) inflater.inflate(R.layout.offline_server, top,false);
				TextView t = (TextView) row.findViewById(R.id.servername);
				t.setText(a.servername);
				top.addView(row);
			} else {
				ViewGroup row = (ViewGroup) inflater.inflate(R.layout.one_server, top,false);
				TextView t = (TextView) row.findViewWithTag("server_name");
				t.setText(a.servername);
				Button b = (Button) row.findViewWithTag("button");
				Log.v(TAG,b.toString());
				b.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Log.v(TAG,"onClick("+a.servername+")");
						Intent login = new Intent(getActivity(), LoggingIn.class);
						AccountWrap a2 = new AccountWrap(a);
						login.putExtras(a2.toBundle());
						startActivity(login);
						Log.v(TAG, "doing login on world " + a.servername);
					}
				});
				top.addView(row);
				Log.v(TAG,"inflated row "+a.servername);
				Log.v(TAG,"serverid:"+a.serverid);
				Log.v(TAG,"offline: "+a.offline);
			}
		}
		return root;
	}
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.server_list, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	@Override public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refresh:
			return false; // FIXME
		case R.id.logout:
			Log.v(TAG,"doing logout");
			SharedPreferences.Editor trans = getActivity().getSharedPreferences("main", Context.MODE_PRIVATE).edit();
			trans.remove("cookie");
			trans.commit();
			SessionKeeper.session2.logout();
			getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Loading()).commit();
			return true;
		case R.id.update:
			Uri location = Uri.parse("http://andoria.angeldsis.com/apks/LouMain.apk");
			Intent i = new Intent(Intent.ACTION_VIEW,location);
			startActivity(i);
			return true;
		}
		return false;
	}
}
