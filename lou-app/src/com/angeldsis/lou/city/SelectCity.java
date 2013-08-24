package com.angeldsis.lou.city;

import junit.framework.Assert;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.angeldsis.lou.FragmentBase;
import com.angeldsis.lou.R;
import com.angeldsis.louapi.CityGroup;
import com.angeldsis.louapi.LouState.City;
import com.angeldsis.louapi.data.Coord;

public class SelectCity extends FragmentBase implements OnItemSelectedListener {
	// FIXME, if you change city thru another method, it doesn't update
	// is the above still broken?
	CitySelected callback;
	int palaceLocation = -1;
	public static final int ChangeCurrentCity = 1;
	public static final int ModeNormal = 2;
	public class CityAdapter extends BaseAdapter {
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
				v = new TextView(getActivity());
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
	private class GroupAdapter extends BaseAdapter {
		public int getCount() {
			return groupList.length;
		}
		public long getItemId(int pos) {
			return 0;
		}
		public CityGroup getItem(int pos) {
			return groupList[pos];
		}
		public View getView(int pos, View row, ViewGroup parent) {
			CityGroup cg = getItem(pos);
			TextView v;
			if (row == null) {
				v = new TextView(getActivity());
				row = v;
			} else v = (TextView) row;
			v.setText(cg.name);
			return row;
		}
	}
	Object[] rawList;
	CityGroup[] groupList;
	GroupAdapter groupAdapter;
	CityAdapter cityAdapter;
	Spinner groupSpinner,citySpinner;
	private int mode = -1;
	private City[] cityList;
	int groupRestore = 0;
	public SelectCity() {
		Log.v("SelectCity","constructor");
	}
	@Override public void onCreate(Bundle sis) {
		Log.v("SelectCity",String.format("onCreate(%s)",sis));
		super.onCreate(sis);
		groupAdapter = new GroupAdapter();
		cityAdapter = new CityAdapter();
	}
	public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle sis) {
		Log.v("SelectCity","onCreateView "+this+" "+groupRestore);
		ViewGroup top = (ViewGroup) inflater.inflate(R.layout.select_city, root, false);
		groupSpinner = (Spinner) top.findViewById(R.id.group);
		citySpinner = (Spinner) top.findViewById(R.id.city);
		//LayoutParams d = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
		//addView(spinner,d);
		//if (this.isInEditMode()) {
			/*allCities = new City[0];
			allBookmarks = new int[1];
			allBookmarks[0] = 30605677;
			spinner.setAdapter(adapter);*/
		//}
		groupSpinner.setOnItemSelectedListener(this);
		citySpinner.setOnItemSelectedListener(this);
		
		if (sis != null) {
			groupRestore = sis.getInt("group");
			Log.v("SelectCity","i have saved state");
		}
		return top;
	}
	public void onDestroyView() {
		super.onDestroyView();
		groupSpinner = null;
		citySpinner = null;
		groupList = null;
		cityList = null;
		rawList = null;
		Log.v("SelectCity","onDestroyView");
	}
	public void setHook(CitySelected cb) {
		callback = cb;
	}
	public void session_ready() {
		Log.v("SelectCity","session_ready "+groupRestore);
		int activeItem;
		Assert.assertFalse("setMode must be called first", -1 == mode);
		
		if (groupList == null) {
			groupList = parent.session.state.groups;
			groupSpinner.setAdapter(groupAdapter);
			cityList = groupList[groupRestore].cities;
			groupSpinner.setSelection(groupRestore);
			
			activeItem = rebuildList();
			
			citySpinner.setAdapter(cityAdapter);
			if (activeItem != -1) citySpinner.setSelection(activeItem);
			Log.v("SelectCity","a"+groupAdapter.getCount()+" b"+cityAdapter.getCount());
		} else rebuildList();
	}
	@Override public void onSaveInstanceState(Bundle sis) {
		super.onSaveInstanceState(sis);
		sis.putInt("group", groupSpinner.getSelectedItemPosition());
		Exception e = new Exception();
		e.printStackTrace();
	}
	private int rebuildList() {
		int activeItem = -1;
		boolean showBookmarks = true;
		if (mode == ChangeCurrentCity) showBookmarks = false;

		int count = (palaceLocation!=-1?1:0) + cityList.length;
		String[] bookmarks = null;
		if (showBookmarks) {
			SharedPreferences p = getActivity().getSharedPreferences("bookmarks", Context.MODE_PRIVATE);
			String part = p.getString("bookmarks", "");
			if (part.length() > 0) {
				bookmarks = part.split(",");
				count += bookmarks.length;
			}
		}
		rawList = new Object[count];
		int position = 0;
		
		if (palaceLocation != -1) rawList[position++] = palaceLocation;
		int i;
		for (i=0; i<cityList.length; i++) {
			rawList[position] = cityList[i];
			if ((mode == ChangeCurrentCity) && (rawList[position] == parent.session.state.currentCity)) activeItem = position;
			position++;
		}
		
		if (showBookmarks) {
			if (bookmarks != null) {
				for (String b : bookmarks) {
					rawList[position++] = Coord.fromString(b);
				}
			}
		}
		cityAdapter.notifyDataSetChanged();
		return activeItem;
	}
	@Override public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long arg3) {
		Log.v("SelectCity","onItemSelected"+arg0+" "+arg0.getSelectedItemPosition()+" "+pos);
		switch (arg0.getId()) {
		case R.id.group:
			Log.v("SelectCity","group "+pos);
			groupRestore = pos;
			cityList = groupList[pos].cities;
			rebuildList();
			break;
		case R.id.city:
			if (mode == ChangeCurrentCity) {
				parent.session.state.changeCity((City) cityAdapter.getItem(pos));
			} else callback.selected(Coord.getX(arg3),Coord.getY(arg3));
			break;
		default:
			Log.v("SelectCity","bad id"+arg1.getId());
		}
	}
	@Override public void onCityChanged() {
		// FIXME, make sure this works
		if (mode == ChangeCurrentCity) {
			int i;
			City findme = parent.session.state.currentCity;
			for (i=0; i<rawList.length; i++) {
				if (rawList[i] == findme) {
					citySpinner.setSelection(i);
					break;
				}
			}
		}
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
