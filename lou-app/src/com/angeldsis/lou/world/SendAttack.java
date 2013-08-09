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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.angeldsis.lou.R;
import com.angeldsis.lou.SessionUser;
import com.angeldsis.louapi.LouState.City;
import com.angeldsis.louapi.RPC.OrderUnitsCallback;
import com.angeldsis.louapi.data.Coord;
import com.angeldsis.louapi.data.UnitCount;

public class SendAttack extends SessionUser implements OrderUnitsCallback {
	private static final String TAG = "SendAttack";
	Coord target;
	int maxloot;
	TextView debug,avail_ts;
	ListView units;
	int[] unitcounts = new int[20];
	int[] maxcounts = new int[20];
	@Override public void onCreate(Bundle sis) {
		super.onCreate(sis);
		Intent msg = getIntent();
		Bundle args = msg.getExtras();
		target = Coord.fromCityId(args.getInt("target"));
		maxloot = args.getInt("maxloot");
		int zerks = args.getInt("zerks");
		Log.v(TAG,"target:"+target.format());
		setContentView(R.layout.sendattack);
		
		if (zerks > 0) unitcounts[6] = zerks;
		else unitcounts[6] = maxloot/10;
		
		debug = (TextView)findViewById(R.id.textView2);
		avail_ts = (TextView) findViewById(R.id.avail_ts);
		units = (ListView) findViewById(R.id.units);
	}
	@Override public void session_ready() {
		update_units();
	}
	public void sendAttack(View v) throws JSONException {
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
		session.rpc.OrderUnits(session.rpc.state.currentCity,units,target,this);
	}
	private void update_units() {
		Log.v(TAG,"update_units");
		City c = session.rpc.state.currentCity;
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
		units.setAdapter(new UnitAdapter(this,units_present));
		avail_ts.setText(""+total_ts);
	}
	public void gotCityData() {
		update_units();
	}
	@Override
	public void done(int r0, int r1) {
		debug.setText(String.format("reply %d %d",r0,r1));
	}
	private class UnitAdapter extends ArrayAdapter<UnitCount> {
		public UnitAdapter(Context context, ArrayList<UnitCount> units_present) {
			super(context, 0, units_present);
		}
		public View getView(int position,View oldrow, ViewGroup root) {
			final ViewHolder holder;
			if(oldrow == null) {
				oldrow = SendAttack.this.getLayoutInflater().inflate(R.layout.sendattack_row, root, false);
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
}
