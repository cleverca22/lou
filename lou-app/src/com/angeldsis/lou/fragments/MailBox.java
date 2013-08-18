package com.angeldsis.lou.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.angeldsis.lou.FragmentBase;
import com.angeldsis.lou.MyTableRow;
import com.angeldsis.lou.MyTableRow.LayoutParameters;
import com.angeldsis.lou.R;
import com.angeldsis.louapi.MailBoxFolder;
import com.angeldsis.louapi.MailHeader;
import com.angeldsis.louapi.RPC.MailBoxCallback;
import com.angeldsis.louapi.RPC.MessageCountCallback;
import com.angeldsis.louapi.RPC.MessageHeaderCallback;

public class MailBox extends FragmentBase implements MailBoxCallback, MessageCountCallback, MessageHeaderCallback, OnItemClickListener {
	static private final String TAG = "MailBox";
	LayoutParameters grid;
	MailBoxFolder inbox,outbox;
	MailListAdapter adapter;
	@Override public void session_ready() {
		Log.v(TAG,"getting folders");
		parent.session.rpc.IGMGetFolders(this);
	}
	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.simple_list, container, false);
		ListView list = (ListView) vg.findViewById(R.id.list);
		adapter = new MailListAdapter();
		list.setAdapter(adapter);
		grid = new LayoutParameters();
		list.setOnItemClickListener(this);
		return vg;
	}
	@Override public void done(MailBoxFolder[] folders) {
		Log.v(TAG,"got folders");
		int x;
		for (x=0; x<folders.length; x++) {
			if (folders[x].name.equals("@In")) inbox = folders[x];
			else if (folders[x].name.equals("@Out")) outbox = folders[x];
		}
		Log.v(TAG,"getting inbox");
		if (parent == null) return;
		if (parent.session == null) return;
		parent.session.rpc.IGMGetMsgCount(inbox,this);
	}
	@Override public void gotCount(int count, MailBoxFolder folder) {
		Log.v(TAG,"message count: "+count);
		parent.session.rpc.IGMGetMsgHeader(0,20,inbox,3,false,true,this);
	}
	@Override public void gotHeaders(MailHeader[] headers) {
		adapter.setData(headers);
	}
	private class MailListAdapter extends BaseAdapter {
		MailHeader[] data;
		MailListAdapter() {
			data = new MailHeader[0];
		}
		public void setData(MailHeader[] headers) {
			data = headers;
			notifyDataSetChanged();
		}
		public int getCount() {
			return data.length;
		}
		public MailHeader getItem(int position) {
			return data[position];
		}
		public long getItemId(int position) {
			return 0;
		}
		public View getView(int position, View convertView, ViewGroup root) {
			MyTableRow row;
			Holder h;
			if (convertView == null) {
				row = (MyTableRow) getActivity().getLayoutInflater().inflate(R.layout.mail_header, root, false);
				row.setClickable(true);
				row.bind(grid);
				h = new Holder();
				h.subject = (TextView) row.findViewById(R.id.subject);
				h.from = (TextView) row.findViewById(R.id.from);
				h.ts = (TextView) row.findViewById(R.id.timestamp);
				row.setTag(h);
				convertView = row;
			} else h = (Holder) convertView.getTag();
			MailHeader header = getItem(position);
			h.subject.setText(header.subject);
			h.from.setText(header.from);
			return convertView;
		}
	}
	private static class Holder {
		public TextView subject,from,ts;
	}
	@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		MailHeader h = adapter.getItem(position);
		Log.v(TAG,"mail clicked:"+h.id);
	}
}
