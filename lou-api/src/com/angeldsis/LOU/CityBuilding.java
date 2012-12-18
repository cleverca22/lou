package com.angeldsis.LOU;

import org.json.JSONException;
import org.json.JSONObject;

public class CityBuilding extends LouVisData {
	public int level;
	//private int visId;
	public final static int BUILDING = 1;
	public final static int WALL = 2;

	public CityBuilding(JSONObject structure, int subtype) throws JSONException {
		super(structure);
		//visId = structure.getInt("i");
		switch (subtype) {
		case BUILDING:
			level = structure.getInt("l");
			int s = structure.getInt("s");
			int ss = structure.getInt("ss");
			int se = structure.getInt("se");
			break;
		case WALL:
			int c = structure.getInt("c");
			//Log.v("CityBuilding","c "+c+" typeid "+typeid);
			break;
		}
		
		// state data
		//Log.v("CityBuilding","type id is "+typeid);
	}

}
