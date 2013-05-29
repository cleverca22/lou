package com.angeldsis.lou.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.angeldsis.lou.FragmentBase;
import com.angeldsis.lou.R;
import com.angeldsis.louapi.data.Coord;
import com.angeldsis.louapi.world.WorldParser;

public class ShrineMonitor extends FragmentBase {
	private ShrineAdapter mAdapter;
	private ListView list;
	private void getShrines(int worldid) {
		final Integer[] w86shrines = { 27263141, 28573877, 9175574, 4653636 };
		// TODO: use a the database service
		if (worldid == 86) {
			done(w86shrines);
		}
	}
	private void done(Integer[] shrines) {
		// FIXME, list things better
		mAdapter = new ShrineAdapter(parent,shrines);
		list.setAdapter(mAdapter);
	}
	@Override public void session_ready() {
		parent.session.rpc.setWorldEnabled(true);
		WorldParser p = parent.session.rpc.worldParser;
		Coord c = parent.session.state.currentCity.location;
		int col = c.x/32;
		int row = c.y/32;
		p.mincol = col - 1;
		p.maxcol = col + 1;
		p.minrow = row - 1;
		p.maxrow = row + 1;
		getShrines(parent.acct.worldid);
	}
	private class ShrineAdapter extends ArrayAdapter<Integer> {
		public ShrineAdapter(Context context, Integer[] shrines) {
			super(context, 0, shrines);
		}
		@Override public View getView(int position,View convertView,ViewGroup root) {
			TextView tv;
			if (convertView == null) {
				convertView = tv = new TextView(root.getContext());
			} else tv = (TextView) convertView;
			
			Coord loc = Coord.fromCityId(getItem(position));
			tv.setText(""+loc.format());
			return convertView;
		}
	}
	@Override public void onStop() {
		if (parent.session != null) parent.session.rpc.setWorldEnabled(false);
		super.onStop();
	}
	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.shrine_monitor, container, false);
		ViewGroup vg = (ViewGroup) v;
		list = (ListView) vg.findViewById(R.id.shrines);
		return v;
	}
}
