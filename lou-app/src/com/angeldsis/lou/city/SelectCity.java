package com.angeldsis.lou.city;

import java.util.Iterator;

import junit.framework.Assert;
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
	// FIXME, if you change city thru another method, it doesn't update
	// TODO, change into a fragment
	CitySelected callback;
	int palaceLocation = -1;
	public static final int ChangeCurrentCity = 1;
	public static final int ModeNormal = 2;
	public class MyAdapter extends BaseAdapter {
		@Override public int getCount() {
			return rawList.length;
		}
		@Override public Object getItem(int position) {
			return rawList[position];
		}
		@Override public long getItemId(int position) {
			Object o = getItem(position);
			if (o instanceof City) {
				City c = (City) o;
				return c.location.toCityId();
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

			if ((palaceLocation != -1) && (position == 0) && !(o instanceof City)) {
				Coord c = Coord.fromCityId((Integer)o);
				v.setText("Palace "+c.getContinent()+" "+c.format());
				return row;
			}
			
			if (o instanceof City) {
				City c = (City) o;
				if (c.location == null) {
					//throw new IllegalStateException("c.location shouldnt be null "+position);
					v.setText("ERROR "+c.name);
				} else {
					v.setText(String.format("%3s %7s %s",c.location.getContinent(),c.location.format(),c.name));
				}
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
			Object o = getItem(position);
			if (o instanceof City) return 0;
			else return 1;
		}
	}
	Object[] rawList;
	MyAdapter adapter;
	Activity mActivity;
	Spinner spinner;
	private int mode = -1;
	private LouState mState;
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
	public void setHook(CitySelected cb) {
		callback = cb;
	}
	public void session_ready(LouState state,Activity a) {
		int activeItem = -1;
		Assert.assertFalse("setMode must be called first", -1 == mode);
		mActivity = a;
		
		boolean showBookmarks = true;
		if (mode == ChangeCurrentCity) showBookmarks = false;

		int count = (palaceLocation!=-1?1:0) + state.cities.size();
		String[] bookmarks = null;
		if (showBookmarks) {
			SharedPreferences p = mActivity.getSharedPreferences("bookmarks", Context.MODE_PRIVATE);
			String part = p.getString("bookmarks", "");
			if (part.length() > 0) {
				bookmarks = part.split(",");
				count += bookmarks.length;
			}
		}
		rawList = new Object[count];
		int position = 0;
		
		if (palaceLocation != -1) rawList[position++] = palaceLocation;
		Iterator<City> it = state.cities.values().iterator();
		while (it.hasNext()) {
			rawList[position] = it.next();
			if ((mode == ChangeCurrentCity) && (rawList[position] == state.currentCity)) activeItem = position;
			position++;
		}
		mState = state;
		
		if (showBookmarks) {
			if (bookmarks != null) {
				for (String b : bookmarks) {
					rawList[position++] = Coord.fromString(b);
				}
			}
		}
		adapter = new MyAdapter();
		spinner.setAdapter(adapter);
		if (activeItem != -1) spinner.setSelection(activeItem);
	}
	@Override public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		//Log.v(TAG,"onItemSelected",new Exception());
		if (mode == ChangeCurrentCity) {
			mState.changeCity((City) adapter.getItem(arg2));
		} else callback.selected(Coord.getX(arg3),Coord.getY(arg3));
	}
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}
	public interface CitySelected {
		void selected(int x, int y);
	}
	public void setPalace(int targetCity) {
		Assert.assertFalse("setMode must be called first", -1 == mode);
		palaceLocation = targetCity;
	}
	public void setMode(int mode) {
		this.mode = mode;
	}
}
