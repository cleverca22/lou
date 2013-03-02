package com.angeldsis.lou.city;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.angeldsis.louapi.LouState;
import com.angeldsis.louapi.LouState.City;
import com.angeldsis.louapi.data.Coord;

public class SelectCity extends LinearLayout implements OnItemSelectedListener {
	CitySelected callback;
	private static final String TAG = "SelectCity";
	public class MyAdapter extends BaseAdapter {
		@Override public int getCount() {
			return SelectCity.this.allCities.length + SelectCity.this.allBookmarks.length;
		}
		@Override public Object getItem(int position) {
			int size1 = SelectCity.this.allCities.length;
			if (position < size1) return SelectCity.this.allCities[position];
			else return SelectCity.this.allBookmarks[position - size1];
		}
		@Override public long getItemId(int position) {
			Object o = getItem(position);
			if (o instanceof City) {
				City c = (City) o;
				return c.cityid;
			}
			return (Integer)o;
		}
		@Override public View getView(int position, View row, ViewGroup parent) {
			Object o = getItem(position);
			TextView v;
			if (row == null) {
				v = new TextView(getContext());
				row = v;
			} else v = (TextView) row;

			if (position < SelectCity.this.allCities.length) {
				City c = (City) o;
				v.setText(String.format("%3s %7s %s",c.location.getContinent(),c.location.format(),c.name));
			} else {
				Coord c = Coord.fromCityId((Integer)o);
				v.setText(c.getContinent()+" "+c.format());
			}
			return row;
		}
		public int getViewTypeCount() {
			return 2;
		}
		public int getItemViewType(int position) {
			if (position < SelectCity.this.allCities.length) return 0;
			else return 1;
		}
	}
	City[] allCities;
	int[] allBookmarks;
	MyAdapter adapter;
	Activity mActivity;
	Spinner spinner;
	public SelectCity(Context context) {
		super(context);
		init(context);
	}
	public SelectCity(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	private void init(Context context) {
		spinner = new Spinner(context);
		LayoutParams d = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
		addView(spinner,d);
		if (this.isInEditMode()) {
			/*allCities = new City[0];
			allBookmarks = new int[1];
			allBookmarks[0] = 30605677;
			spinner.setAdapter(adapter);*/
		}
		spinner.setOnItemSelectedListener(this);
	}
	public void session_ready(LouState state,Activity a, CitySelected cb) {
		mActivity = a;
		callback = cb;
		allCities = new City[state.cities.size()];
		int i = 0;
		for (City c : state.cities) {
			allCities[i] = c;
			i++;
		}
		SharedPreferences p = mActivity.getSharedPreferences("bookmarks", Context.MODE_PRIVATE);
		String part = p.getString("bookmarks", "");
		if (part.length() > 0) {
			String[] bookmarks = part.split(",");
			allBookmarks = new int[bookmarks.length];
			i = 0;
			for (String b : bookmarks) {
				allBookmarks[i] = Coord.fromString(b);
				i++;
			}
		} else allBookmarks = new int[0];
		adapter = new MyAdapter();
		spinner.setAdapter(adapter);
	}
	@Override public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		callback.selected(Coord.getX(arg3),Coord.getY(arg3));
	}
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	public interface CitySelected {
		void selected(int x, int y);
	}
}
