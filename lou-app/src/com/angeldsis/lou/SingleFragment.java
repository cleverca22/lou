package com.angeldsis.lou;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

public class SingleFragment extends FragmentUser {
	private static final String TAG = "SingleFragment";
	@Override public void onCreate(Bundle sis) {
		super.onCreate(sis);
		
		Intent msg = getIntent();
		Bundle args = msg.getExtras();
		Class<? extends Fragment> fragmentClass = (Class<? extends Fragment>) args.get("fragment");
		Log.v(TAG,fragmentClass.toString());
		setContentView(R.layout.main);
		try {
			FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
			trans.replace(R.id.main_frame, fragmentClass.newInstance());
			trans.commit();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
