package com.angeldsis.lou.reports;

import com.angeldsis.lou.R;
import com.angeldsis.lou.SessionUser;
import com.angeldsis.louapi.RPC.ReportHeaderCallback;
import com.angeldsis.louapi.ReportHeader;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class Reports extends SessionUser implements ReportHeaderCallback, OnItemClickListener {
	static final private String TAG = "Reports";
	boolean refreshing = false;
	ReportListAdapter mAdapter;
	ReportHeader[] list;
	boolean flipped = false;
	long load_start;
	@Override
	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		if (sis == null) {
		}
		setContentView(R.layout.report_list);
		mAdapter = new ReportListAdapter(this);
		ListView listview = (ListView) findViewById(R.id.reports);
		listview.setAdapter(mAdapter);
		listview.setOnItemClickListener(this);
	}
	@Override
	public void session_ready() {
		refresh();
	}
	private void refresh() {
		if (refreshing) {
			return;
		}
		load_start = System.currentTimeMillis();
		// 0->99 == 100 rows (inclusive it seems)
		session.rpc.ReportGetHeader("kashikoi",-1,0,999,1,false,200575,this);
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
			//Log.v(TAG,String.format("getView(%d,%s,%s)",position,convertView,parent));
			ReportHeader h = getItem(position);
			
			ViewGroup row = (ViewGroup) Reports.this.getLayoutInflater().inflate(R.layout.report_row, parent,false);

			TextView col1 = (TextView) row.findViewById(R.id.msg);
			col1.setText(h.toString());
			TextView col2 = (TextView) row.findViewById(R.id.stamp);
			col2.setText(h.formatTime());
			
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
}
