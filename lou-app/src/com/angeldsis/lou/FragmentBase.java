package com.angeldsis.lou;

import java.util.ArrayList;

import com.angeldsis.louapi.ChatMsg;
import com.angeldsis.louapi.world.WorldParser.Cell;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class FragmentBase extends Fragment {
	protected FragmentUser parent;
	public void onAttach(Activity a) {
		super.onAttach(a);
		Log.v("FragmentBase","onAttach");
		parent = (FragmentUser) a;
	}
	@Override public void onDetach () {
		super.onDetach();
		parent = null;
	}
	public void onFoodWarning() {
	}
	@Override public void onStart() {
		super.onStart();
	}
	@Override public void onStop() {
		super.onStop();
	}
	@Override public void onViewCreated(View view, Bundle sis) {
		parent.addHook(this);
	}
	@Override public void onDestroyView() {
		super.onDestroyView();
		parent.removeHook(this);
	}
	abstract public void session_ready();
	public abstract View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);
	public void gotCityData() {}
	public boolean onChat(ArrayList<ChatMsg> d) {
		return false;
	}
	public void onPlayerData() {}
	public void cityListChanged() {}
	public void onReportCountUpdate() {}
	public void cellUpdated(Cell c) {}
}
