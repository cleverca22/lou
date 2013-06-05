package com.angeldsis.lou.fragments;

import java.util.TreeMap;

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
import com.angeldsis.lou.R;
import com.angeldsis.louapi.LouState.City;

public class FoodWarnings extends FragmentBase implements OnItemClickListener {
	private static final String TAG = "FoodWarnings";
	MyTableRow.LayoutParameters grid = new MyTableRow.LayoutParameters();
	ListView list;
	private FoodAdapter mAdapter;
	private class FoodAdapter extends BaseAdapter {
		private City[] data;

		@Override public int getCount() {
			if (data == null) return 0;
			return data.length;
		}
		@Override public City getItem(int arg0) {
			return data[arg0];
		}
		@Override public long getItemId(int arg0) {
			return data[arg0].cityid;
		}
		@Override public View getView(int position, View convertView, ViewGroup root) {
			if (convertView == null) {
				convertView = parent.getLayoutInflater().inflate(R.layout.food_warning_row, root, false);
			}
			MyTableRow row = (MyTableRow) convertView;
			row.bind(grid);
			TextView name = (TextView) row.findViewById(R.id.name);
			TextView hours = (TextView) row.findViewById(R.id.hours);
			City c = getItem(position);
			name.setText(c.name);

			int secondsleft = c.foodEmptyTime(parent.session.rpc.state);
			hours.setText(String.format("%2d:%02d",secondsleft/60/60,secondsleft/60%60));
			return convertView;
		}

		public void update(TreeMap<Integer, City> warnings) {
			data = new City[warnings.size()];
			warnings.values().toArray(data);
			notifyDataSetChanged();
		}
		
	}
	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		mAdapter = new FoodAdapter();
	}
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.simple_list, container, false);
		ViewGroup vg = (ViewGroup) v;
		list = (ListView) vg.findViewById(R.id.list);
		list.setAdapter(mAdapter);
		list.setOnItemClickListener(this);
		return v;
	}
	@Override public void session_ready() {
		TreeMap<Integer,City> warnings = parent.session.rpc.foodWarnings.warnings;
		Log.v("FoodWarnings","warning count:"+warnings.size());
		mAdapter.update(warnings);
		parent.setTitle("Food Warnings");
	}
	public void onFoodWarning() {
		mAdapter.update(parent.session.rpc.foodWarnings.warnings);
	}
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		Log.v(TAG,"id:"+id);
		parent.session.state.changeCity(mAdapter.getItem(position));
	}
}
