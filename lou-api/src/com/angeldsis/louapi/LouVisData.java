package com.angeldsis.louapi;

import org.json2.JSONException;
import org.json2.JSONObject;

import com.angeldsis.louapi.LouState.City;

public abstract class LouVisData {
	private City c;
	public int x,y, subid, typeid;
	// subid would be resource type, structure type, or wall direction
	public int type, visId;
	public Hook hook = null;
	LouVisData(City c,JSONObject base) throws JSONException {
		this.c = c;
		typeid = base.getInt("v");
		x = base.getInt("x");
		y = base.getInt("y");
		visId = base.getInt("i");
	}
	abstract void update(JSONObject structure) throws JSONException;
	public interface Hook {
		void updated();
	}
	City getCity() { return c; }
}
