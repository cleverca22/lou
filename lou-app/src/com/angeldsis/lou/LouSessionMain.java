package com.angeldsis.lou;

import java.util.Collection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.angeldsis.lou.SessionKeeper.Session;
import com.angeldsis.lou.fragments.GoldDisplay;
import com.angeldsis.lou.fragments.ResourceBar;
import com.angeldsis.lou.reports.Reports;
import com.angeldsis.louapi.LouState;
import com.angeldsis.louapi.LouState.City;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class LouSessionMain extends FragmentBase implements OnItemClickListener, Runnable {
	static final String TAG = "LouSessionMain";
	cityList mAdapter;
	Handler h = new Handler();
	ListView list;
	private AdView adView;
	@Deprecated Session session;
	private TextView currentCity,mana,incoming;
	private Button reports;
	
	@Override public void onCreate(Bundle sis) {
		super.onCreate(sis);
		mAdapter = new cityList();
	}
	public void onAttach(Activity a) {
		super.onAttach(a);
		Log.v(TAG,"onAttach");
	}
	public void onDetach () {
		super.onDetach();
		Log.v(TAG,"onDetatch");
	}
	@Override public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle sis) {
		ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.session_core, root, false);
		Log.v(TAG,"onCreateView");
		list = (ListView) vg.findViewById(R.id.cities);
		list.setAdapter(mAdapter);
		list.setOnItemClickListener(this);
		currentCity = (TextView) vg.findViewById(R.id.current_city);
		mana = (TextView) vg.findViewById(R.id.mana);
		incoming = (TextView) vg.findViewById(R.id.incoming_attacks);
		incoming.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				Intent i = new Intent(parent,IncomingAttacks.class);
				i.putExtras(parent.acct.toBundle());
				startActivity(i);
			}
		});
		reports = (Button) vg.findViewById(R.id.reports);
		reports.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				Log.v(TAG,"opening reports");
				Bundle args = parent.acct.toBundle();
				Intent i = new Intent(parent,Reports.class);
				i.putExtras(args);
				startActivity(i);
			}
		});

		adView = new AdView(getActivity(), AdSize.BANNER, "a15115491d452e5");
		ViewGroup ad = (ViewGroup) vg.findViewById(R.id.ad);
		ad.addView(adView);
		adView.loadAd(new AdRequest()
			//.addTestDevice(AdRequest.TEST_EMULATOR)
			//.addTestDevice("3BAAE9494C8A3A8046F0239B242006E8")
		);
		if (sis == null) {
			FragmentTransaction ft = getChildFragmentManager().beginTransaction();
			ft.add(R.id.gold, new GoldDisplay());
			ft.commit();
		}
		return vg;
	}
	@Override public void onDestroy() {
		if (adView != null) {
			adView.destroy();
		}
		super.onDestroy();
		Log.v(TAG,"onDestroy");
	}
	public void onStart() {
		super.onStart();
		Log.v(TAG,"onStart");
		/*
		Configuration c = getResources().getConfiguration();
		int size = c.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
		switch (size) {
		case Configuration.SCREENLAYOUT_SIZE_NORMAL:
			Log.v(TAG,"screen size normal");
			break;
		case Configuration.SCREENLAYOUT_SIZE_LARGE:
			Log.v(TAG,"screen size large");
			break;
		default:
			Log.v(TAG,"screen size "+size);
		}*/
	}
	@Override public void session_ready() {
		Log.v(TAG,"session_ready");
		session = parent.session;
		if (session.alive == false) {
			Log.wtf(TAG, "session not alive in session_ready");
			parent.onEjected();
		} else {
			if (session.state.currentCity != null) {
				cityListChanged();
				currentCity.setText(session.state.currentCity.name);
			}
			updateTickers();
			onReportCountUpdate();
			gotCityData();
		}
	}
	public void onCityChanged() {
		Log.v(TAG,"cityChanged");
		currentCity.setText(session.state.currentCity.name);
	}
	public void onStop() {
		super.onStop();
		Log.v(TAG,"onStop");
	}
	public void gotCityData() {
		Log.v(TAG,"gotCityData");
		Session session = parent.session; // FIXME
		LouState state = session.state;
		currentCity.setText(state.currentCity.name);
		int x;
		for (x=list.getChildCount() - 1; x >= 0; x--) {
			ViewHolder holder = (ViewHolder) list.getChildAt(x).getTag();
			holder.bar2.update();
		}
	}
	public void tick() {
		// called from the network thread, needs to re-dir to main one
		Runnable resync = new Runnable() {
			public void run() {
				LouSessionMain.this.mainTick();
			}
		};
		parent.runOnUiThread(resync);
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
		// FIXME, used to track down a null pointer
		Session session = parent.session;
		LouState state = session.state;
		mana.setText(""+state.mana.getCurrent());
		incoming.setText("" + state.incoming_attacks.size());
	}
	@Override public void cityListChanged() {
		Log.v(TAG,"cityListChanged");
		Collection<City> cities = parent.session.state.cities.values();
		City[] list = new City[cities.size()];
		cities.toArray(list);
		mAdapter.update(list);
	}
	@Override public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long id) {
		Log.v(TAG,"id:"+id);
		Log.v(TAG,session.state.cities.get((int)id).toString());
		session.state.changeCity(session.state.cities.get((int)id));
	}
	private static class ViewHolder {
		public TextView name;
		public ResourceBar bar2;
	}
	class cityList extends BaseAdapter {
		City[] list;
		cityList() {
			super();
			list = new City[0];
		}
		public void update(City[] list2) {
			list = list2;
			notifyDataSetChanged();
		}
		public long getItemId(int position) {
			return getItem(position).cityid;
		}
		@Override public City getItem(int position) {
			return list[position];
		}
		@Override public int getCount() {
			return list.length;
		}
		public View getView(int position, View convertView, ViewGroup root) {
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				ViewGroup row = (ViewGroup) parent.getLayoutInflater().inflate(R.layout.session_core_row, root, false);
				holder.name = (TextView) row.findViewById(R.id.name);

				FrameLayout bar = (FrameLayout) row.findViewById(R.id.resource_bar);
				holder.bar2 = new ResourceBar(parent);
				holder.bar2.setState(parent.session.state);
				bar.addView(holder.bar2);
				row.setTag(holder);
				convertView = row;
			} else holder = (ViewHolder) convertView.getTag();
			
			City i = getItem(position);
			holder.bar2.update(i);
			holder.name.setText(i.name);
			return convertView;
		}
	}
	@Override public void onReportCountUpdate() {
		String msg = getResources().getString(R.string.reports, parent.session.state.unviewed_reports);
		reports.setText(msg);
	}
	@Override public void onResume() {
		super.onResume();
		run();
		Log.v(TAG, "onResume");
	}
	@Override public void onPause() {
		super.onPause();
		h.removeCallbacks(this);
		Log.v(TAG,"onPause");
	}
	@Override public void run() {
		// FIXME, remove this once the resource bars are fragmented
		int x;
		for (x=list.getChildCount() - 1; x >= 0; x--) {
			ViewHolder holder = (ViewHolder) list.getChildAt(x).getTag();
			holder.bar2.update();
		}
		h.removeCallbacks(this);
		h.postDelayed(this, 5000);
	}
	private static final Uri uri = Uri.parse("loudroid://core");
	public static Intent getIntent(AccountWrap acct, Context context) {
		Bundle args = acct.toBundle();
		args.putString("two", "two");
		args.putSerializable("fragment", LouSessionMain.class);
		Intent intent = new Intent(context,SingleFragment.class);
		intent.putExtras(args);
		intent.setData(uri);
		return intent;
	}
}
