package com.angeldsis.lou.reports;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.TimeZone;

import org.json2.JSONArray;
import org.json2.JSONException;
import org.json2.JSONObject;
import org.json2.JSONTokener;

import com.angeldsis.lou.R;
import com.angeldsis.lou.SessionUser;
import com.angeldsis.louapi.RPC.ReportHeaderCallback;
import com.angeldsis.louapi.ReportDumper;
import com.angeldsis.louapi.ReportHeader;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class Reports extends SessionUser implements ReportHeaderCallback, OnItemClickListener, OnClickListener {
	static final private String TAG = "Reports";
	boolean refreshing = false;
	ReportListAdapter mAdapter;
	ReportHeader[] list;
	boolean flipped = false;
	long load_start;
	private TimeZone tz;
	private static int filterCheckboxIds[] = { R.id.scout, R.id.plunder, R.id.assault,
		R.id.seige, R.id.raid_boss, R.id.raid_dungeon, R.id.support, R.id.trade,
		R.id.other_reports, R.id.skirmish, R.id.read, R.id.unread };
	CheckBox filterCheckboxes[];
	CheckBox incoming,outgoing;
	int activeMask;
	@Override
	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		if (sis == null) {
		}
		setContentView(R.layout.report_list);
		filterCheckboxes = new CheckBox[filterCheckboxIds.length];
		int i;
		for (i=0; i<filterCheckboxIds.length; i++) {
			filterCheckboxes[i] = (CheckBox) findViewById(filterCheckboxIds[i]);
			filterCheckboxes[i].setOnClickListener(this);
		}
		incoming = (CheckBox) findViewById(R.id.incoming);
		incoming.setOnClickListener(this);
		outgoing = (CheckBox) findViewById(R.id.outgoing);
		outgoing.setOnClickListener(this);
		mAdapter = new ReportListAdapter(this);
		ListView listview = (ListView) findViewById(R.id.reports);
		listview.setAdapter(mAdapter);
		listview.setOnItemClickListener(this);
	}
	@Override protected void onStart() {
		//mAdapter.clear();
		super.onStart();
	}
	@Override
	public void session_ready() {
		String uireports = session.state.getConfig("ur");
		if (uireports == null) {
			int i;
			for (i=0; i<filterCheckboxes.length; i++) {
				filterCheckboxes[i].setChecked(true);
			}
			incoming.setChecked(true);
			outgoing.setChecked(true);
			activeMask = getMask();
		} else {
			try {
				JSONObject config;
				config = (JSONObject) new JSONTokener(uireports).nextValue();
				JSONArray filterCheckBoxValues = config.getJSONArray("filterCheckBoxValues");
				int i;
				for (i=0; i<filterCheckboxes.length; i++) {
					filterCheckboxes[i].setChecked(filterCheckBoxValues.getBoolean(i));
				}
				JSONArray directionCheckBoxValues = config.getJSONArray("directionCheckBoxValues");
				incoming.setChecked(directionCheckBoxValues.getBoolean(0));
				outgoing.setChecked(directionCheckBoxValues.getBoolean(1));
				Log.v(TAG,"uireports:"+config.toString(1));
				activeMask = getMask();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.tz = session.rpc.state.tz;
		refresh();
	}
	private int getMask() {
		int i;
		int mask = 0;
		for (i=0; i<filterCheckboxes.length; i++) {
			if (filterCheckboxes[i].isChecked()) mask |= 1 << i;
		}
		if (incoming.isChecked()) mask |= 1 << 16;
		if (outgoing.isChecked()) mask |= 1 << 17;
		Log.v(TAG,"mask is "+mask);
		return mask;
	}
	private void refresh() {
		if (refreshing) {
			return;
		}
		load_start = System.currentTimeMillis();
		// 0->99 == 100 rows (inclusive it seems)
		session.rpc.ReportGetHeader("kashikoi",-1,0,999,1,false,activeMask,this);
	}
	@Override
	public void done(ReportHeader[] list) {
		ViewSwitcher f = (ViewSwitcher) findViewById(R.id.flipper);
		Log.v(TAG,"list size:"+list.length);
		mAdapter.clear();
		for (ReportHeader h : list) mAdapter.add(h);
		if (!flipped) {
			f.showNext();
			flipped = true;
		}
		this.list = list;
	}
	class ReportListAdapter extends ArrayAdapter<ReportHeader> {
		ReportListAdapter(Context c) {
			super(c,0);
		}
		@Override public long getItemId(int position) {
			return getItem(position).id;
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			//Log.v(TAG,String.format("getView(%d,%s,%s) %d",position,convertView,parent,getCount()));
			ReportHeader h = getItem(position);
			
			ViewGroup row;
			if (convertView == null) {
				row = (ViewGroup) Reports.this.getLayoutInflater().inflate(R.layout.report_row, parent,false);
			} else {
				row = (ViewGroup) convertView;
			}
			//if (session == null) return row; // FIXME

			TextView col1 = (TextView) row.findViewById(R.id.msg);
			col1.setText(h.toString());
			TextView col2 = (TextView) row.findViewById(R.id.stamp);
			//Log.v(TAG,"tz:"+tz);
			String ts = h.formatTime(tz);
			col2.setText(ts);
			
			return row;
		}
	}
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		Log.v(TAG,String.format("onItemClick(%s,%s,%d,%d)",arg0,arg1,position,id));
		Bundle args = acct.toBundle();
		args.putInt("reportid", (int) id);
		Intent i = new Intent(this,ShowReport.class);
		i.putExtras(args);
		startActivity(i);
	}
	public void doExport(View v) {
		try {
			FileOutputStream f = this.openFileOutput("export", MODE_PRIVATE);
			ReportDumper d = new ReportDumper(session.rpc);
			d.dumpReports(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override public void onClick(View view) {
		int newMask = getMask();
		if (newMask != activeMask) {
			activeMask = newMask;
			refresh();
		}
	}
}
