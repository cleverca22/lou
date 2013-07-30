package com.angeldsis.lou;

import java.util.Arrays;
import java.util.Comparator;

import com.angeldsis.louapi.LouState.City;
import com.angeldsis.louapi.data.UnitCount;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class IdleUnits extends SessionUser implements OnItemClickListener {
	private static final String TAG = "IdleUnits";
	MyTableRow.LayoutParameters grid = new MyTableRow.LayoutParameters();
	ListView list;
	IdleUnitAdapter mAdapter;
	TextView cityname;
	@Override public void onCreate(Bundle sis) {
		super.onCreate(sis);
		setContentView(R.layout.idle_units);
		list = (ListView) findViewById(R.id.list);
		list.setOnItemClickListener(this);
		mAdapter = new IdleUnitAdapter(this);
		list.setAdapter(mAdapter);
		cityname = (TextView) findViewById(R.id.cityname);
		setTitle("Idle Units");
	}
	@Override public void session_ready() {
		session.rpc.setDefenseOverviewEnabled(true);
		onDefenseOverviewUpdate();
		updateCityName();
	}
	@Override public void onStop() {
		if (session != null) session.rpc.setDefenseOverviewEnabled(false);
		if (mAdapter != null) mAdapter.notifyDataSetInvalidated();
		super.onStop();
	}
	@Override public void onDefenseOverviewUpdate() {
		City[] list2 = new City[session.rpc.state.cities.size()];
		list2 = session.rpc.state.cities.values().toArray(list2);
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
		public IdleUnitAdapter(Context context) {
			super();
			list = new City[0];
		}
		public void update(City[] list2) {
			list = list2;
			notifyDataSetChanged();
		}
		@Override public View getView(int position, View oldrow, ViewGroup root) {
			if (oldrow == null) {
				oldrow = IdleUnits.this.getLayoutInflater().inflate(R.layout.idle_unit_row, root,false);
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
			} else unitcount.setText("none units");
			return oldrow;
		}
		@Override public int getCount() {
			return list.length;
		}
		@Override public City getItem(int position) {
			return list[position];
		}
		@Override public long getItemId(int position) {
			return getItem(position).cityid;
		}
	}
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		City c = mAdapter.getItem(arg2);
		session.rpc.state.changeCity(c);
		Exception e = new Exception();
		e.printStackTrace();
	}
	public void onCityChanged() {
		Log.v(TAG,"city changed!");
		updateCityName();
	}
	private void updateCityName() {
		cityname.setText(session.rpc.state.currentCity.name);
	}
}
