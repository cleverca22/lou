package com.angeldsis.lou.home;

import com.angeldsis.lou.R;
import com.angeldsis.lou.SessionKeeper;
import com.angeldsis.lou.louLogin;
import com.angeldsis.lou.SessionKeeper.CookieCallback;
import com.angeldsis.louapi.LouSession.result;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Loading extends Fragment {
	Boolean stopped;
	private static final String TAG = "LoadingFragment";
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.v(TAG,"onCreateView");
		stopped = false;
		if ((SessionKeeper.session2 != null) && (SessionKeeper.session2.servers.size() > 0)) {
			Log.v(TAG,"session already setup");
			FragmentTransaction trans = getActivity().getSupportFragmentManager().beginTransaction();
			trans.replace(R.id.main_frame, new ServerList());
			trans.commit();
			Log.v(TAG,"returning null");
			return null;
		}
		String cookie = container.getContext().getSharedPreferences("main",
				Context.MODE_PRIVATE).getString("cookie", null);
		if (cookie != null) { // restore cookie, check if its valid
			SessionKeeper.restore_cookie(cookie);
			SessionKeeper.checkCookie(new CookieCallback() {
				public void done(result r) {
					if (r.worked) {
						Log.v(TAG,"cookie checked "+SessionKeeper.session2.servers.size());
						if (!stopped) {
							FragmentTransaction trans = getActivity().getSupportFragmentManager().beginTransaction();
							trans.replace(R.id.main_frame, new ServerList());
							trans.commit();
						} else {
							Log.v(TAG,"fragment stopped, cant update ui");
						}
					}
					else {
						Log.e(TAG,"cookie check failed");
						// assume you need to login again
						Intent doLogin = new Intent(getActivity(), louLogin.class);
						startActivity(doLogin);
						getActivity().finish();
					}
				}
			});
		} else { // open login page
			Intent doLogin = new Intent(getActivity(), louLogin.class);
			startActivity(doLogin);
			getActivity().finish();
		}
		return inflater.inflate(R.layout.loading, container,false);
	}
	public void onStop() {
		super.onStop();
		stopped = true;
	}
}
