package com.angeldsis.lou;

import java.util.Iterator;

import com.angeldsis.lou.city.SelectCity;
import com.angeldsis.louapi.LouState.City;
import com.angeldsis.louapi.LouState.Trade;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class CityCore extends FragmentBase {
	private static final String TAG = "CityCore";
	TradeAdapter tradeAdapter;
	@Override public void onCreate(Bundle sis) {
		super.onCreate(sis);
		tradeAdapter = new TradeAdapter();
	}
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.city_core, container,false);
		ViewGroup vg = (ViewGroup) v;
		ListView lv = (ListView) vg.findViewById(R.id.trades);
		lv.setAdapter(tradeAdapter);
		return v;
	}
	@Override public void session_ready() {
		SelectCity sc = (SelectCity) getView().findViewById(R.id.selectCity);
		sc.setMode(SelectCity.ChangeCurrentCity);
		sc.session_ready(parent.session.rpc.state, parent);
	}
	public void gotCityData() {
		City c = parent.session.state.currentCity;
		// TODO, show trades out
		if (c.trade_in != null) {
			Log.v(TAG,"trade in size: "+c.trade_in.size());
			Trade[] trades = new Trade[c.trade_in.size()];
			Iterator<Trade> i = c.trade_in.iterator();
			int x = 0;
			// TODO, recreate in game sorting
			while (i.hasNext()) trades[x++] = i.next();
			tradeAdapter.setData(trades);
		}
	}
	// TODO: make it a stand-alone class for use in other areas
	class TradeAdapter extends BaseAdapter {
		Trade[] data;
		@Override public int getCount() {
			if (data == null) return 0;
			return data.length;
		}
		public void setData(Trade[] trades) {
			data = trades;
			notifyDataSetChanged();
		}
		@Override public Object getItem(int position) {
			return data[position];
		}
		@Override public long getItemId(int position) {
			return data[position].id;
		}
		@Override public View getView(int position, View convertView, ViewGroup root) {
			if (convertView == null) {
				convertView = CityCore.this.getActivity().getLayoutInflater().inflate(R.layout.trade_row, root, false);
			}
			return convertView;
		}
	};
}
