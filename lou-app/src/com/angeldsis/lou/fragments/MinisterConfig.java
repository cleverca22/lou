package com.angeldsis.lou.fragments;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.angeldsis.lou.FragmentBase;
import com.angeldsis.lou.R;

public class MinisterConfig extends FragmentBase implements OnClickListener {
	@Override
	public void session_ready() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.minister_config, container, false);
		Button b = (Button) vg.findViewById(R.id.trade_mini);
		b.setOnClickListener(this);
		return vg;
	}
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.trade_mini:
			FragmentTransaction ft = getChildFragmentManager().beginTransaction();
			ft.replace(R.id.mini_frame, new TradeMini());
			ft.commit();
			break;
		}
	}
}
