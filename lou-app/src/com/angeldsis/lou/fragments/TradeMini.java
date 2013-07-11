package com.angeldsis.lou.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.angeldsis.lou.FragmentBase;
import com.angeldsis.lou.R;

public class TradeMini extends FragmentBase {
	@Override
	public void session_ready() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.trade_mini, container, false);
		return vg;
	}
}
