package com.angeldsis.louapi.data;

import org.json2.JSONObject;

public class BuildQueue {
	// state 5 == demolish?
	public int warnings,type,state,building,l,y,x,i;
	public boolean isPaid;
	public BuildQueue(JSONObject q) {
		// {"w":0,"t":14,"s":1,"b":66569,"p":true,"l":0,"y":4,"x":9,"i":30387448}
		warnings = q.optInt("w");
		type = q.optInt("t");
		state = q.optInt("s");
		building = q.optInt("b");
		isPaid = q.optBoolean("p");
		l = q.optInt("l");
		y = q.optInt("y");
		x = q.optInt("x");
		i = q.optInt("i");
	}
}
