package com.angeldsis.lou.fragments;

import com.angeldsis.lou.R;
import com.angeldsis.lou.Utils;
import com.angeldsis.louapi.LouState;
import com.angeldsis.louapi.LouState.City;
import com.angeldsis.louapi.Resource;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ResourceBar extends ViewGroup {
	private static final String TAG = "ResourceBar";
	TextView[] counts;
	TextView[] rates;
	private View self;
	City lastCity;
	final static int[] countids = { R.id.woodC, R.id.stoneC, R.id.ironC, R.id.foodC };
	final static int[] rateids = { R.id.woodR, R.id.stoneR, R.id.ironR, R.id.foodR };
	LouState state;
	
	public ResourceBar(Context context) {
		super(context);
		self = inflate(context,R.layout.resource_bar, null);
		addView(self);
	}
	public ResourceBar(Context context, AttributeSet attrs) {
		super(context,attrs);
		self = inflate(context,R.layout.resource_bar, null);
		addView(self);
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
	public void update() {
		if (lastCity == null) return;
		init();
		int x;
		for (x = 0; x < 4; x++) {
			Resource r = lastCity.resources[x];
			int current = lastCity.getResourceCount(state,x);
			counts[x].setText(Utils.NumberFormat(current));
			rates[x].setText(Utils.NumberFormat((int) lastCity.getResourceRate(state, x)));
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
			counts[x].setTextColor(self.getContext().getResources().getColor(color));
		}
	}
	@Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
		self.layout(0, 0, r-l, b-t);
	}
	@Override protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
		self.measure(widthMeasureSpec, heightMeasureSpec);
		this.setMeasuredDimension(self.getMeasuredWidth(), self.getMeasuredHeight());
	}
	public void setState(LouState state2) {
		state = state2;
	}
}
