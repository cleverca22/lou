package com.angeldsis.louapi;

import java.util.Collection;
import java.util.TreeMap;

import org.json2.JSONArray;
import org.json2.JSONException;

import com.angeldsis.louapi.LouState.City;

public class CityGroup {
	public String name;
	public City[] cities;
	public enum Type {
		ALL
	};
	public CityGroup(String name, JSONArray cities, LouState state) {
		this.name = name;
		int i;
		this.cities = new City[cities.length()];
		for (i=0; i<cities.length(); i++) {
			try {
				this.cities[i] = state.cities.get(cities.getInt(i));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public CityGroup(Type type, TreeMap<Integer, City> cities) {
		if (type == Type.ALL) name = "All";
		Collection<City> temp = cities.values();
		this.cities = new City[temp.size()];
		this.cities = temp.toArray(this.cities);
	}
}
