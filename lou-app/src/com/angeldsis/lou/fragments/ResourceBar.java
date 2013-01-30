package com.angeldsis.lou.fragments;

import com.angeldsis.lou.R;
import com.angeldsis.louapi.LouState.City;
import com.angeldsis.louapi.Resource;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ResourceBar implements Runnable {
	TextView[] counts;
	TextView[] rates;
	public View self;
	City lastCity;
	final static int[] countids = { R.id.woodC, R.id.stoneC, R.id.ironC, R.id.foodC };
	final static int[] rateids = { R.id.woodR, R.id.stoneR, R.id.ironR, R.id.foodR };
	Handler h = new Handler();
	boolean posted = false;
	
	public ResourceBar(Activity context) {
		self = context.getLayoutInflater().inflate(R.layout.resource_bar, null);
	}
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		self = inflater.inflate(R.layout.resource_bar,container,false);
		return self;
	}
	void init() {
		if (counts != null) return;
		rates = new TextView[4];
		counts = new TextView[4];
		int i;
		for (i = 0; i<4;i++) {
			counts[i] = (TextView) self.findViewById(countids[i]);
			rates[i] = (TextView) self.findViewById(rateids[i]);
		}
	}
	public void update(City city) {
		lastCity = city;
		update();
	}
	private synchronized void update() {
		init();
		int x;
		for (x = 0; x < 4; x++) {
			Resource r = lastCity.resources[x];
			counts[x].setText(""+r.getCurrent());
			rates[x].setText(r.getRate());
			int color;
			// 25-71% == green
			// 82-88% == yellow
			// 90% == orange
			// 100% == red!
			if (r.getMax() == 0) color = android.R.color.white;
			else {
				int percent = (r.getCurrent() * 100) / r.getMax();
				if (percent == 100) color = R.color.resource_red;
				else if (percent > 90) color = R.color.resource_orange;
				else if (percent > 82) color = R.color.resource_yellow;
				else color = R.color.resource_green;
			}
			counts[x].setTextColor(self.getContext().getResources().getColor(color));
		}
		if (!posted) {
			h.postDelayed(this, 10000); // FIXME, make it adjustable?
			posted = true;
		}
	}
	@Override public synchronized void run() {
		posted = false;
		update();
	}
}
