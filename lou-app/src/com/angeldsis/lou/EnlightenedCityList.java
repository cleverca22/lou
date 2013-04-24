package com.angeldsis.lou;

import java.util.ArrayList;

import com.angeldsis.lou.city.SendTrade;
import com.angeldsis.louapi.EnlightenedCities.EnlightenedCity;
import com.angeldsis.louapi.data.Coord;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class EnlightenedCityList extends SessionUser {
	private static final String TAG = "EnlightenedCityList";
	MyTableRow.LayoutParameters params;
	CityList adapter;
	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		params = new MyTableRow.LayoutParameters();
		setContentView(R.layout.el_city_list);
	}
	private static class ViewHolder {

		public TextView coord;
		public TextView level;
		public TextView wood;
		public TextView stone;
		public EnlightenedCity city;
	}
	@Override public void session_ready() {
		super.session_ready();
		onEnlightenedCityChanged();
	}
	@Override public void onEnlightenedCityChanged() {
		EnlightenedCity[] data;
		ArrayList<EnlightenedCity> datain = session.rpc.enlightenedCities.data;
		data = new EnlightenedCity[datain.size()];
		datain.toArray(data);
		adapter = new CityList(this,data);
		((ListView)findViewById(R.id.list)).setAdapter(adapter);
	}
	private class CityList extends ArrayAdapter<EnlightenedCity> {
		public CityList(Context context, EnlightenedCity[] objects) {
			super(context, 0, objects);
		}
		public View getView(int position,View convertView,ViewGroup root) {
			final ViewHolder holder;
			if (convertView == null) {
				convertView = EnlightenedCityList.this.getLayoutInflater().inflate(R.layout.el_city_row, root, false);
				MyTableRow row = (MyTableRow) convertView;
				row.bind(params);
				holder = new ViewHolder();
				row.setTag(holder);
				holder.coord = (TextView)row.findViewById(R.id.coord);
				holder.level = (TextView)row.findViewById(R.id.level);
				holder.wood = (TextView)row.findViewById(R.id.wood);
				holder.stone = (TextView)row.findViewById(R.id.stone);
				Button b = (Button) row.findViewById(R.id.button);
				b.setOnClickListener(new OnClickListener() {
					@Override public void onClick(View v) {
						Log.v(TAG,""+holder.city.id);
						Intent i = new Intent(EnlightenedCityList.this,SendTrade.class);
						i.putExtras(acct.toBundle());
						i.putExtra("targetCity", holder.city.id);
						startActivity(i);
					}});
			} else holder = (ViewHolder) convertView.getTag();
			
			holder.city = getItem(position);
			Coord coord = Coord.fromCityId(holder.city.id);
			holder.level.setText(""+holder.city.palace_level);
			holder.coord.setText(coord.getContinent()+" "+coord.format());
			
			int needed = EnlightenedCity.res_needed[holder.city.palace_level];
			int missing_wood = needed - (holder.city.wood + holder.city.normal[0].getCurrent(session.rpc.state) + holder.city.incoming_wood);
			int missing_stone = needed - (holder.city.stone + holder.city.normal[1].getCurrent(session.rpc.state) + holder.city.incoming_stone);
			
			if (missing_wood > 0) holder.wood.setText(Utils.NumberFormat(missing_wood));
			else holder.wood.setText("overfilled "+Utils.NumberFormat(missing_wood * -1));
			
			if (missing_stone > 0) holder.stone.setText(""+Utils.NumberFormat(missing_stone));
			else holder.stone.setText("overfilled "+Utils.NumberFormat(missing_stone * -1));
			return convertView;
		}
	}
}
