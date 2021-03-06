package com.angeldsis.louapi;

import java.util.TreeMap;

import org.json2.JSONArray;
import org.json2.JSONException;
import org.json2.JSONObject;

import com.angeldsis.louapi.data.Coord;

public class EnlightenedCities {
	public static final int undefined = 0;
	public static final int doNotBuild = 1;
	public static final int low = 2;
	public static final int medium = 3;
	public static final int high = 4;
	public static class EnlightenedCity implements Comparable<Integer> {
		public int id;
		public Coord location;
		public static final long[] res_needed = { 10000000, 30000000, 60000000, 100000000, 250000000,
			350000000, 500000000, 800000000 ,1000000000, 1500000000 };
		// dedicated wood/stone
		public long wood,stone;
		public long incoming_wood,incoming_stone;
		public Resource[] normal;
		public int shrine_type,palace_level,priority,defaultPriority;
		public String comment;
		String name;
		public int endstep;
		Player player;
		public boolean known = false;
		public EnlightenedCity(int id2, JSONObject y, RPC rpc) throws JSONException {
			id = id2;
			location = Coord.fromCityId(id);
			normal = new Resource[2];
			normal[0] = new Resource(rpc.state,1);
			normal[1] = new Resource(rpc.state,2);
			player = rpc.world.getPlayer(y.getInt("pi"),y.getString("pn"));
			update(y);
		}
		public void update(JSONObject y) throws JSONException {
			wood = y.getLong("w");
			stone = y.getLong("s");
			comment = y.getString("c");
			name = y.getString("n");
			endstep = y.getInt("et");
			shrine_type = y.getInt("st");
			palace_level = y.getInt("pl");
			int x;
			for (x=0; x<2; x++) {
				JSONObject in = y.getJSONArray("r").getJSONObject(x);
				int type = in.getInt("i");
				Resource r = normal[type-1];
				r.delta = in.getDouble("d");
				r.max = in.getInt("m");
				r.base = in.getDouble("b");
				r.step = in.getLong("s");
				if (type == 1) incoming_wood = in.getLong("p");
				else incoming_stone = in.getLong("s");
			}
			priority = y.getInt("p");
			defaultPriority = y.getInt("pd");
		}
		@Override public int compareTo(Integer arg0) {
			if (this.id < arg0) return -1;
			if (this.id > arg0)return 1; 
			return 0;
		}
		public int getResourceCount(LouState state,int id) {
			int stepsPassed = (int) (state.getServerStep() - normal[id].step);
			double delta = normal[id].delta;
			double newVal = stepsPassed * delta + normal[id].base;
			if (newVal > normal[id].max) return normal[id].max;
			return (int) newVal;
		}
	}
	private static final String TAG = "EnlightenedCities";
	private boolean initial;
	public TreeMap<Integer,EnlightenedCity> data;
	EnlightenedCities() {
		initial = true;
	}
	public String getRequestDetails() {
		if (initial) {
			return "a";
		}
		return "";
	}
	public void parse(JSONObject p, final RPC rpc) throws JSONException {
		JSONObject D = p.getJSONObject("D");
		//Log.v(TAG,D.toString());
		if (!D.isNull("f")) {
			Log.v(TAG,"Flush!!!");
			initial = true;
			int flush = D.optInt("f");
			if (flush == 1) this.data = new TreeMap<Integer,EnlightenedCity>();
		}
		JSONArray c = D.optJSONArray("c");
		if (c != null) {
			int x;
			for (x=0; x<c.length(); x++) {
				JSONObject y = c.getJSONObject(x);
				int id = y.getInt("i");
				EnlightenedCity result = data.get(id);
				if (y.has("n") == false) {
					if (result != null) {
						data.remove(result);
					}
					continue;
				}
				if (result == null) {
					//Log.v(TAG,"making new obj "+y.toString());
					result = new EnlightenedCity(id,y,rpc);
					if (initial) result.known = true;
					data.put(id,result);
				} else {
					result.update(y);
				}
			}
		}
		initial = false;
		rpc.runOnUiThread(new Runnable() {
			@Override public void run() {
				rpc.onEnlightenedCityChanged();
			}});
	}
}
