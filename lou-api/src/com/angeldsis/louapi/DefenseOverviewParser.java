package com.angeldsis.louapi;

import org.json2.JSONArray;
import org.json2.JSONException;
import org.json2.JSONObject;

import com.angeldsis.louapi.LouState.City;
import com.angeldsis.louapi.data.UnitCount;

public class DefenseOverviewParser {
	private static final String TAG = "DefenseOverviewParser";
	private boolean getall = true;
	public String getRequestDetails() {
		if (getall) return "a";
		return "";
	}
	public void parse(JSONArray jsonArray,final RPC rpc) throws JSONException {
		getall = false;
		int i,j,x;
		for (i=0; i<jsonArray.length();i++) {
			JSONObject item = jsonArray.getJSONObject(i);
			//Log.v(TAG,"item:"+item.toString());
			int id = item.getInt("i");
			City city = rpc.state.findCityById(id);
			JSONArray c = item.getJSONArray("c");
			for(j=0; j<c.length();j++) {
				JSONObject item2 = c.getJSONObject(j);
				int id2 = item2.getInt("i");
				if (id2 != 0) continue;
				//Log.v(TAG,"item2:"+item2.toString());
				JSONArray units = item2.getJSONArray("u");
				if (city.units == null) {
					city.units = new UnitCount[78];
				}
				// FIXME, find the right entry, rather then nuke it
				for (x=0; x<units.length(); x++) {
					JSONObject ucin = units.getJSONObject(x);
					int type = ucin.getInt("t");
					UnitCount uc = city.units[type];
					if (uc == null) uc = city.units[type] = new UnitCount();
					uc.t = type;
					uc.c = ucin.getInt("c");
					if (uc.tc < uc.c) uc.tc = uc.c;
				}
			}
		}
		rpc.runOnUiThread(new Runnable() {
			public void run() {
				rpc.onDefenseOverviewUpdate();
			}
		});
	}
}
