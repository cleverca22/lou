package com.angeldsis.louapi;

import java.util.Date;
import java.util.TreeMap;

import org.json2.JSONArray;
import org.json2.JSONException;
import org.json2.JSONObject;

import com.angeldsis.louapi.LouState.City;

public class FoodWarningParser {
	private static final String TAG = "FoodWarningParser";
	private boolean initial = true;
	public TreeMap<Integer,City> warnings;
	public FoodWarningParser() {
		warnings = new TreeMap<Integer,City>();
	}
	public String getRequestDetails() {
		if (initial) {
			initial = false;
			return "a";
		}
		return "";
	}
	public void parse(JSONObject p, final RPC rpc) throws JSONException {
		JSONArray D = p.optJSONArray("D");
		int i;
		for (i=0; i<D.length(); i++) {
			// deletes a warning? {"d":1,"i":23724372}
			// {"r":{"d":-8.93749999999982,"s":14389674,"b":330863.559027776,"m":1975000},"i":19333478}
			JSONObject data = D.optJSONObject(i);
			int id = data.getInt("i");
			if (data.has("d")) {
				if (warnings.containsKey(id)) warnings.remove(id);
			} else {
				// FIXME, this delta includes food consumption
				JSONObject r = data.getJSONObject("r");
				City c = rpc.state.cities.get(id);
				double d = r.getDouble("d");
				double b = r.getDouble("b");
				int m = r.getInt("m");
				int s = r.getInt("s");
				c.resources[3].set(d, b, m, s, rpc.state);
				warnings.put(id, c);
				double secondsleft = c.foodEmptyTime(rpc.state);
				Date runsout = new Date(System.currentTimeMillis() + (1000 * (long)secondsleft));
				Log.v(TAG,String.format("hours: %d %s %s",(int)(secondsleft/60/60),c.name,runsout));
			}
		}
		rpc.runOnUiThread(new Runnable() {
			@Override public void run() {
				rpc.onFoodWarning();
			}});
	}
}
