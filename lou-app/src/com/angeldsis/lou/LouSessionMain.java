package com.angeldsis.lou;

import java.util.ArrayList;
import java.util.Iterator;

import com.angeldsis.lou.fragments.ResourceBar;
import com.angeldsis.lou.reports.Reports;
import com.angeldsis.louapi.ChatMsg;
import com.angeldsis.louapi.LouState;
import com.angeldsis.louapi.LouState.City;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class LouSessionMain extends SessionUser implements SessionKeeper.Callbacks, OnItemClickListener, Runnable {
	static final String TAG = "LouSessionMain";
	ResourceBar resource_bar;
	cityList mAdapter;
	Handler h = new Handler();
	ListView list;
	private AdView adView;

	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		Log.v(TAG,"onCreate");
		setContentView(R.layout.session_core);
		Log.v(TAG,"bar1");
		resource_bar = new ResourceBar(this);
		((FrameLayout) findViewById(R.id.resource_bar)).addView(resource_bar);
		mAdapter = new cityList(this);
		list = (ListView) findViewById(R.id.cities);
		list.setAdapter(mAdapter);
		list.setOnItemClickListener(this);
		
		adView = new AdView(this, AdSize.BANNER, "a15115491d452e5");
		ViewGroup ad = (ViewGroup) findViewById(R.id.ad);
		ad.addView(adView);
		adView.loadAd(new AdRequest().addTestDevice(AdRequest.TEST_EMULATOR));
	}
	@Override protected void onDestroy() {
		if (adView != null) {
			adView.destroy();
		}
		super.onDestroy();
	}
	protected void onStart() {
		super.onStart();
		Log.v(TAG,"onStart");
	}
	public void session_ready() {
		Log.v(TAG,"session_ready");
		if (session.alive == false) {
			onEjected();
		} else {
			LouState state = session.state;
			ArrayList<ChatMsg> msgs = state.chat_history;
			int total = msgs.size();
			TextView chat = (TextView) findViewById(R.id.chat);
			chat.setText(""+total);
			if (session.state.currentCity != null) {
				cityListChanged();
				TextView city = (TextView) findViewById(R.id.current_city);
				city.setText(session.state.currentCity.name);
			}
			updateTickers();
			onReportCountUpdate();
		}
	}
	public void cityChanged() {
		Log.v(TAG,"cityChanged");
		TextView city = (TextView) findViewById(R.id.current_city);
		city.setText(session.state.currentCity.name);
	}
	protected void onStop() {
		super.onStop();
		Log.v(TAG,"onStop");
	}
	public void visDataReset() {
		super.visDataReset();
		//if (!vis_data_loaded) gotVisDataInit();
	}
	public void gotCityData() {
		Log.v(TAG,"gotCityData");
		resource_bar.update(session.state.currentCity);
		TextView city = (TextView) findViewById(R.id.current_city);
		city.setText(session.state.currentCity.name);
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
	}
	public void showIncoming(View v) {
		Intent i = new Intent(this,IncomingAttacks.class);
		i.putExtras(acct.toBundle());
		startActivity(i);
	}
	public void onChat(ArrayList<ChatMsg> c) {
		int total = session.state.chat_history.size();
		TextView chat = (TextView) findViewById(R.id.chat);
		chat.setText(""+total);
	}
	@Override
	public void cityListChanged() {
		Log.v(TAG,"cityListChanged()");
		mAdapter.clear();
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
		// getView gets called at regular intervals, causing excessive recreation of objects
		//SparseArray<LinearLayout> views;
		cityList(Context c) {
			super(c,0);
			//views = new SparseArray<LinearLayout>();
		}
		public void clear() {
			super.clear();
			//views.clear();
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			// FIXME, use convertView
			LinearLayout row;// = views.get(position);
			row = new LinearLayout(LouSessionMain.this);
			row.setOrientation(LinearLayout.VERTICAL);
			TextView name = new TextView(LouSessionMain.this);
			City i = getItem(position);
			name.setText(i.name);
			row.addView(name);
			// FIXME
			FrameLayout bar = new FrameLayout(LouSessionMain.this);
			//FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
			ResourceBar bar2 = new ResourceBar(LouSessionMain.this);
			bar2.update(i);
			//bar.setId(0x1000 + position);
			//trans.add(0x1000 + position, bar2);
			//bar2.update(i);
			//trans.commit();
			bar.addView(bar2);
			row.addView(bar);
			//views.put(position, row);
			return row;
		}
	}
	@Override
	public void onReportCountUpdate() {
		String msg = getResources().getString(R.string.reports, session.state.unviewed_reports);
		((Button)findViewById(R.id.reports)).setText(msg);
		Log.v(TAG,"unviewed:"+session.state.unviewed_reports);
	}
	public void openReports(View v) {
		Log.v(TAG,"opening reports");
		Bundle args = acct.toBundle();
		Intent i = new Intent(this,Reports.class);
		i.putExtras(args);
		startActivity(i);
	}
	@Override protected void onResume() {
		super.onResume();
		run();
	}
	@Override protected void onPause() {
		super.onPause();
		h.removeCallbacks(this);
	}
	@Override public void run() {
		int x;
		for (x=list.getChildCount() - 1; x >= 0; x--) {
			View v = list.getChildAt(x).findViewById(R.id.resource_bar);
			if (v instanceof ResourceBar) {
				ResourceBar b = (ResourceBar) v;
				b.update();
			}
		}
		resource_bar.update();
		h.removeCallbacks(this);
		h.postDelayed(this, 5000);
	}
}
