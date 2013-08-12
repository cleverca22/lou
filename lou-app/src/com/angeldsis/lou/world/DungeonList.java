package com.angeldsis.lou.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import com.angeldsis.lou.MyTableRow;
import com.angeldsis.lou.R;
import com.angeldsis.louapi.data.Coord;
import com.angeldsis.louapi.data.UnitCount;
import com.angeldsis.louapi.world.Boss;
import com.angeldsis.louapi.world.CityMapping;
import com.angeldsis.louapi.world.Dungeon;
import com.angeldsis.louapi.world.LawlessCity;
import com.angeldsis.louapi.world.WorldParser.Cell;
import com.angeldsis.louapi.world.WorldParser.MapItem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class DungeonList extends WorldUser implements OnItemClickListener, OnItemSelectedListener {
	int lootCapacity;
	MyTableRow.LayoutParameters params;
	private static final String TAG = "DungeonList";
	DungeonListAdapter adapter;
	Coord currentCity;
	ListView list;
	TextView zerks;
	Filter dungeonFilter = new Filter() {
		@Override public boolean checkItem(MapItem i) {
			if (i instanceof Dungeon) {
				Dungeon d = (Dungeon) i;
				return d.state; // return its active state
			} else return false;
		}
	};
	Filter lawlessFilter = new Filter() {
		@Override public boolean checkItem(MapItem i) {
			if (i instanceof LawlessCity) {
				LawlessCity lc = (LawlessCity) i;
				return lc.canSettle();
			} else return false;
		}
	};
	Filter cityFilter = new Filter() {
		@Override public boolean checkItem(MapItem i) {
			if (i instanceof CityMapping) {
				return true;
			} else return false;
		}
	};
	Filter bossFilter = new Filter() {
		@Override public boolean checkItem(MapItem i) {
			if (i instanceof Boss) return true;
			return false;
		}
	};
	Filter filter = dungeonFilter;
	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		params = new MyTableRow.LayoutParameters();
	}
	@Override public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle sis) {
		ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.dungeon_list, root, false);
		list = (ListView) vg.findViewById(R.id.list);
		list.setOnItemClickListener(this);
		adapter = new DungeonListAdapter();
		list.setAdapter(adapter);
		zerks = (TextView) vg.findViewById(R.id.zerks);
		Spinner filter = (Spinner) vg.findViewById(R.id.filter);
		filter.setOnItemSelectedListener(this);
		return vg;
	}
	@Override public void cellUpdated(Cell cIN) {
		currentCity = parent.session.rpc.state.currentCity.location;
		// FIXME, only scan what changed
		ArrayList<MapItem> allItems = new ArrayList<MapItem>();
		int i,j;
		for (i=0; i < parent.session.rpc.worldParser.cells.length; i++) {
			Cell c = parent.session.rpc.worldParser.cells[i];
			if (c == null) continue;
			for (j=0; j<c.objects.length; j++) {
				MapItem item = c.objects[j];
				if (item == null) continue;
				if (filter.checkItem(item)) allItems.add(item);
				//if (d.type != 4) continue; // only mountains
			}
		}
		Log.v(TAG,"found "+allItems.size()+" items");
		MapItem[] list2 = new MapItem[allItems.size()];
		allItems.toArray(list2);
		if (filter == dungeonFilter) {
			final float speed = parent.session.state.getInfantrySpeed();
			Arrays.sort(list2,new Comparator<MapItem>() {
				@Override
				public int compare(MapItem x, MapItem y) {
					Dungeon x1 = (Dungeon) x;
					Dungeon y1 = (Dungeon) y;
					double lootrate1 = x1.lootRate(speed,lootCapacity);
					double lootrate2 = y1.lootRate(speed,lootCapacity);
					if (lootrate1 < lootrate2) return 1;
					else if (lootrate1 > lootrate2) return -1;
					return 0;
				}});
		} else {
			Arrays.sort(list2,new Comparator<MapItem>() {
				@Override
				public int compare(MapItem x, MapItem y) {
					double dist1 = x.location.distance(currentCity);
					double dist2 = y.location.distance(currentCity);
					if (dist1 < dist2) return -1;
					else if (dist1 > dist2) return 1;
					return 0;
				}});
		}
		adapter.update(list2);
	}
	class DungeonListAdapter extends BaseAdapter {
		MapItem[] list;
		public DungeonListAdapter() {
			super();
			list = new MapItem[0];
		}
		public void update(MapItem[] list2) {
			list = list2;
			notifyDataSetChanged();
		}
		@Override public View getView(int position,View convertView,ViewGroup root) {
			DungeonViewHolder holder;
			if (convertView == null) {
				// FIXME, use a different row and holder for each type
				convertView = parent.getLayoutInflater().inflate(R.layout.dungeon_row, root,false);
				MyTableRow row = (MyTableRow) convertView;
				row.bind(params);
				holder = new DungeonViewHolder();
				holder.type = (TextView) row.findViewById(R.id.type);
				holder.level = (TextView) row.findViewById(R.id.level);
				holder.distance = (TextView) row.findViewById(R.id.distance);
				holder.maxloot = (TextView) row.findViewById(R.id.maxloot);
				holder.lootRate = (TextView) row.findViewById(R.id.lootRate);
				convertView.setTag(holder);
			} else holder = (DungeonViewHolder) convertView.getTag();
			
			MapItem item = getItem(position);
			float speed = parent.session.state.getInfantrySpeed();
			double time = speed * item.location.distance(currentCity);
			if (item instanceof Dungeon) {
				Dungeon obj = (Dungeon)item;
				holder.type.setText(obj.getType());
				holder.level.setText("("+obj.level+") "+obj.progress+"%");
				holder.maxloot.setText(""+obj.getloot());
				int lootrate = (int) obj.lootRate(speed,lootCapacity);
				holder.lootRate.setText(lootrate+"/h");
			}
			if (item instanceof Boss) {
				Boss boss = (Boss)item;
				holder.type.setText(boss.getType());
				holder.level.setText(""+boss.bossLevel);
				holder.maxloot.setText(""+boss.getZerks());
				holder.lootRate.setText(boss.location.format());
			}
			if (item instanceof LawlessCity) {
				LawlessCity obj = (LawlessCity) item;
				holder.type.setText(""+obj.points);
			}
			if (item instanceof CityMapping) {
				CityMapping cm = (CityMapping) item;
				holder.type.setText(cm.name);
			}
			holder.distance.setText(String.format("%dmins",(int)(time/60)));
			return convertView;
		}
		@Override public int getItemViewType(int position) {
			MapItem i = getItem(position);
			if (i instanceof Dungeon) return 0;
			if (i instanceof LawlessCity) return 1;
			return 0;
		}
		@Override public int getViewTypeCount() {
			return 2;
		}
		@Override public long getItemId(int position) {
			return getItem(position).location.toCityId();
		}
		@Override public boolean hasStableIds() {
			return true;
		}
		@Override public int getCount() {
			return list.length;
		}
		@Override public MapItem getItem(int position) {
			return list[position];
		}
	}
	private static class BaseHolder {
		public TextView distance;
	}
	private static class DungeonViewHolder extends BaseHolder {
		public TextView lootRate;
		public TextView maxloot;
		public TextView level;
		public TextView type;
	}
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		MapItem item = adapter.getItem(position);
		if (item instanceof Dungeon) {
			Dungeon d = (Dungeon) item;
			Intent i = new Intent(parent,SendAttack.class);
			i.putExtras(parent.acct.toBundle());
			i.putExtra("target", d.location.toCityId());
			i.putExtra("maxloot", d.getloot());
			startActivity(i);
		}
		if (item instanceof Boss) {
			Boss b = (Boss) item;
			Intent i = new Intent(parent,SendAttack.class);
			i.putExtras(parent.acct.toBundle());
			i.putExtra("target", b.location.toCityId());
			i.putExtra("zerks", b.getZerks());
			startActivity(i);
		}
		// FIXME, allow settling lawless, plundering cities, and assaulting castles
	}
	@Override public void session_ready() {
		super.session_ready();
		onCityChanged();
	}
	@Override public void onCityChanged() {
		super.resetFocus();
		lootCapacity = 0;
		if (parent.session.rpc.state.currentCity.units != null) {
			UnitCount uc = parent.session.rpc.state.currentCity.units[6];
			if (uc != null) {
				zerks.setText(""+uc.c);
				lootCapacity = uc.c * 10;
				return;
			}
		}
		zerks.setText("0?");
	}
	@Override public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// NOTE, this is sensitive to the order of items in the xml list
		switch (arg2) {
		case 0:
			filter = dungeonFilter;
			break;
		case 1:
			filter = lawlessFilter;
			break;
		case 2:
			filter = cityFilter;
			break;
		case 3:
			filter = bossFilter;
		}
		cellUpdated(null);
	}
	interface Filter {
		boolean checkItem(MapItem i);
	}
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
}
