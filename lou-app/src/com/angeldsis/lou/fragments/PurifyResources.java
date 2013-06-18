package com.angeldsis.lou.fragments;

import org.json2.JSONArray;
import org.json2.JSONException;
import org.json2.JSONObject;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.angeldsis.lou.FragmentBase;
import com.angeldsis.lou.R;
import com.angeldsis.lou.Utils;

public class PurifyResources extends FragmentBase {
	// todo list
	// enforce a max limit, and give a max button
	// help in purifying enough to reach a research goal
	private static final int[] inputs = { R.id.editText1, R.id.editText2, R.id.editText3, R.id.editText4 };
	private static final int[] needed = { R.id.needed1, R.id.needed2, R.id.needed3, R.id.needed4 };
	private static final int[] totals = { R.id.total1, R.id.total2, R.id.total3, R.id.total4 };
	@Override public void session_ready() {
		onPlayerData();
	}
	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.purify_resources, container, false);
		int x;
		for (x=0;x<4;x++) {
			EditText et = (EditText) vg.findViewById(inputs[x]);
			final TextView tv = (TextView) vg.findViewById(needed[x]);
			et.addTextChangedListener(new TextWatcher(){
				@Override public void afterTextChanged(Editable s) {
				}
				@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}
				@Override public void onTextChanged(CharSequence s, int start, int before, int count) {
					try {
						int number = Integer.parseInt(s.toString());
						tv.setText(Utils.NumberFormat(number * 1000));
					} catch (NumberFormatException e) {
					}
				}});
		}
		Button b = (Button) vg.findViewById(R.id.purify);
		b.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				try {
					JSONArray counts = new JSONArray();
					int x;
					for (x=0;x<4;x++) {
						EditText et = (EditText) vg.findViewById(inputs[x]);
						int count = Integer.parseInt(et.getText().toString());
						if (count > 0) {
							JSONObject item = new JSONObject();
							item.put("t", x+1);
							item.put("c", count*1000);
							counts.put(item);
						}
					}
					parent.session.rpc.ResourceToVoid(parent.session.state.currentCity,counts);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}});
		return vg;
	}
	public void onPlayerData() {
		Log.v("PurifyResources","onPlayerData");
		int x;
		ViewGroup vg = (ViewGroup) getView();
		for (x=0; x<4; x++) {
			TextView tv = (TextView) vg.findViewById(totals[x]);
			tv.setText(Utils.NumberFormat(parent.session.state.voidResources[x]));
		}
	}
}
