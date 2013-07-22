package com.angeldsis.louapi;

import org.json2.JSONException;
import org.json2.JSONObject;

public class MailBoxFolder {
	public String name;
	int id,p;
	public int count;
	public MailBoxFolder(JSONObject in) throws JSONException {
		id = in.getInt("i");
		name = in.getString("n");
		p = in.getInt("p"); // whats this do?
	}
}
