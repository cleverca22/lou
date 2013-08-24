package com.angeldsis.lou.fragments;

import java.util.Arrays;
import java.util.Comparator;

import com.angeldsis.lou.FragmentBase;
import com.angeldsis.lou.MyTableRow;
import com.angeldsis.lou.R;
import com.angeldsis.lou.SessionUser;
import com.angeldsis.lou.MyTableRow.LayoutParameters;
import com.angeldsis.lou.R.id;
import com.angeldsis.lou.R.layout;
import com.angeldsis.louapi.LouState.City;
import com.angeldsis.louapi.data.UnitCount;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class IdleUnits extends FragmentBase implements OnItemClickListener {
	private static final String TAG = "IdleUnits";
	MyTableRow.LayoutParameters grid = new MyTableRow.LayoutParameters();
	ListView list;
	IdleUnitAdapter mAdapter;
	TextView cityname;
	@Override public void onCreate(Bundle sis) {
		super.onCreate(sis);
		//setTitle("Idle Units");
	}
	@Override public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle sis) {
		final ViewGroup root2 = (ViewGroup) inflater.inflate(R.layout.idle_units, root, false);
		list = (ListView) root2.findViewById(R.id.list);
		list.setOnItemClickListener(this);
		mAdapter = new IdleUnitAdapter();
		list.setAdapter(mAdapter);
		cityname = (TextView) root2.findViewById(R.id.cityname);
		
		return root2;
	}
	@Override public void session_ready() {
		parent.session.rpc.setDefenseOverviewEnabled(true);
		onDefenseOverviewUpdate();
		updateCityName();
	}
	@Override public void onStop() {
		if (parent != null && parent.session != null) parent.session.rpc.setDefenseOverviewEnabled(false);
		if (mAdapter != null) mAdapter.notifyDataSetInvalidated();
		super.onStop();
	}
	@Override public void onDefenseOverviewUpdate() {
		City[] list2 = new City[parent.session.rpc.state.cities.size()];
		list2 = parent.session.rpc.state.cities.values().toArray(list2);
		Arrays.sort(list2, new Comparator<City>(){
			@Override
			public int compare(City a, City b) {
				int ac,bc;
				
				ac = a.getTotalArmy();
				bc = b.getTotalArmy();
				
				if (ac < bc) return 1;
				if (ac > bc) return -1;
				return 0;
			}});
		mAdapter.update(list2);
	}
	private class IdleUnitAdapter extends BaseAdapter {
		City[] list;
		public IdleUnitAdapter() {
			super();
			list = new City[0];
		}
		public void update(City[] list2) {
			list = list2;
			notifyDataSetChanged();
		}
		@Override public View getView(int position, View oldrow, ViewGroup root) {
			if (oldrow == null) {
				oldrow = parent.getLayoutInflater().inflate(R.layout.idle_unit_row, root,false);
			}
			MyTableRow row = (MyTableRow) oldrow;
			row.bind(grid);
			TextView city = (TextView) row.findViewById(R.id.cityname);
			TextView unitcount = (TextView)row.findViewById(R.id.unitcount);
			City c = getItem(position);
			city.setText(c.name);
			if (c.units != null) {
				StringBuilder b = new StringBuilder();
				if (c.units[UnitCount.ZERK] != null) {
					b.append("zerks:"+c.units[UnitCount.ZERK].c);
				}
				if (c.units[UnitCount.PALADIN] != null) {
					b.append("paladins:"+c.units[UnitCount.PALADIN].c);
				}
				if (b.length() == 0) b.append("other");
				unitcount.setText(b.toString());
			} else unitcount.setText("no units");
			return oldrow;
		}
		@Override public int getCount() {
			return list.length;
		}
		@Override public City getItem(int position) {
			return list[position];
		}
		@Override public long getItemId(int position) {
			return getItem(position).location.toCityId();
		}
	}
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		City c = mAdapter.getItem(arg2);
		parent.session.rpc.state.changeCity(c);
		//Exception e = new Exception();
		//e.printStackTrace();
	}
	@Override public void onCityChanged() {
		Log.v(TAG,"city changed!");
		updateCityName();
	}
	private void updateCityName() {
		cityname.setText(parent.session.rpc.state.currentCity.name);
	}
}
