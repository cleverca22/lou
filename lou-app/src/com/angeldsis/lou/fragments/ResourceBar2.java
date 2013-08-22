package com.angeldsis.lou.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.angeldsis.lou.FragmentBase;
import com.angeldsis.lou.R;
import com.angeldsis.lou.Utils;
import com.angeldsis.louapi.LouState;
import com.angeldsis.louapi.Resource;
import com.angeldsis.louapi.LouState.City;

public class ResourceBar2 extends FragmentBase {
	private static final String TAG = "ResourceBar2";
	TextView[] counts;
	TextView[] rates;
	City lastCity;
	final static int[] countids = { R.id.woodC, R.id.stoneC, R.id.ironC, R.id.foodC };
	final static int[] rateids = { R.id.woodR, R.id.stoneR, R.id.ironR, R.id.foodR };
	Handler handler = new Handler();
	Runnable ticker = new Runnable() {
		@Override public void run() {
			ResourceBar2.this.update();
			handler.removeCallbacks(ticker);
			handler.postDelayed(ticker, 2000);
		}
	};
	@Override public void onStart() {
		super.onStart();
		Log.v(TAG,"onStart");
	}
	@Override public void onResume() {
		super.onResume();
		if (parent == null) return;
		if (parent.session == null) return;
		ticker.run();
	}
	@Override public void onPause() {
		super.onPause();
		Log.v(TAG,"onPause");
		handler.removeCallbacks(ticker);
	}
	@Override public void gotCityData() {
		// this is called if the current city is updated
		if (lastCity == null || 
				(lastCity == parent.session.state.currentCity)) update();
	}
	@Override public void session_ready() {
		Log.v(TAG,"session_ready");
		ticker.run();
	}
	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.resource_bar, container, false);
		rates = new TextView[4];
		counts = new TextView[4];
		int i;
		for (i = 0; i<4;i++) {
			counts[i] = (TextView) vg.findViewById(countids[i]);
			rates[i] = (TextView) vg.findViewById(rateids[i]);
		}
		return vg;
	}
	public void update(City city) {
		lastCity = city;
		update();
	}
	public void update() {
		City c;
		if (parent == null) {
			Log.v(TAG,"parent is null!");
			return;
		}
		if (parent.session == null) {
			Log.v(TAG,"session is null!");
			return;
		}
		if (lastCity == null) c = parent.session.state.currentCity;
		else c = lastCity;
		int x;
		if (parent == null) {
			Log.v("ResourceBar2","parent is null!");
			return;
		}
		LouState state = parent.session.state;
		for (x = 0; x < 4; x++) {
			Resource r = c.resources[x];
			int current = c.getResourceCount(state,x);
			counts[x].setText(Utils.NumberFormat(current));
			rates[x].setText(Utils.NumberFormat((int) c.getResourceRate(state, x)));
			int color;
			// 25-71% == green
			// 82-88% == yellow
			// 90% == orange
			// 100% == red!
			if (r.getMax() == 0) color = android.R.color.white;
			else {
				int percent = (current * 100) / r.getMax();
				if (percent == 100) color = R.color.resource_red;
				else if (percent > 90) color = R.color.resource_orange;
				else if (percent > 82) color = R.color.resource_yellow;
				else if (percent < 0) color = R.color.resource_red;
				else color = R.color.resource_green;
			}
			counts[x].setTextColor(getResources().getColor(color));
		}
	}
}
