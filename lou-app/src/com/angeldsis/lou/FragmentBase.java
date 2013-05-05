package com.angeldsis.lou;

import android.app.Activity;
import android.support.v4.app.Fragment;

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
}
