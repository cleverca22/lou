package com.angeldsis.louapi.data;

import org.json2.JSONException;
import org.json2.JSONObject;

public class ForumPost {
	public String msg;
	public ForumPost(JSONObject o) throws JSONException {
		// TODO Auto-generated constructor stub
		// {"m":"....","pi":51832,"pli":1199,"pn":"Vishalicious","t":1360038310930,"up":true}
		msg = o.getString("m");
	}

}
