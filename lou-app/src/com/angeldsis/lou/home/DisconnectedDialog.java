package com.angeldsis.lou.home;

import com.angeldsis.lou.R;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class DisconnectedDialog extends DialogFragment {
	public static DisconnectedDialog newInstance(String code) {
		DisconnectedDialog f = new DisconnectedDialog();
		Bundle args = new Bundle();
		args.putString("code", code); // FIXME, use this field in the dialog
		f.setArguments(args);
		return f;
	}
	@Override public void onCreate(Bundle sis) {
		super.onCreate(sis);
		setStyle(DialogFragment.STYLE_NORMAL,android.R.style.Theme_Holo_Dialog);
	}
	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.disconnected, container,false);
		((Button)v.findViewById(R.id.ok)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				DisconnectedDialog.this.getActivity().finish();
			}
		});
		return v;
	}
	@Override public void onDismiss(DialogInterface dialog) {
		Activity a = getActivity();
		if (a != null) a.finish();
	}
}
