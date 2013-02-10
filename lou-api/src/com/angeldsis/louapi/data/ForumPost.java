package com.angeldsis.louapi.data;

import org.json2.JSONException;
import org.json2.JSONObject;

public class ForumPost {
	public String msg,playerName;
	public ForumPost(JSONObject o) throws JSONException {
		// {"pi":51832,"t":1360038310930,"up":true}
		msg = o.getString("m");
		playerName = o.getString("pn");
		int playerid = o.getInt("pli");
	}

}
