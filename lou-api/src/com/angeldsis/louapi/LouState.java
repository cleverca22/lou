package com.angeldsis.louapi;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.angeldsis.louapi.LouState.City;

public class LouState {
	private static final String TAG = "LouState";
	int AllianceId;
	String AllianceName,Name;
	public ArrayList<City> cities;
	public ArrayList<LouVisData> visData;
	public City currentCity;
	public Counter gold;
	public ManaCounter mana;
	public ArrayList<IncomingAttack> incoming_attacks;
	private int serverOffset, diff, stepTime;
	long refTime;
	public ArrayList<ChatMsg> chat_history;
	RPC rpc;

	public LouState() {
		this.rpc = rpc;
		visData = new ArrayList<LouVisData>();
		incoming_attacks = new ArrayList<IncomingAttack>();
		chat_history = new ArrayList<ChatMsg>();
		gold = new Counter(this);
		mana = new ManaCounter(this);
	}
	public void processPlayerInfo(JSONObject obj) throws JSONException {
		JSONArray cities = obj.getJSONArray("Cities");
		int x;
		this.cities = new ArrayList<City>();
		for (x = 0; x < cities.length(); x++) {
			JSONObject cityin = cities.getJSONObject(x);
			City cityout = new City();
			cityout.name = cityin.getString("n");
			cityout.cityid = cityin.getLong("i");
			this.cities.add(cityout);
		}
		currentCity = this.cities.get(0);
		AllianceId = obj.getInt("AllianceId");
		AllianceName = obj.getString("AllianceName");
		Name = obj.getString("Name");
	}
	public class City {
		public Resource[] resources;
		public String name;
		long cityid;
		City() {
			resources = new Resource[4];
			int i;
			for (i = 0; i < 4; i++) resources[i] = new Resource();
		}
		public String toString() {
			return name;
		}
	}
	public void addVisObj(LouVisData parsed) {
		visData.add(parsed);
	}
	public void parsePlayerUpdate(JSONObject d) throws JSONException {
		if (d.has("g")) {
			JSONObject g = d.optJSONObject("g");
			double base = g.optDouble("b");
			double delta = g.optDouble("d");
			int step = g.optInt("s");
			gold.update(base,delta,step);
		}
		if (d.has("m")) {
			JSONObject m = d.optJSONObject("m");
			double base = m.optDouble("b");
			double delta = m.optDouble("d");
			int step = m.optInt("s");
			int max = m.optInt("m");
			mana.update(base, delta, max, step);
		}
		if (d.has("iuo")) {
			Object iuo2 = d.get("iuo");
			if (iuo2 != JSONObject.NULL) {
				JSONArray iuo = (JSONArray) iuo2;
				//Log.v(TAG, iuo.toString(1));
				int x;
				for (x = 0; x < iuo.length(); x++) {
					// incoming attacks on current city
					JSONObject X = iuo.getJSONObject(x);
					IncomingAttack ia = new IncomingAttack(X);
					Log.v(TAG,"attack incoming to "+ia.targetCityName+" from player "+ia.playerName);
					incoming_attacks.add(ia);
				}
			}
			else Log.v(TAG,"no attacks 2!");
		}
		else Log.v(TAG,"no attacks?");
	}
	public void setTime(long refTime2, int stepTime, int diff, int serverOffset) {
		refTime = refTime2;
		this.stepTime = stepTime;
		this.diff = diff;
		this.serverOffset = serverOffset;
		Log.v(TAG,"ref:"+refTime2+" stepTime:"+stepTime+" diff:"+diff+" serverOffset"+serverOffset);
	}
	// not sure entirely what these are for yet, so i'm reproducing them exactly
	long getServerStep() {
		if (stepTime == 0) return 0;
		long d = System.currentTimeMillis() - refTime - diff;
		return d / stepTime;
	}
	public void changeCity(City city) {
		currentCity = city;
		rpc.interrupt();
		rpc.cityChanged(); // FIXME, maybe fire this after the new data is in
	}
	public void setRPC(RPC rpc2) {
		rpc = rpc2;
	}
}
