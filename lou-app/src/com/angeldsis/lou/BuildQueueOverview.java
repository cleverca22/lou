package com.angeldsis.lou;

import java.util.Arrays;
import java.util.Comparator;

import com.angeldsis.louapi.BuildQueueParser;
import com.angeldsis.louapi.BuildQueueParser.BuildQueueData;
import com.angeldsis.louapi.LouState;
import com.angeldsis.louapi.LouState.City;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

// FIXME, requeries the server on rotation
public class BuildQueueOverview extends SessionUser {
	BQOAdapter adapter;
	CheckBox showFull;
	private static final String TAG = "BuildQueueOverview";
	MyTableRow.LayoutParameters grid = new MyTableRow.LayoutParameters();
	@Override public void onCreate(Bundle sis) {
		super.onCreate(sis);
		adapter = new BQOAdapter();
		setContentView(R.layout.build_queue_overview);
		ListView v = (ListView) findViewById(R.id.list);
		v.setAdapter(adapter);
		showFull = (CheckBox) findViewById(R.id.showFull);
		showFull.setChecked(true);
	}
	@Override public void session_ready() {
		session.rpc.setBuildQueueWatching(true);
		adapter.update(session.rpc.buildQueueParser);
	}
	@Override public void onStop() {
		session.rpc.setBuildQueueWatching(false);
		super.onStop();
		adapter.data = null;
		adapter.notifyDataSetChanged();
	}
	@Override public void onBuildQueueUpdate() {
		adapter.update(session.rpc.buildQueueParser);
	}
	public class BQOAdapter extends BaseAdapter {
		BuildQueueData[] data;
		Comparator<BuildQueueData> order = new SortByQueueSize();
		@Override public int getCount() {
			if (data == null) return 0;
			return data.length;
		}
		public void update(BuildQueueParser buildQueueParser) {
			data = new BuildQueueData[buildQueueParser.data2.size()];
			int i;
			for (i=0; i<buildQueueParser.data2.size(); i++) {
				data[i] = buildQueueParser.data2.get(i);
			}
			Arrays.sort(data, order);
			this.notifyDataSetChanged();
		}
		@Override public BuildQueueData getItem(int position) {
			return data[position];
		}
		@Override public long getItemId(int position) {
			return data[position].id;
		}
		@Override public View getView(int position, View row, ViewGroup parent) {
			final ViewHolder holder;
			if (row == null) {
				row = getLayoutInflater().inflate(R.layout.build_queue_overview_row, parent,false);
				ViewGroup row2 = (ViewGroup) row;
				MyTableRow tablerow = (MyTableRow) row2.findViewById(R.id.row);
				tablerow.bind(grid);
				holder = new ViewHolder();
				row.setTag(holder);
				holder.payall = (Button) row.findViewById(R.id.payAll);
				holder.payall.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						session.rpc.BuildingQueuePayAll(holder.root.id);
					}});
				holder.buildone = (Button) row.findViewById(R.id.buildOne);
				holder.buildone.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						session.rpc.QueueMinisterBuildOrder(holder.root.id);
					}});
				holder.buildall = (Button) row.findViewById(R.id.buildAll);
				holder.buildall.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						session.rpc.BuildingQueueFill(holder.root.id);
					}});
				holder.debug = (TextView) row.findViewById(R.id.debug);
				holder.wood = (TextView) row.findViewById(R.id.wood);
				holder.stone = (TextView) row.findViewById(R.id.stone);
				holder.b = Bitmap.createBitmap(5*16,32,Bitmap.Config.ARGB_8888);
			} else holder = (ViewHolder) row.getTag();
			BuildQueueData obj = getItem(position);
			LouState state = session.rpc.state;
			City c = state.findCityById(obj.id);
			//int state = obj.__LK();
			holder.root = obj;
			TextView tv = (TextView) row.findViewById(R.id.cityname);
			tv.setText(c.name);
			TextView qs = (TextView) row.findViewById(R.id.queueSize);
			qs.setText(""+obj.getQueueSize());
			
			holder.buildone.setEnabled((obj.auto & 1) == 1);
			holder.buildall.setEnabled((obj.auto & 1) == 1);
			
			holder.debug.setText(""+obj.auto);
			
			holder.payall.setEnabled(!obj.allpaid);
			
			holder.wood.setText(Utils.NumberFormat(c.resources[0].getCurrent(state)));
			holder.stone.setText(Utils.NumberFormat(c.resources[1].getCurrent(state)));
			
			ImageView bar = (ImageView) row.findViewById(R.id.bar);
			int paid=obj.paid,i,unpaid=obj.unpaid;
			holder.b.eraseColor(Color.RED);
			holder.b.eraseColor(Color.BLUE);
			if (obj.queue != null) {
				int[] pixels = new int[paid*5*32];
				for (i=0; i < paid*5*32; i++) pixels[i] = Color.RED;
				int width = paid*5;
				holder.b.setPixels(pixels, 0, width, 0, 0, width, 32);
				pixels = new int[unpaid*5*32];
				for (i=0; i < unpaid*5*32; i++) pixels[i] = Color.BLUE;
				width = unpaid*5;
				holder.b.setPixels(pixels, 0, width, paid*5, 0, width, 32);
			}
			bar.setImageBitmap(holder.b);
			return row;
		}
	}
	private static class ViewHolder {
		public Bitmap b;
		public Button payall;
		public TextView debug,wood,stone;
		public Button buildall;
		public Button buildone;
		public BuildQueueData root;
	}
	private class SortByQueueSize implements Comparator<BuildQueueData> {
		@Override public int compare(BuildQueueData a, BuildQueueData b) {
			if (a.getQueueSize() < b.getQueueSize()) return 1;
			else if (a.getQueueSize() > b.getQueueSize()) return -1;
			
			if (a.allpaid && !b.allpaid) return -1;
			if (!a.allpaid && b.allpaid) return 1;
			
			if (a.paid > b.paid) return -1;
			if (a.paid < b.paid) return 1; 
			return 0;
		}
	}
	public void updateShowFull(View v) {
		
	}
}
