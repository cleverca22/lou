package com.angeldsis.lou.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.angeldsis.lou.FragmentBase;
import com.angeldsis.lou.R;
import com.angeldsis.lou.Utils;

public class GoldDisplay extends FragmentBase {
	// note, doesn't tick up yet, only updates on events
	TextView gold;
	@Override public void session_ready() {
		onPlayerData();
	}
	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.gold, container, false);
		gold = (TextView) vg.findViewById(R.id.gold);
		return vg;
	}
	@Override public void onPlayerData() {
		gold.setText(Utils.NumberFormat(parent.session.state.gold.getCurrent()));
	}
}
