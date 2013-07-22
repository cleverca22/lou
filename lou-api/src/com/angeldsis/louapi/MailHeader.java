package com.angeldsis.louapi;

import org.json2.JSONArray;
import org.json2.JSONException;
import org.json2.JSONObject;

public class MailHeader {
	public String subject,from;
	public int id;
	public long date;
	public String[] to;
	public MailHeader(JSONObject o) throws JSONException {
		// {"r":true,"mt":0,"fi":637,"ci":[],"ti":[1487,2721],"cc":[]}
		subject = o.getString("s");
		from = o.getString("f");
		id = o.getInt("i");
		date = o.getLong("d");
		JSONArray toIN = o.optJSONArray("t");
		int i;
		if (toIN != null) {
			to = new String[toIN.length()];
			for (i=0; i<toIN.length(); i++) {
				to[i] = toIN.getString(i);
			}
		}
	}
}
