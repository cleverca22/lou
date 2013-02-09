package com.angeldsis.louapi.data;

import java.util.ArrayList;

import org.json2.JSONArray;
import org.json2.JSONException;
import org.json2.JSONObject;

import com.angeldsis.louapi.Log;

public class AllianceForum {
	boolean hup,sf;
	String forumName;
	ArrayList<What> rw = new ArrayList<What>();
	int forumID;
	
	public class What {

	}
	public AllianceForum(JSONObject x) throws JSONException {
		hup = x.getBoolean("hup");
		sf = x.getBoolean("sf");
		forumName = x.getString("ft");
		forumID = x.getInt("fi");
		int i;
		JSONArray a = x.getJSONArray("rw");
		for (i=0; i < a.length(); i++) {
			JSONObject y = a.getJSONObject(i);
			Log.v("AllianceForum",y.toString(1));
		}
	}
	public String toString() {
		return forumName;
	}
}
