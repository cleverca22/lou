package com.angeldsis.LOU;

import org.json.JSONException;
import org.json.JSONObject;

public class LouVisData {
	public int x,y, subid, typeid;
	// subid would be resource type, structure type, or wall direction
	public int type;
	LouVisData(JSONObject base) throws JSONException {
		typeid = base.getInt("v");
		x = base.getInt("x");
		y = base.getInt("y");
	}
}
