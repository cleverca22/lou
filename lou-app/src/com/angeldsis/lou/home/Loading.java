package com.angeldsis.lou.home;

import java.net.UnknownHostException;

import com.angeldsis.lou.R;
import com.angeldsis.lou.SessionKeeper;
import com.angeldsis.lou.SessionKeeper.CookieCallback;
import com.angeldsis.lou.louLogin;
import com.angeldsis.louapi.LouSession.result;
import com.angeldsis.louutil.HttpUtilImpl;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Loading extends Fragment {
	Boolean stopped;
	private static final String TAG = "home.Loading";
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.v(TAG,"onCreateView");
		ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if ((ni == null) || !ni.isConnected()) {
			Fragment f = new ShowError();
			Bundle b = new Bundle();
			
			b.putInt("msgid", R.string.network_offline);
			
			f.setArguments(b);
			getActivity().getSupportFragmentManager().beginTransaction()
				.replace(R.id.main_frame, f).commit();
			return null;
		}
		stopped = false;
		if ((SessionKeeper.session2 != null) && (SessionKeeper.session2.servers != null) &&
				(SessionKeeper.session2.servers.size() > 0)) {
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
			Log.v(TAG,"restoring cookie");
			HttpUtilImpl.getInstance().restore_cookie(cookie);
			SessionKeeper.checkCookie(new CookieCallback() {
				public void done(result r) {
					FragmentActivity a = getActivity();
					if (a == null) return;
					if (r.worked) {
						Log.v(TAG,"cookie checked "+SessionKeeper.session2.servers.size());
						if (!stopped) {
							a.getSupportFragmentManager().beginTransaction()
								.replace(R.id.main_frame, new ServerList()).commit();
						} else {
							Log.v(TAG,"fragment stopped, cant update ui");
						}
					}
					else {
						if (r.e == null) {
							Log.e(TAG,"cookie check failed");
							Loading.this.openLogin();
						} else {
							Log.e(TAG,"error",r.e);
							Fragment f = new ShowError();
							Bundle b = new Bundle();
							
							if (r.e instanceof UnknownHostException) b.putString("message", "dns error");
							else b.putString("message", "unknown error");
							
							f.setArguments(b);
							a.getSupportFragmentManager().beginTransaction()
								.replace(R.id.main_frame, f).commit();
						}
					}
				}
			},getActivity().getSharedPreferences("main",Context.MODE_PRIVATE).getString("email", null));
		} else openLogin();

		return inflater.inflate(R.layout.loading, container,false);
	}
	private void openLogin() {
		FragmentActivity a = getActivity();
		FragmentTransaction trans = a.getSupportFragmentManager().beginTransaction();
		//trans.replace(R.id.main_frame, new Webview());
		trans.replace(R.id.main_frame,new louLogin());
		trans.commitAllowingStateLoss();
	}
	public void onStop() {
		super.onStop();
		stopped = true;
	}
}
