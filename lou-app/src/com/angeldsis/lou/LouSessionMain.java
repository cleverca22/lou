package com.angeldsis.lou;

import java.util.ArrayList;
import java.util.Iterator;

import com.angeldsis.lou.fragments.ResourceBar;
import com.angeldsis.louapi.ChatMsg;
import com.angeldsis.louapi.LouState.City;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class LouSessionMain extends SessionUser implements SessionKeeper.Callbacks, OnItemClickListener {
	static final String TAG = "LouSessionMain";
	ResourceBar resource_bar;
	cityList mAdapter;
	ArrayList<ResourceBar> bars;

	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		Log.v(TAG,"onCreate");
		setContentView(R.layout.session_core);
		resource_bar = new ResourceBar(this);
		((FrameLayout) findViewById(R.id.resource_bar)).addView(resource_bar.self);
		mAdapter = new cityList(this);
		ListView list = (ListView) findViewById(R.id.cities);
		list.setAdapter(mAdapter);
		list.setOnItemClickListener(this);
		bars = new ArrayList<ResourceBar>();
	}
	protected void onStart() {
		super.onStart();
		Log.v(TAG,"onStart");
	}
	protected void session_ready() {
		Log.v(TAG,"session_ready");
		if (session.alive == false) {
			onEjected();
		} else {
			int total = session.state.chat_history.size();
			Button chat = (Button) findViewById(R.id.chat);
			chat.setText("chat ("+total+")");
			if (session.state.currentCity != null) {
				cityListChanged();
				TextView city = (TextView) findViewById(R.id.current_city);
				city.setText(session.state.currentCity.name);
			}
			updateTickers();
		}
	}
	public void cityChanged() {
		Log.v(TAG,"cityChanged");
		TextView city = (TextView) findViewById(R.id.current_city);
		city.setText(session.state.currentCity.name);
	}
	public void onEjected() {
		Log.v(TAG,"you have been logged out");
		// FIXME give a better error
		finish();
	}
	protected void onStop() {
		super.onStop();
		Log.v(TAG,"onStop");
	}
	public void visDataReset() {
		Log.v(TAG,"vis count "+session.rpc.state.currentCity.visData.size());
		//if (!vis_data_loaded) gotVisDataInit();
	}
	public void gotCityData() {
		Log.v(TAG,"gotCityData");
		resource_bar.update(session.state.currentCity);
		TextView city = (TextView) findViewById(R.id.current_city);
		city.setText(session.state.currentCity.name);
		
		// Doesn't need to hit them all, but its easier then finding it
		Iterator<ResourceBar> i = bars.iterator();
		while (i.hasNext()) i.next().update();
	}
	public void tick() {
		// called from the network thread, needs to re-dir to main one
		Runnable resync = new Runnable() {
			public void run() {
				LouSessionMain.this.mainTick();
			}
		};
		this.runOnUiThread(resync);
	}
	/** tick ran in main thread by poller
	**/
	void mainTick() {
		updateTickers();
	}
	public void cityView(View v) {
		Log.v(TAG,"opening city view");
		Intent i = new Intent(this,CityView.class);
		i.putExtras(acct.toBundle());
		startActivity(i);
	}
	@Override
	public void onPlayerData() {
		updateTickers();
	}
	public void updateTickers() {
		TextView gold = (TextView) findViewById(R.id.gold);
		gold.setText(""+session.state.gold.getCurrent());
		TextView mana = (TextView) findViewById(R.id.mana);
		mana.setText(""+session.state.mana.getCurrent());
		TextView incoming = (TextView) findViewById(R.id.incoming_attacks);
		incoming.setText("" + session.state.incoming_attacks.size());
		if (session.state.currentCity != null) resource_bar.update(session.state.currentCity);
		Iterator<ResourceBar> i = bars.iterator();
		while (i.hasNext()) i.next().update();
	}
	public void showIncoming(View v) {
		Intent i = new Intent(this,IncomingAttacks.class);
		i.putExtras(acct.toBundle());
		startActivity(i);
	}
	public void openChat(View v) {
		Intent i = new Intent(this,ChatWindow.class);
		i.putExtras(acct.toBundle());
		startActivity(i);
	}
	public void doLogout(View v) {
		session.logout();
		finish();
	}
	public void onChat(ArrayList<ChatMsg> c) {
		int total = session.state.chat_history.size();
		Button chat = (Button) findViewById(R.id.chat);
		chat.setText("chat ("+total+")");
	}
	@Override
	public void cityListChanged() {
		mAdapter.clear();
		bars.clear();
		Iterator<City> i = session.state.cities.iterator();
		while (i.hasNext()) {
			mAdapter.add(i.next());
		}
	}
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Log.v(TAG,session.state.cities.get(arg2).toString());
		session.state.changeCity(session.state.cities.get(arg2));
	}
	class cityList extends ArrayAdapter<City> {
		cityList(Context c) {
			super(c,0);
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewGroup row = new LinearLayout(LouSessionMain.this);
			TextView name = new TextView(LouSessionMain.this);
			City i = getItem(position);
			name.setText(i.name);
			row.addView(name);
			// FIXME
			FrameLayout bar = new FrameLayout(LouSessionMain.this);
			//FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
			ResourceBar bar2 = new ResourceBar(LouSessionMain.this);
			bar2.update(i);
			bars.add(bar2); // FIXME, possible memory leak?
			//bar.setId(0x1000 + position);
			//trans.add(0x1000 + position, bar2);
			//bar2.update(i);
			//trans.commit();
			bar.addView(bar2.self);
			row.addView(bar);
			return row;
		}
	}
}
