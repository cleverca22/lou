package com.angeldsis.louapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json2.JSONArray;
import org.json2.JSONException;
import org.json2.JSONObject;

import com.angeldsis.louapi.LouState.City;
import com.angeldsis.louapi.data.Coord;

public class BuildQueueParser {
	private static final String TAG = "BuildQueueParser";
	boolean fullRefresh = true;
	public Map<Integer,BuildQueueData> data1 = new HashMap<Integer,BuildQueueData>();
	public ArrayList<BuildQueueData> data2 = new ArrayList<BuildQueueData>();
	private LouState state;
	public BuildQueueParser(LouState state) {
		this.state = state;
	}
	public String getRequestDetails() {
		if (fullRefresh) return "a";
		return "";
	}
	public void parse(JSONArray jsonArray, final RPC rpc) throws JSONException {
		fullRefresh = false;
		int i;
		for (i=0; i<jsonArray.length(); i++) {
			JSONObject x = jsonArray.getJSONObject(i);
			BuildQueueData one;
			int id = x.getInt("i");
			if (data1.containsKey(id)) {
				one = data1.get(id);
			} else {
				one = new BuildQueueData(id);
				data1.put(id, one);
				data2.add(one);
			}
			one.update(x);
			// temp fix to improve debug
			x.remove("q");
			
			//if (id == 16056456) Log.v(TAG,"packet: "+x.toString(1));
			one.dump();
		}
		rpc.runOnUiThread(new Runnable() {
			public void run() {
				rpc.onBuildQueueUpdate();
			}
		});
	}
	public class BuildQueueData {
		JSONArray unpaidTraining;
		JSONArray trades; // if e is less then ??, total up c
		JSONArray incomingRaids; // similar
		int end;
		int start;
		public int paid,unpaid;
		int b; // no clue
		public QueueRow[] queue;
		public boolean allpaid;
		public int auto; // bitfield, autoBuild = (dy.a & (1 << 1)) != 0
		// autoDef = (dy.a & (1 << 2)) != 0;
		// fill = (dy.a & (1 << 0)) != 0;
		int points;
		boolean k; // k defines which error msg explains why the queue is halted
		boolean warnings; // gets set when parsing queue
		public int id;
		public BuildQueueData(int id2) {
			id = id2;
		}
		public void dump() {
			Log.v(TAG,String.format("%s %d %d %b",Coord.fromCityId(id),b,queue == null ? -1 : queue.length,warnings));
		}
		public void update(JSONObject x) throws JSONException {
			unpaidTraining = x.optJSONArray("u");
			trades = x.optJSONArray("t");
			end = x.getInt("e");
			start = x.getInt("s");
			b = x.getInt("b");
			
			JSONArray resources = x.getJSONArray("r");
			City c = BuildQueueParser.this.state.findCityById(id);
			int i;
			for (i=0; i<resources.length(); i++) {
				JSONObject o = resources.getJSONObject(i);
				double d = o.getDouble("d");
				double b = o.getDouble("b");
				int s = o.getInt("s");
				int m = o.getInt("m");
				int type = o.getInt("i");
				Resource r = c.resources[type-1];
				r.set(d, b, m, s, BuildQueueParser.this.state);
			}
			
			JSONArray queueIN = x.optJSONArray("q");
			if (queueIN != null) {
				allpaid = true;
				queue = new QueueRow[queueIN.length()];
				paid = unpaid = 0;
				for (i=0; i<queueIN.length(); i++) {
					JSONObject b = queueIN.getJSONObject(i);
					Log.v(TAG,"queue "+i+" "+b.toString());
					QueueRow t = new QueueRow();
					t.paid = b.getBoolean("p");
					t.w = b.getInt("w");
					t.level = b.getInt("l");
					t.t = b.getInt("t");
					t.b = b.getInt("b");
					t.i = b.getInt("i");
					if (t.paid == false) {
						allpaid = false;
						unpaid++;
					} else paid++;
					queue[i] = t;
				}
			} else allpaid = true;
			auto = x.getInt("a");
			points = x.getInt("p");
			incomingRaids = x.optJSONArray("l");
			k = x.getBoolean("k");
		}
		public int getQueueSize() {
			if (queue == null) return 0;
			return queue.length;
		}
		public int __LK() {
			// explains why the build mini cant queue things
			/*var gR = webfrontend.data.Player.getInstance();
			if (gR.getTitle() < webfrontend.base.GameObjects.ePlayerTitle.Marquess) {
				return 1;
			}*/
			boolean autoBuildNormal = (auto & (1 << 1)) != 0;
			boolean autoBuildDef = (auto & (1 << 2)) != 0;
			boolean bitthree = (auto & (1 << 3)) != 0;
			boolean bitfour = (auto & (1 << 4)) != 0;
			boolean bitfive = (auto & (1 << 5)) != 0;
			if (!autoBuildNormal && !autoBuildDef) {
				return 2;
			} else if (autoBuildNormal && bitthree && autoBuildDef && bitfive) {
				return 3;
			} else if (autoBuildNormal && bitthree && !autoBuildDef) {
				return 4;
			} else if (autoBuildDef && bitfive && !autoBuildNormal) {
				return 5;
			}
			if (bitfour) {
				return 6;
			}
			return 0;
		}
	}
	public static class QueueRow {
		public int w,level,t,b,i;
		public boolean paid;
	}
}
