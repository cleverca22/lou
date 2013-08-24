package com.angeldsis.lou.world;

import java.util.ArrayList;

import org.json2.JSONArray;
import org.json2.JSONException;
import org.json2.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.angeldsis.lou.FragmentBase;
import com.angeldsis.lou.R;
import com.angeldsis.lou.SessionUser;
import com.angeldsis.louapi.LouState.City;
import com.angeldsis.louapi.RPC.NewOrder;
import com.angeldsis.louapi.RPC.OrderUnitsCallback;
import com.angeldsis.louapi.data.Coord;
import com.angeldsis.louapi.data.UnitCount;

public class SendAttack extends FragmentBase implements OrderUnitsCallback, OnSeekBarChangeListener, OnClickListener {
	private static final String TAG = "SendAttack";
	Coord target;
	int maxloot;
	TextView debug,avail_ts;
	ListView units;
	int[] unitcounts = new int[80];
	int[] maxcounts = new int[80];
	int raidTimeReferenceType; // 0normal, 1 repeat until done
	SeekBar repeat;
	public enum AttackType {
		raid, bossraid;
	}
	AttackType attackType;
	private TextView repeatText;
	@Override public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle sis) {
		Bundle args = getArguments();
		target = Coord.fromCityId(args.getInt("target"));
		maxloot = args.getInt("maxloot");
		int zerks = args.getInt("zerks");
		Log.v(TAG,"target:"+target.format());
		ViewGroup top = (ViewGroup) inflater.inflate(R.layout.sendattack,root,false);
		
		if (zerks > 0) {
			raidTimeReferenceType = 0;
			unitcounts[6] = zerks;
			attackType = AttackType.bossraid;
		} else {
			raidTimeReferenceType = 1;
			unitcounts[6] = maxloot/10;
			attackType = AttackType.raid;
		}
		
		repeat = (SeekBar) top.findViewById(R.id.repeat);
		repeatText = (TextView) top.findViewById(R.id.repeatText);
		debug = (TextView)top.findViewById(R.id.textView2);
		avail_ts = (TextView) top.findViewById(R.id.avail_ts);
		units = (ListView) top.findViewById(R.id.units);
		repeat.setOnSeekBarChangeListener(this);
		
		Button send = (Button) top.findViewById(R.id.send);
		send.setOnClickListener(this);
		return top;
	}
	@Override public void session_ready() {
		update_units();
		ViewGroup root2 = (ViewGroup) getActivity().findViewById(R.id.second_frame);
		ViewGroup root3 = (ViewGroup) getActivity().findViewById(R.id.main_frame);
		View root = (View) root2.getParent();
		Log.v(TAG,"width:"+root.getWidth()+" width2:"+root.getLayoutParams().width);
		Log.v(TAG,"width:"+root2.getWidth()+" width2:"+root2.getLayoutParams().width);
		Log.v(TAG,"width:"+root3.getWidth()+" width2:"+root3.getLayoutParams().width);
	}
	public void sendAttack(View v) {
		try {
			JSONArray units = new JSONArray();
			int type;
			for (type=0; type < unitcounts.length; type++) {
				if (unitcounts[type] > 0) {
					JSONObject data = new JSONObject();
					data.put("t", type);
					data.put("c",unitcounts[type]);
					units.put(data);
				}
			}
			debug.setText("sending...");
			NewOrder order = new NewOrder();
			order.repeat = repeat.getProgress();
			parent.session.rpc.OrderUnits(parent.session.state.currentCity,units,target,raidTimeReferenceType,this,order);
		} catch (JSONException e) {
			e.printStackTrace(); // FIXME
			debug.setText("internal error");
		}
	}
	private void update_units() {
		Log.v(TAG,"update_units");

		if (attackType == AttackType.bossraid) {
			repeat.setMax(1);
			repeat.setProgress(1);
		} else {
			// FIXME, set max properly as the user enters things
			UnitCount[] localunits = parent.session.state.currentCity.units;
			// FIXME, support other units
			if ((localunits != null) && (localunits[6] != null)) {
				repeat.setMax(localunits[6].c / unitcounts[6]);
				repeat.setProgress(localunits[6].c / unitcounts[6]);
			}
		}

		City c = parent.session.state.currentCity;
		int i,total_ts=0;
		if (c.units == null) {
			debug.setText("no units in city");
			avail_ts.setText("");
			return;
		}
		ArrayList<UnitCount> units_present = new ArrayList<UnitCount>();
		for (i=0; i<c.units.length; i++) {
			UnitCount d = c.units[i];
			if (d == null) continue;
			total_ts += d.c;
			maxcounts[d.t] = d.c;
			Log.v(TAG,String.format("FINDME %d %d %d",d.c,d.tc,d.t));
			units_present.add(d);
		}
		units.setAdapter(new UnitAdapter(getActivity(),units_present));
		avail_ts.setText(""+total_ts);
	}
	public void gotCityData() {
		update_units();
	}
	@Override
	public void done(int r0, int r1) {
		if ((r0 == 0) && (r1 == 0) && (attackType == AttackType.bossraid)) {
			parent.session.state.recentBosses.add(target);
		}
		debug.setText(String.format("reply %d %d",r0,r1));
	}
	private class UnitAdapter extends ArrayAdapter<UnitCount> {
		public UnitAdapter(Context context, ArrayList<UnitCount> units_present) {
			super(context, 0, units_present);
		}
		public View getView(int position,View oldrow, ViewGroup root) {
			final ViewHolder holder;
			if(oldrow == null) {
				oldrow = getActivity().getLayoutInflater().inflate(R.layout.sendattack_row, root, false);
				holder = new ViewHolder();
				oldrow.setTag(holder);
				holder.name = (TextView) oldrow.findViewById(R.id.name);
				holder.counts = (TextView) oldrow.findViewById(R.id.counts);
				holder.to_send = (EditText) oldrow.findViewById(R.id.to_send);
				Button b = (Button) oldrow.findViewById(R.id.max);
				b.setOnClickListener(new OnClickListener() {
					@Override public void onClick(View v) {
						holder.setCount(maxcounts[holder.data.t]);
					}
				});
				holder.to_send.addTextChangedListener(holder);
			} else holder = (ViewHolder) oldrow.getTag();
			holder.data = getItem(position);
			
			switch (holder.data.t) {
			case 6: holder.name.setText("zerks");break;
			default: holder.name.setText("FIXME"+holder.data.t);
			}
			holder.counts.setText(""+holder.data.c);
			
			holder.to_send.setText(""+unitcounts[holder.data.t]);
			holder.updateColor();
			
			return oldrow;
		}
	}
	private class ViewHolder implements TextWatcher {
		public UnitCount data;
		TextView name,counts;
		EditText to_send;
		void setCount(int count) {
			unitcounts[data.t] = count;
			to_send.setText(""+unitcounts[data.t]);
			updateColor();
		}
		public void updateColor() {
			if (unitcounts[data.t] > maxcounts[data.t]) {
				to_send.setTextColor(Color.RED);
			} else {
				to_send.setTextColor(Color.WHITE);
			}
		}
		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			try {
				int val = Integer.parseInt(s.toString());
				unitcounts[data.t] = val;
				updateColor();
			} catch (java.lang.NumberFormatException e) {
				to_send.setTextColor(Color.RED);
			}
		}
	}
	@Override public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		String msg = this.getResources().getString(R.string.repeat_x_times);
		repeatText.setText(String.format(msg,arg1));
	}
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.send:
			sendAttack(arg0);
		}
	}
}
