package com.angeldsis.louapi;

import org.json.JSONException;
import org.json.JSONObject;

public class CityBuilding extends LouVisData {
	// FIXME, add the rest
	/** the types for typeid **/
	public static final int COTTAGE = 4;
	
	public int level,s,ss,se;
	public final static int BUILDING = 1;
	public final static int WALL = 2;
	// s = 1 means upgrade working/in-queue
	// ss/se are start/end, 0 for in-queue

	public CityBuilding(JSONObject structure, int subtype) throws JSONException {
		super(structure);
		switch (subtype) {
		case BUILDING:
			level = structure.getInt("l");
			s = structure.getInt("s");
			ss = structure.getInt("ss");
			se = structure.getInt("se");
			break;
		case WALL:
			//int c = structure.getInt("c");
			//Log.v("CityBuilding","c "+c+" typeid "+typeid);
			break;
		}
		
		// state data
		//Log.v("CityBuilding","type id is "+typeid);
	}
	@Override
	void update(JSONObject structure) {
		int t = structure.optInt("t");
		if (t == 4) {
			s = structure.optInt("s");
			ss = structure.optInt("ss");
			se = structure.optInt("se");
			int newlevel = structure.optInt("l");
			Log.v("CityBuilding","upgraded from "+level+"->"+newlevel);
			level = newlevel;
		}
		if (hook != null) hook.updated();
	}
}
