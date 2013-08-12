package com.angeldsis.lou;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

import com.angeldsis.lou.city.SelectCity;
import com.angeldsis.lou.city.SendTrade;
import com.angeldsis.lou.fragments.ResourceBar;
import com.angeldsis.louapi.EnlightenedCities.EnlightenedCity;
import com.angeldsis.louapi.LouState;
import com.angeldsis.louapi.LouState.City;
import com.angeldsis.louapi.data.Coord;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

public class EnlightenedCityList extends SessionUser {
	private static final String TAG = "EnlightenedCityList";
	MyTableRow.LayoutParameters params;
	CityList adapter;
	boolean loaded;
	boolean filter = true;
	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		params = new MyTableRow.LayoutParameters();
		setContentView(R.layout.el_city_list);
		adapter = new CityList();
		((ListView)findViewById(R.id.list)).setAdapter(adapter);
	}
	private static class ViewHolder {
		public TextView coord;
		public TextView level;
		public TextView wood;
		public TextView stone;
		public EnlightenedCity city;
		public TextView comment,start;
	}
	@Override public void session_ready() {
		Log.v(TAG,"session_ready()");
		onEnlightenedCityChanged();
		if (!loaded) {
			SelectCity s = (SelectCity) findViewById(R.id.selectCity);
			s.setMode(SelectCity.ChangeCurrentCity);
			s.session_ready(session.rpc.state,this);
			loaded = true;
		}
		((ResourceBar)findViewById(R.id.resourceBar)).setState(session.state);
		super.session_ready();
	}
	@Override public void onEnlightenedCityChanged() {
		Log.v(TAG,"onEnlightenedCityChanged()");
		EnlightenedCity[] data;
		TreeMap<Integer,EnlightenedCity> datain = session.rpc.enlightenedCities.data;
		data = new EnlightenedCity[datain.size()];
		datain.values().toArray(data);
		adapter.setData(datain.values());
	}
	private class CityList extends BaseAdapter {
		Collection<EnlightenedCity> realData;
		ArrayList<EnlightenedCity> data;
		int continent;
		CityList() {
			data = new ArrayList<EnlightenedCity>();
		}
		@Override public int getCount() {
			if (data == null) return 0;
			return data.size();
		}
		public void setData(Collection<EnlightenedCity> collection) {
			realData = collection;
			filterContinent(continent);
		}
		@Override public long getItemId(int position) {
			return getItem(position).id;
		}
		@Override public EnlightenedCity getItem(int position) {
			return data.get(position);
		}
		public View getView(int position,View convertView,ViewGroup root) {
			final ViewHolder holder;
			MyTableRow row;
			if (convertView == null) {
				convertView = EnlightenedCityList.this.getLayoutInflater().inflate(R.layout.el_city_row, root, false);
				row = (MyTableRow) convertView;
				row.bind(params);
				holder = new ViewHolder();
				row.setTag(holder);
				holder.coord = (TextView)row.findViewById(R.id.coord);
				holder.level = (TextView)row.findViewById(R.id.level);
				holder.wood = (TextView)row.findViewById(R.id.wood);
				holder.stone = (TextView)row.findViewById(R.id.stone);
				holder.comment = (TextView) row.findViewById(R.id.comment);
				holder.start = (TextView) row.findViewById(R.id.start);
				Button b = (Button) row.findViewById(R.id.button);
				b.setOnClickListener(new OnClickListener() {
					@Override public void onClick(View v) {
						Log.v(TAG,""+holder.city.id);
						Intent i = new Intent(EnlightenedCityList.this,SendTrade.class);
						i.putExtras(acct.toBundle());
						i.putExtra("targetCity", holder.city.id);
						startActivity(i);
					}});
			} else {
				row = (MyTableRow) convertView;
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.city = getItem(position);
			Coord coord = Coord.fromCityId(holder.city.id);
			holder.level.setText(""+holder.city.palace_level);
			holder.coord.setText(coord.format());
			holder.comment.setText(holder.city.comment);
			
			holder.start.setText(session.state.stepToString(holder.city.endstep));
			
			long needed = EnlightenedCity.res_needed[holder.city.palace_level];
			long missing_wood = needed - (holder.city.wood + holder.city.getResourceCount(session.rpc.state, 0) + holder.city.incoming_wood);
			long missing_stone = needed - (holder.city.stone + holder.city.getResourceCount(session.rpc.state, 1) + holder.city.incoming_stone);
			
			// [1:00:08 AM] [RD] Mr. Liver: if hlp & !not full > greeen or highlighting in some way
			TypedArray a = obtainStyledAttributes(null, R.styleable.ElCityList);
			int color = a.getColor(R.styleable.ElCityList_rowHighlight1, 0);
			a.recycle();
			if (color == 0) { throw new IllegalStateException("You forgot to put the attr on the theme, artard"); }
			int green = color;
			int transparent = Color.TRANSPARENT;
			if (holder.city.comment.contains(">100% Faith")) {
				row.setBackgroundColor(transparent);
			} else if ((missing_wood > 0) || (missing_stone > 0)) {
				row.setBackgroundColor(green);
			} else if (holder.city.comment.contains("HLP")) {
				row.setBackgroundColor(green);
			} else row.setBackgroundColor(transparent);
			
			// FIXME, find a color/icon scheme, to save space
			if (missing_wood > 0) holder.wood.setText(Utils.NumberFormat(missing_wood));
			else holder.wood.setText("overfilled "+Utils.NumberFormat(missing_wood * -1));
			
			if (missing_stone > 0) holder.stone.setText(""+Utils.NumberFormat(missing_stone));
			else holder.stone.setText("overfilled "+Utils.NumberFormat(missing_stone * -1));
			return convertView;
		}
		public void filterContinent(int continent) {
			this.continent = continent;
			data.clear();
			if (continent == -1) {
				// FIXME
				Iterator<EnlightenedCity> i = realData.iterator();
				while (i.hasNext()) {
					data.add(i.next());
				}
			} else {
				Iterator<EnlightenedCity> i = realData.iterator();
				while (i.hasNext()) {
					EnlightenedCity c = i.next();
					if (c.location.getContinentInt() == continent) data.add(c);
				}
			}
			notifyDataSetChanged();
		}
	}
	public void filterChanged(View v) {
		CheckBox c = (CheckBox) v;
		filter = c.isChecked();
		onCityChanged();
	}
	public void onCityChanged() {
		if (filter) {
			LouState state = session.state;
			City city = state.currentCity;
			Coord location = city.location;
			int continent = location.getContinentInt();
			adapter.filterContinent(continent);
		} else adapter.filterContinent(-1);
	}
	public void gotCityData() {
		Log.v(TAG,"gotCityData()");
		((TextView)findViewById(R.id.avail_carts)).setText(String.format("%d",session.state.currentCity.freecarts));
		((ResourceBar)findViewById(R.id.resourceBar)).update(session.state.currentCity);
	}
	public static Intent getIntent(AccountWrap acct, Context context) {
		Intent i = new Intent(context,EnlightenedCityList.class);
		i.putExtras(acct.toBundle());
		return i;
	}
}
