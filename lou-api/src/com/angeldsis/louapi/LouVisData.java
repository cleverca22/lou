package com.angeldsis.louapi;

import org.json2.JSONException;
import org.json2.JSONObject;

public abstract class LouVisData {
	public int x,y, subid, typeid;
	// subid would be resource type, structure type, or wall direction
	public int type, visId;
	public Hook hook = null;
	LouVisData(JSONObject base) throws JSONException {
		typeid = base.getInt("v");
		x = base.getInt("x");
		y = base.getInt("y");
		visId = base.getInt("i");
	}
	abstract void update(JSONObject structure) throws JSONException;
	public interface Hook {
		void updated();
	}
}
