package com.angeldsis.lou;

import com.angeldsis.lou.city.SelectCity;
import com.angeldsis.louapi.LouState.City;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CityCore extends FragmentBase {
	private static final String TAG = "CityCore";
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.city_core, container,false);
		return v;
	}
	@Override public void session_ready() {
		SelectCity sc = (SelectCity) getView().findViewById(R.id.selectCity);
		sc.setMode(SelectCity.ChangeCurrentCity);
		sc.session_ready(parent.session.rpc.state, parent);
	}
	public void gotCityData() {
		City c = parent.session.state.currentCity;
		if (c.trade_in != null) {
			Log.v(TAG,"trade in size: "+c.trade_in.size());
		}
	}
}
