package com.angeldsis.lou.fragments;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeMap;

import android.content.Context;
import android.content.Intent;
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

import com.angeldsis.lou.AccountWrap;
import com.angeldsis.lou.CityCore;
import com.angeldsis.lou.FragmentBase;
import com.angeldsis.lou.MyTableRow;
import com.angeldsis.lou.R;
import com.angeldsis.lou.SessionKeeper;
import com.angeldsis.lou.SingleFragment;
import com.angeldsis.louapi.LouState;
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
			TextView end_time = (TextView) row.findViewById(R.id.end_time);
			City c = getItem(position);
			name.setText(c.name);

			int secondsleft = c.foodEmptyTime(parent.session.rpc.state);
			hours.setText(String.format("%2d:%02d",secondsleft/60/60,secondsleft/60%60));
			end_time.setText(parent.session.state.stepToString(parent.session.state.getServerStep() + secondsleft));
			return convertView;
		}

		public void update(City[] list2) {
			data = list2;
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
		onFoodWarning();
		parent.setTitle("Food Warnings");
	}
	public void gotCityData() {
		onFoodWarning();
	}
	private static class mysort implements Comparator<City> {
		private LouState state;
		public mysort(LouState state) {
			this.state = state;
		}
		@Override public int compare(City lhs, City rhs) {
			int l = lhs.foodEmptyTime(state);
			int r = rhs.foodEmptyTime(state);
			if (l < r) return -1;
			if (l > r) return 1;
			return 0;
		}
	}
	public void onFoodWarning() {
		Collection<City> list = parent.session.rpc.foodWarnings.warnings.values();
		City[] list2 = list.toArray(new City[list.size()]);
		Arrays.sort(list2, new mysort(parent.session.state));
		mAdapter.update(list2);
	}
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		Log.v(TAG,"id:"+id);
		parent.session.state.changeCity(mAdapter.getItem(position));
	}
	public static Intent getIntent(AccountWrap acct, Context context) {
		Bundle options = acct.toBundle();
		options.putSerializable("fragment2", FoodWarnings.class);
		options.putSerializable("fragment", CityCore.class);
		options.putInt("layout", R.layout.fragment_pair);
		Intent intent = new Intent(context,SingleFragment.class);
		intent.putExtras(options);
		return intent;
	}
}
