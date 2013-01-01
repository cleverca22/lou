package com.angeldsis.lou.fragments;

import com.angeldsis.lou.R;
import com.angeldsis.louapi.LouState.City;
import com.angeldsis.louapi.Resource;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ResourceBar {
	TextView[] counts;
	TextView[] rates;
	public View self;
	City lastCity;
	final static int[] countids = { R.id.woodC, R.id.stoneC, R.id.ironC, R.id.foodC };
	final static int[] rateids = { R.id.woodR, R.id.stoneR, R.id.ironR, R.id.foodR }; 
	public ResourceBar(Activity context) {
		self = context.getLayoutInflater().inflate(R.layout.resource_bar, null);
	}
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		self = inflater.inflate(R.layout.resource_bar,container,false);
		return self;
	}
/*	public void setLevels(int wood, int stone, int iron, int food) {
		((TextView)getActivity().findViewById(R.id.woodC)).setText(""+wood);
		((TextView)getActivity().findViewById(R.id.stoneC)).setText(""+stone);
		((TextView)getActivity().findViewById(R.id.ironC)).setText(""+iron);
		((TextView)getActivity().findViewById(R.id.foodC)).setText(""+food);
	}*/
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
	public void update() {
		init();
		int x;
		for (x = 0; x < 4; x++) {
			Resource r = lastCity.resources[x];
			counts[x].setText(""+r.getCurrent());
			rates[x].setText(r.getRate());
			int color = android.R.color.white;
			// FIXME, 50% is not red
			if (r.getCurrent() > (r.getMax()/2)) {
				color = R.color.red;
				
			}
			counts[x].setTextColor(self.getContext().getResources().getColor(color));
		}
	}
}
