package com.angeldsis.lou;

import java.util.Iterator;

import com.angeldsis.lou.city.SelectCity;
import com.angeldsis.lou.fragments.MinisterConfig;
import com.angeldsis.lou.fragments.PurifyResources;
import com.angeldsis.lou.fragments.ResourceBar2;
import com.angeldsis.louapi.LouState.City;
import com.angeldsis.louapi.LouState.Trade;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class CityCore extends FragmentBase implements OnClickListener {
	private static final String TAG = "CityCore";
	TradeAdapter tradeAdapter;
	@Override public void onCreate(Bundle sis) {
		super.onCreate(sis);
		Log.v(TAG,"onCreate");
		tradeAdapter = new TradeAdapter();
	}
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.v(TAG,"onCreateView");
		View v = inflater.inflate(R.layout.city_core, container,false);
		ViewGroup vg = (ViewGroup) v;
		ListView lv = (ListView) vg.findViewById(R.id.trades);
		lv.setAdapter(tradeAdapter);
		
		Button b = (Button) vg.findViewById(R.id.purify);
		b.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				Intent i = new Intent(getActivity(),SingleFragment.class);
				i.putExtras(parent.acct.toBundle());
				i.putExtra("fragment", PurifyResources.class);
				getActivity().startActivity(i);
			}});
		b = (Button) vg.findViewById(R.id.ministers);
		b.setOnClickListener(this);
		if (savedInstanceState == null) {
			FragmentTransaction ft = getChildFragmentManager().beginTransaction();
			ft.replace(R.id.resource_bar, new ResourceBar2());
			SelectCity selectCity = new SelectCity();
			selectCity.setMode(SelectCity.ChangeCurrentCity);
			ft.replace(R.id.selectCity, selectCity);
			ft.commit();
		}
		return v;
	}
	@Override public void session_ready() {
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
	// maybe just make the entire list a fragment
	private class ViewHolder {
		ImageView state,icon2;
		TextView target,infoText;
	}
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
		@Override public Trade getItem(int position) {
			return data[position];
		}
		@Override public long getItemId(int position) {
			return data[position].id;
		}
		@Override public View getView(int position, View convertView, ViewGroup root) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = CityCore.this.getActivity().getLayoutInflater().inflate(R.layout.trade_row, root, false);
				ViewGroup vg = (ViewGroup) convertView;
				holder = new ViewHolder();
				holder.state = (ImageView) vg.findViewById(R.id.state);
				holder.icon2 = (ImageView) vg.findViewById(R.id.icon2);
				holder.target = (TextView) vg.findViewById(R.id.target);
				holder.infoText = (TextView) vg.findViewById(R.id.infoText);
				convertView.setTag(holder);
			} else holder = (ViewHolder) convertView.getTag();
			
			Trade t = getItem(position);
			switch (t.state) {
			case Trade.Return:
				holder.state.setImageResource(R.drawable.icon_trade_info_mnstr_return);
				break;
			case Trade.Working:
				switch (t.type) {
				case Trade.TradeMinisterRequested:
					holder.state.setImageResource(R.drawable.icon_trade_info_minister);
					break;
				default:
					holder.state.setImageResource(R.drawable.building_casern);
				}
				break;
			case Trade.ReturnFromCancel:
			case Trade.WorkingPalaceSupport:
				holder.state.setImageResource(R.drawable.building_casern);
			}
			if (t.state == Trade.Return) {
				holder.icon2.setImageResource(R.drawable.icon_ship_city_info);
				holder.target.setText(t.cityName);
			} else {
				// FIXME, display resources
				holder.infoText.setText("Arrives:");
				holder.target.setText(parent.session.state.stepToString(t.end));
			}
			// FIXME, show the right artifact and make it work
			return convertView;
		}
	}
	@Override public void onClick(View view) {
		switch (view.getId()) {
		case R.id.ministers:
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.replace(R.id.main_frame, new MinisterConfig());
			ft.addToBackStack(null);
			ft.commit();
			break;
		}
	};
}
