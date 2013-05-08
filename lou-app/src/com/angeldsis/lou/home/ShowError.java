package com.angeldsis.lou.home;

import com.angeldsis.lou.R;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ShowError extends Fragment {
	int id = -1;
	private static final String TAG = "ShowError";
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.v(TAG,"onCreateView");
		Context context = container.getContext();
		LinearLayout all = new LinearLayout(context);
		all.setOrientation(LinearLayout.VERTICAL);
		TextView tv = new TextView(context);
		Bundle b = getArguments();
		if (b.containsKey("msgid")) {
			id = b.getInt("msgid");
			tv.setText(id);
		} else {
			String msg = b.getString("message");
			tv.setText(msg);
		}
		all.addView(tv);
		
		Button btn = new Button(context);
		switch (id) {
		case R.string.network_offline:
			btn.setText("open network config");
			all.addView(btn);
			btn.setOnClickListener(new OnClickListener() {
				@Override public void onClick(View v) {
					Log.v(TAG,"foo");
					startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
				}});
			break;
		default:
			btn.setText("retry");
			btn.setOnClickListener(new OnClickListener() {
				@Override public void onClick(View v) {
					getActivity().getSupportFragmentManager().beginTransaction()
					.replace(R.id.main_frame, new Loading()).commit();
				}});
			all.addView(btn);
		}
		return all;
	}
	public void onStart() {
		if (id == R.string.network_offline) {
			ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo ni = cm.getActiveNetworkInfo();
			if ((ni != null) && ni.isConnected()) {
				getActivity().getSupportFragmentManager().beginTransaction()
				.replace(R.id.main_frame, new Loading()).commit();
			}
		}
		super.onStart();
	}
}
