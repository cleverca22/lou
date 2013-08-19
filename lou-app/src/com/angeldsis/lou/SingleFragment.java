package com.angeldsis.lou;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

public class SingleFragment extends FragmentUser {
	private static final String TAG = "SingleFragment";
	@Override public void onCreate(Bundle sis) {
		Log.v(TAG,"onCreate");
		super.onCreate(sis);
		Intent msg = getIntent();
		Bundle args = msg.getExtras();
		int layout = R.layout.fragment_pair;
		//if (args.containsKey("layout")) layout = args.getInt("layout");
		setContentView(layout);
		if (sis == null) {
			FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
			Class<? extends Fragment> fragmentClass = (Class<? extends Fragment>) args.get("fragment");
			Log.v(TAG,args.toString());
			Log.v(TAG,"class:"+fragmentClass);
			if (fragmentClass == null) {
				throw new IllegalStateException("unexpected null with args:"+args.toString());
			}
			try {
				trans.replace(R.id.main_frame, fragmentClass.newInstance());
			
				if (args.containsKey("fragment2")) {
					Class<? extends Fragment> fragmentClass2 = (Class<? extends Fragment>) args.get("fragment2");
					trans.replace(R.id.second_frame, fragmentClass2.newInstance());
				}
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			trans.commit();
		}
	}
}
