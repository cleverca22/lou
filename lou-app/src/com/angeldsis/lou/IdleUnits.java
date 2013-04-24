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
import android.widget.ArrayAdapter;
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
		list2 = session.rpc.state.cities.toArray(list2);
		Arrays.sort(list2, new Comparator<City>(){
			@Override
			public int compare(City a, City b) {
				int ac,bc;
				if (a.units == null) ac = -1;
				else if (a.units[6] == null) ac = -1;
				else ac = a.units[6].c;
				
				if (b.units == null) bc = -1;
				else if (b.units[6] == null) bc = -1;
				else bc = b.units[6].c;
				
				if (ac < bc) return 1;
				if (ac > bc) return -1;
				return 0;
			}});
		mAdapter = new IdleUnitAdapter(this,list2);
		list.setAdapter(mAdapter);
	}
	private class IdleUnitAdapter extends ArrayAdapter<City> {
		public IdleUnitAdapter(Context context, City[] list2) {
			super(context, 0, list2);
		}
		@Override public View getView(int position, View oldrow, ViewGroup root) {
			if (oldrow == null) {
				oldrow = IdleUnits.this.getLayoutInflater().inflate(R.layout.idle_unit_row, root,false);
			}
			MyTableRow row = (MyTableRow) oldrow;
			row.bind(grid);
			TextView city = (TextView) row.findViewById(R.id.cityname);
			TextView zerks = (TextView)row.findViewById(R.id.unitcount);
			City c = getItem(position);
			city.setText(c.name);
			if ((c.units != null) && (c.units[6] != null)) {
				zerks.setText(""+c.units[6].c);
			} else zerks.setText("none");
			return oldrow;
		}
	}
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		City c = mAdapter.getItem(arg2);
		session.rpc.state.changeCity(c);
	}
	public void cityChanged() {
		Log.v(TAG,"city changed!");
		updateCityName();
	}
	private void updateCityName() {
		cityname.setText(session.rpc.state.currentCity.name);
	}
}
