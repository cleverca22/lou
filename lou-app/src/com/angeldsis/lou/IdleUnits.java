package com.angeldsis.lou;

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
		mAdapter = new IdleUnitAdapter(this);
		list.setAdapter(mAdapter);
	}
	private class IdleUnitAdapter extends ArrayAdapter<City> {
		public IdleUnitAdapter(Context context) {
			super(context, 0, session.rpc.state.cities);
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
			if (c.units != null) {
				int x;
				for (x=0; x<c.units.length; x++) {
					UnitCount y = c.units[x];
					if (y.t == 6) zerks.setText(""+y.c);
				}
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
