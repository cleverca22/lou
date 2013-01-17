package com.angeldsis.louapi;

import java.util.Observable;

import org.json2.JSONException;
import org.json2.JSONObject;

public class IncomingAttack extends Observable {
	private static final String TAG = "IncomingAttack";
	public String targetCityName,playerName;
	public int start,end;
	public int id;
	private LouState state;
	public IncomingAttack(JSONObject X) throws JSONException {
		// FIXME, actually use these fields
		// FIXME, not all fields extracted
		int city = X.getInt("c");
		int alliance = X.getInt("a");
		int stepMoongate = X.getInt("ms");
		boolean isMoongate = X.getBoolean("m");
		id = X.getInt("i");
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
	public IncomingAttack(LouState state, int id) {
		this.id = id;
		this.state = state;
	}
	public void updateCityType(JSONObject x) throws JSONException {
		int city = x.getInt("c");
		int alliance = x.getInt("a");
		int stepMoongate = x.getInt("ms");
		boolean isMoongate = x.getBoolean("m");
		int type = x.getInt("t");
		int state2 = x.getInt("s");
		String cityName = x.getString("cn"); // source city
		int player = x.getInt("p");
		String allianceName = x.getString("an"); // source alliance
		int start = x.getInt("ss");
		String playerName = x.getString("pn"); // source player name
		int end = x.getInt("es");
		Log.v(TAG,"attack incoming to current city, from "+playerName);
		Log.v(TAG,"time left: "+(end - state.getServerStep()));
		// FIXME, actually use these fields
		setChanged();
		notifyObservers(this);
	}
}
