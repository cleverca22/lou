package com.angeldsis.lou.fragments;

import com.angeldsis.lou.R;
import com.angeldsis.louapi.LouState;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ResourceBar extends Fragment {
	TextView[] counts;
	TextView[] rates;
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View r = inflater.inflate(R.layout.resource_bar,container,false);
		return r;
	}
/*	public void setLevels(int wood, int stone, int iron, int food) {
		((TextView)getActivity().findViewById(R.id.woodC)).setText(""+wood);
		((TextView)getActivity().findViewById(R.id.stoneC)).setText(""+stone);
		((TextView)getActivity().findViewById(R.id.ironC)).setText(""+iron);
		((TextView)getActivity().findViewById(R.id.foodC)).setText(""+food);
	}*/
	void init() {
		if (counts != null) return;
		counts = new TextView[4];
		counts[0] = (TextView) getActivity().findViewById(R.id.woodC);
		counts[1] = (TextView) getActivity().findViewById(R.id.stoneC);
		counts[2] = (TextView) getActivity().findViewById(R.id.ironC);
		counts[3] = (TextView) getActivity().findViewById(R.id.foodC);
		rates = new TextView[4];
		rates[0] = (TextView) getActivity().findViewById(R.id.woodR);
		rates[1] = (TextView) getActivity().findViewById(R.id.stoneR);
		rates[2] = (TextView) getActivity().findViewById(R.id.ironR);
		rates[3] = (TextView) getActivity().findViewById(R.id.foodR);
	}
	public void update(LouState state) {
		init();
		int x;
		for (x = 0; x < 4; x++) {
			counts[x].setText(state.resources[x].getCurrent());
			rates[x].setText(state.resources[x].getRate());
		}
	}
}
