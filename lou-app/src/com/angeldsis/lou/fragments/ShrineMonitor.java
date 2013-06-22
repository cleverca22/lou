package com.angeldsis.lou.fragments;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.angeldsis.lou.FragmentBase;
import com.angeldsis.lou.R;
import com.angeldsis.louapi.RPC;
import com.angeldsis.louapi.data.Coord;
import com.angeldsis.louapi.world.Moongate;
import com.angeldsis.louapi.world.Shrine;
import com.angeldsis.louapi.world.WorldParser;
import com.angeldsis.louapi.world.WorldParser.Cell;
import com.angeldsis.louapi.world.WorldParser.MapItem;

public class ShrineMonitor extends FragmentBase {
	private static final String TAG = "ShrineMonitor";
	private ShrineAdapter mAdapter;
	private ListView list;
	Button start,stop,export;
	TextView shrine_count;
	ArrayList<Shrine> shrines;
	ArrayList<Moongate> moongates;
	Handler handler = new Handler();
	private void getShrines(int worldid) {
		// FIXME, edit to use config
		@Deprecated final Integer[] w86shrines = { 27263141, 28573877, 9175574, 4653636 };
		// TODO: use a the database service
		if (worldid == 86) {
			done(w86shrines);
		}
	}
	private void done(Integer[] shrines) {
		// FIXME, list things better
		mAdapter = new ShrineAdapter(parent,shrines);
		list.setAdapter(mAdapter);
	}
	@Override public void session_ready() {
		parent.session.rpc.setWorldEnabled(true);
		WorldParser p = parent.session.rpc.worldParser;
		Coord c = parent.session.state.currentCity.location;
		int col = c.x/32;
		int row = c.y/32;
		p.mincol = col - 1;
		p.maxcol = col + 1;
		p.minrow = row - 1;
		p.maxrow = row + 1;
		getShrines(parent.acct.worldid);
	}
	private class ShrineAdapter extends ArrayAdapter<Integer> {
		public ShrineAdapter(Context context, Integer[] shrines) {
			super(context, 0, shrines);
		}
		@Override public View getView(int position,View convertView,ViewGroup root) {
			TextView tv;
			if (convertView == null) {
				convertView = tv = new TextView(root.getContext());
			} else tv = (TextView) convertView;
			
			Coord loc = Coord.fromCityId(getItem(position));
			tv.setText(""+loc.format());
			return convertView;
		}
	}
	@Override public void onStop() {
		if (parent.session != null) parent.session.rpc.setWorldEnabled(false);
		super.onStop();
	}
	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.shrine_monitor, container, false);
		ViewGroup vg = (ViewGroup) v;
		list = (ListView) vg.findViewById(R.id.shrines);
		start = (Button) vg.findViewById(R.id.start);
		start.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				RPC rpc = parent.session.rpc;
				rpc.setWorldEnabled(true);
				WorldParser p = rpc.worldParser;
				p.mincol = 0;
				p.maxcol = 19;
				
				p.minrow = 0;
				p.maxrow = 19;

				rpc.worldParser.enable();
				start.setEnabled(false);
				stop.setEnabled(true);
				export.setEnabled(false);
			}
		});
		stop = (Button) vg.findViewById(R.id.stop);
		stop.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				parent.session.rpc.setWorldEnabled(false);
				stop.setEnabled(false);
				start.setEnabled(true);
				export.setEnabled(true);
			}
		});
		stop.setEnabled(false);
		export = (Button) vg.findViewById(R.id.export);
		export.setEnabled(false);
		export.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				handler.post(new Runnable() {
					@Override public void run() {
						try {
							File external = Environment.getExternalStorageDirectory();
							File data = new File(external,String.format("shrines_w%02d.txt", parent.acct.worldid));
							FileOutputStream fos = new FileOutputStream(data);
							BufferedOutputStream bos = new BufferedOutputStream(fos);
							PrintStream os = new PrintStream(bos);
							os.println("cords  cont mgdist type");
							for (Shrine s : shrines) {
								//Moongate nearest;
								double distance = 999;
								for (Moongate mg : moongates) {
									double dist2 = mg.location.distance(s.location);
									if (dist2 < distance) {
										distance = dist2;
										//nearest = mg;
									}
								}
								os.println(String.format("%7s %s %6.2f %s",s.location.format(),s.location.getContinent(),distance,Shrine.types[s.type]));
							}
							os.println("moongates");
							for (Moongate mg : moongates) {
								os.println(String.format("%7s %s %d %s",mg.location.format(),mg.location.getContinent(),mg.state,parent.session.state.stepToString(mg.activationStep)));
							}
							os.close();
							fos.close();
							Log.v(TAG,"done");
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}
		});
		shrine_count = (TextView) vg.findViewById(R.id.shrine_count);
		return v;
	}
	public void cellUpdated(Cell c) {
		shrines = new ArrayList<Shrine>();
		moongates = new ArrayList<Moongate>();
		int cells = 0;
		// FIXME, only scan this cell, not all
		WorldParser wp = parent.session.rpc.worldParser;
		for (Cell cell : wp.cells) {
			if (cell == null) continue;
			cells++;
			for (MapItem mi : cell.objects) {
				if (mi == null) continue;
				if (mi instanceof Shrine) {
					Shrine shrine = (Shrine) mi;
					shrines.add(shrine);
				}
				if (mi instanceof Moongate) {
					Moongate mg = (Moongate)mi;
					moongates.add(mg);
				}
			}
		}
		Log.v(TAG,String.format("cellUpdated, found %d shrines in %d cells",shrines.size(),cells));
		shrine_count.setText(String.format("shrines found: %d, moongates: %d", shrines.size(),moongates.size()));
	}
}
