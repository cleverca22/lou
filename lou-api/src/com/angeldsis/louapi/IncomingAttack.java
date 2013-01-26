package com.angeldsis.louapi;

import java.util.Date;
import java.util.Observable;

import org.json2.JSONException;
import org.json2.JSONObject;

public class IncomingAttack extends Observable {
	enum DataSource { alliance, player, city };
	DataSource lastDataSource;
	private static final String TAG = "IncomingAttack";
	public String targetCityName,sourcePlayerName,
		defender;
	// start seems to always be 0?
	public int start,spotted,end;
	public int id;
	public Alliance sourceAlliance;
	private LouState state;
	public String sourceCityName;
	public int total_strength_attacker,total_strength_defender,tc;
	public boolean targetIsMe;
	/*public IncomingAttack(JSONObject X) throws JSONException {
	}*/
	public IncomingAttack(LouState state, int id) {
		this.id = id;
		this.state = state;
		targetIsMe = false;
	}
	public void updateCityType(JSONObject x) throws JSONException {
		targetIsMe = true;
		lastDataSource = DataSource.city;
		start = x.getInt("ss");
		end = x.getInt("es");

		int city = x.getInt("c");
		int alliance = x.getInt("a");
		int stepMoongate = x.getInt("ms");
		boolean isMoongate = x.getBoolean("m");
		int type = x.getInt("t");
		int state2 = x.getInt("s");
		String cityName = x.getString("cn"); // source city
		int player = x.getInt("p");
		String allianceName = x.getString("an"); // source alliance
		String playerName = x.getString("pn"); // source player name
		Log.v(TAG,"attack incoming to current city, from "+playerName);
		printTimes();
		// FIXME, actually use these fields
		setChanged();
		notifyObservers(this);
	}
	public void updatePlayerType(JSONObject X) throws JSONException {
		targetIsMe = true;
		lastDataSource = DataSource.player;
		start = X.getInt("ss");
		end = X.getInt("es");

		// FIXME, actually use these fields
		// FIXME, not all fields extracted
		int city = X.getInt("c");
		int alliance = X.getInt("a");
		int stepMoongate = X.getInt("ms");
		boolean isMoongate = X.getBoolean("m");
		int type = X.getInt("t");
		int state = X.getInt("s");
		String cityName = X.getString("cn"); // source city
		int player = X.getInt("p");
		String allianceName = X.getString("an"); // source alliance
		sourcePlayerName = X.getString("pn"); // source player name
		targetCityName = X.getString("tcn");
		Log.v(TAG,"attack incoming to "+this.targetCityName+" from player "+this.sourcePlayerName);
		printTimes();
		setChanged();
		notifyObservers(this);
	}
	public void updateAllianceType(JSONObject a) {
		lastDataSource = DataSource.alliance;
		// 0==unknown
		// 5==internal attack or beseiged, probly not type
		int type = a.optInt("t");

		tc = a.optInt("tc"); // cityid
		targetCityName = a.optString("tcn");

		sourceCityName = a.optString("cn");
		sourceAlliance = Alliance.get(a.optInt("a"), a.optString("an"));
		total_strength_attacker = a.optInt("ta");
		total_strength_defender = a.optInt("td");
		defender = a.optString("tpn");
		start = a.optInt("ss");
		sourcePlayerName = a.optString("pn");
		end = a.optInt("es");
		spotted = a.optInt("ds"); // uses step units

		boolean b = a.optBoolean("b"); // baron capture?
		int cp = a.optInt("cp"); // claim power?

		int c = a.optInt("c");
		int ms = a.optInt("ms");
		int tp = a.optInt("tp");
		boolean m = a.optBoolean("m"); // moongate used?
		boolean thc = a.optBoolean("thc"); // controls presense of other fields?
		int s = a.optInt("s");
		int p = a.optInt("p");
		printTimes();
		setChanged();
		notifyObservers(this);
	}
	void printTimes() {
		if (state == null) return;
		Date start = new Date(state.stepToMilis(this.start));
		Date detected = new Date(state.stepToMilis(this.spotted));
		Date end = new Date(state.stepToMilis(this.end));
		String msg = String.format("start: %s, spotted: %s, end: %s",start,detected,end);
		Log.v(TAG,msg);
		Log.v(TAG,"time left: "+(this.end - state.getServerStep()));
	}
}
