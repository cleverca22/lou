package com.angeldsis.lou.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import com.angeldsis.lou.MyTableRow;
import com.angeldsis.lou.R;
import com.angeldsis.louapi.data.Coord;
import com.angeldsis.louapi.data.UnitCount;
import com.angeldsis.louapi.world.Dungeon;
import com.angeldsis.louapi.world.WorldParser.Cell;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DungeonList extends WorldUser implements OnItemClickListener {
	MyTableRow.LayoutParameters params;
	private static final String TAG = "DungeonList";
	DungeonListAdapter adapter;
	Coord currentCity;
	ListView list;
	TextView zerks;
	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		setContentView(R.layout.dungeon_list);
		params = new MyTableRow.LayoutParameters();
		list = (ListView) findViewById(R.id.list);
		list.setOnItemClickListener(this);
		zerks = (TextView) findViewById(R.id.zerks);
	}
	@Override
	public void cellUpdated(Cell cIN) {
		currentCity = session.rpc.state.currentCity.location;
		// FIXME, only scan what changed
		ArrayList<Dungeon> allDungeons = new ArrayList<Dungeon>();
		int i,j;
		for (i=0; i < session.rpc.worldParser.cells.length; i++) {
			Cell c = session.rpc.worldParser.cells[i];
			if (c == null) continue;
			for (j=0; j<c.dungeons.length; j++) {
				Dungeon d = c.dungeons[j];
				if (d == null) continue;
				if (d.state == false) continue;
				allDungeons.add(d);
			}
		}
		Log.v(TAG,"found "+allDungeons.size()+" dungeons");
		Dungeon[] list2 = new Dungeon[allDungeons.size()];
		allDungeons.toArray(list2);
		Arrays.sort(list2,new Comparator<Dungeon>() {
			@Override
			public int compare(Dungeon x, Dungeon y) {
				double dist1 = x.location.distance(currentCity);
				double dist2 = y.location.distance(currentCity);
				if (dist1 < dist2) return -1;
				else if (dist1 > dist2) return 1;
				return 0;
			}});
		adapter = new DungeonListAdapter(this,list2);
		list.setAdapter(adapter);
	}
	class DungeonListAdapter extends ArrayAdapter<Dungeon> {
		public DungeonListAdapter(DungeonList dungeonList, Dungeon[] list2) {
			super(dungeonList,0,list2);
		}
		@Override public View getView(int position,View convertView,ViewGroup root) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = DungeonList.this.getLayoutInflater().inflate(R.layout.dungeon_row, root,false);
				MyTableRow row = (MyTableRow) convertView;
				row.bind(params);
				holder = new ViewHolder();
				holder.type = (TextView) row.findViewById(R.id.type);
				holder.level = (TextView) row.findViewById(R.id.level);
				holder.distance = (TextView) row.findViewById(R.id.distance);
				holder.maxloot = (TextView) row.findViewById(R.id.maxloot);
				convertView.setTag(holder);
			} else holder = (ViewHolder) convertView.getTag();
			
			Dungeon obj = getItem(position);
			holder.type.setText(obj.getType());
			holder.level.setText("("+obj.level+") "+obj.progress+"%");
			holder.distance.setText(String.format("%.2f state:%b",obj.location.distance(currentCity),obj.state));
			holder.maxloot.setText(""+obj.getloot());
			return convertView;
		}
		@Override public long getItemId(int position) {
			return getItem(position).location.toCityId();
		}
		@Override public boolean hasStableIds() {
			return true;
		}
	}
	private static class ViewHolder {
		public TextView maxloot;
		public TextView distance;
		public TextView level;
		public TextView type;
	}
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		Dungeon d = adapter.getItem(position);
		Intent i = new Intent(this,SendAttack.class);
		i.putExtras(acct.toBundle());
		i.putExtra("dungeon", d.location.toCityId());
		i.putExtra("maxloot", d.getloot());
		startActivity(i);
	}
	@Override public void session_ready() {
		super.session_ready();
		if (session.rpc.state.currentCity.units != null) {
			UnitCount uc = session.rpc.state.currentCity.units[6];
			if (uc != null) {
				zerks.setText(""+uc.c);
				return;
			}
		}
		zerks.setText("0?");
	}
}
