package com.angeldsis.LOU;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LouState {
	int AllianceId;
	String AllianceName,Name;
	ArrayList<City> cities;
	public ArrayList<LouVisData> visData;
	City currentCity;
	public Resource[] resources;
	public LouState() {
		visData = new ArrayList<LouVisData>();
		resources = new Resource[4];
		int i;
		for (i = 0; i < 4; i++) resources[i] = new Resource();
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
	class City {
		String name;
		long cityid;
	}
	public void addVisObj(LouVisData parsed) {
		visData.add(parsed);
	}
}
