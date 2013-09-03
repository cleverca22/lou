package com.angeldsis.lou.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.angeldsis.lou.FragmentBase;
import com.angeldsis.lou.R;

public class RestartConfirm extends FragmentBase implements OnCheckedChangeListener, OnClickListener {
	private static final String TAG = "RestartConfirm";
	Button restart;
	@Override public void session_ready() {
		// TODO Auto-generated method stub
		
	}
	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.restart_confirm, container, false);
		CheckBox confirm = (CheckBox) vg.findViewById(R.id.confirm);
		confirm.setOnCheckedChangeListener(this);
		restart = (Button) vg.findViewById(R.id.restart);
		restart.setOnClickListener(this);
		return vg;
	}
	@Override
	public void onClick(View v) {
		Log.v(TAG,"restarting!");
	}
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		//restart.setEnabled(isChecked);
	}
}
