package com.angeldsis.lou;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class FragmentBase extends Fragment {
	protected FragmentUser parent;
	public void onAttach(Activity a) {
		super.onAttach(a);
		parent = (FragmentUser) a;
		parent.addHook(this);
	}
	public void onDetach () {
		super.onDetach();
		parent.removeHook(this);
		parent = null;
	}
	public void onFoodWarning() {
		// TODO Auto-generated method stub
		
	}
	abstract public void session_ready();
	public abstract View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);
	public void gotCityData() {}
}
