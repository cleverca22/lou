package com.angeldsis.lou.home;

import java.util.ArrayList;
import java.util.Iterator;

import com.angeldsis.lou.AccountWrap;
import com.angeldsis.lou.LoggingIn;
import com.angeldsis.lou.R;
import com.angeldsis.lou.SessionKeeper;
import com.angeldsis.louapi.Account;
import com.angeldsis.louapi.LouSession;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
			getActivity().getSupportFragmentManager().beginTransaction().add(R.id.main_frame, new Loading()).commit();
			Log.v(TAG,"returning empty list");
			return null;
		}
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.server_list, container,false);
		LouSession sess = SessionKeeper.session2;
		ArrayList<Account> accounts = sess.servers;
		Log.v(TAG,"found "+accounts.size());
		ViewGroup top = (ViewGroup) root.findViewById(R.id.list);
		Iterator<Account> i = SessionKeeper.session2.servers.iterator();
		while (i.hasNext()) {
			final Account a = i.next();
			if (a.offline) {
				ViewGroup row = (ViewGroup) inflater.inflate(R.layout.offline_server, top,false);
				TextView t = (TextView) row.findViewById(R.id.servername);
				t.setText(a.world);
				top.addView(row);
			} else {
				ViewGroup row = (ViewGroup) inflater.inflate(R.layout.one_server, top,false);
				TextView t = (TextView) row.findViewWithTag("server_name");
				t.setText(a.world);
				Button b = (Button) row.findViewWithTag("button");
				Log.v(TAG,b.toString());
				b.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Log.v(TAG,"onClick("+a.world+")");
						Intent login = new Intent(getActivity(), LoggingIn.class);
						login.putExtras((new AccountWrap(a)).toBundle());
						startActivity(login);
						Log.v(TAG, "doing login on world " + a.world);
					}
				});
				top.addView(row);
				Log.v(TAG,"inflated row "+a.world);
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
}
