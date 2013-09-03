package com.angeldsis.louapi;

import java.util.ArrayList;
import java.util.TreeMap;

import org.json2.JSONArray;
import org.json2.JSONException;
import org.json2.JSONObject;

public class QuestTracker {
	private static final String TAG = "QuestTracker";
	ArrayList<Integer> claimable;
	private TreeMap<Integer, Double> running;
	public void update(JSONObject D) throws JSONException {
		//  {"w":[2517],"v":2,"l":[],"s":[],"c":[2613],"r":[{"p":0,"i":1},...,{"p":0,"i":2518}]}
		int v = D.getInt("v"); // version
		//JSONArray waitingForExternal = D.getJSONArray("w");
		JSONArray running = D.getJSONArray("r");
		JSONArray claimable = D.getJSONArray("c");
		this.claimable = new ArrayList<Integer>();
		int i;
		for (i=0; i<claimable.length(); i++) {
			int id = claimable.getInt(i);
			this.claimable.add(id);
			Log.v(TAG,"claimable quest found:"+id);
		}
		this.running = new TreeMap<Integer,Double>();
		for (i=0; i<running.length(); i++) {
			JSONObject quest = running.getJSONObject(i);
			int id = quest.getInt("i");
			double progress = quest.getDouble("p");
			this.running.put(id, progress);
			//Log.v(TAG,"running quest found:"+id);
		}
		//JSONArray closeable = D.getJSONArray("l");
		//JSONArray closedSpecial = D.getJSONArray("s"); // contains 2545 after a certain timeframe
	}
}
