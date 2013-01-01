package com.angeldsis.louapi;

import org.json.JSONException;
import org.json.JSONObject;

public class IncomingAttack {
	public String targetCityName,playerName;
	public int start,end;
	public IncomingAttack(JSONObject X) throws JSONException {
		// FIXME, actually use these fields
		// FIXME, not all fields extracted
		int city = X.getInt("c");
		int alliance = X.getInt("a");
		int stepMoongate = X.getInt("ms");
		boolean isMoongate = X.getBoolean("m");
		int id = X.getInt("i");
		int type = X.getInt("t");
		int state = X.getInt("s");
		String cityName = X.getString("cn"); // source city
		int player = X.getInt("p");
		String allianceName = X.getString("an"); // source alliance
		start = X.getInt("ss");
		playerName = X.getString("pn"); // source player name
		end = X.getInt("es");
		targetCityName = X.getString("tcn");
	}

}
