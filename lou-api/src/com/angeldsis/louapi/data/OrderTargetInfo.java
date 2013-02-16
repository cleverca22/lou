package com.angeldsis.louapi.data;

import org.json2.JSONException;
import org.json2.JSONObject;

import com.angeldsis.louapi.Alliance;
import com.angeldsis.louapi.Player;

public class OrderTargetInfo {
	public Alliance alliance;
	int cityid;
	public String cityname;
	public Player player;
	public double targetDistance,targetDistanceWater;
	public OrderTargetInfo(World world, JSONObject r) throws JSONException {
		alliance = world.getAlliance(r.getInt("a"), r.getString("an"));
		cityid = r.getInt("c");
		cityname = r.getString("cn");
		targetDistance = r.getDouble("d");
		boolean du = r.getBoolean("du");
		targetDistanceWater = r.getDouble("dw");
		int iTargetEnlightenmentEnd = r.getInt("et");
		int et = r.getInt("et");
		int ex = r.getInt("ex");
		int mf = r.getInt("mf");
		int mt = r.getInt("mt");
		player = world.getPlayer(r.getInt("p"), r.getString("pn"));
		int pl = r.getInt("pl");
		int pp = r.getInt("pp");
		int pte = r.getInt("pte");
		int ptps = r.getInt("ptps");
		int pts = r.getInt("pts");
		boolean s = r.getBoolean("s");
		int type = r.getInt("t");
		// (type IN (0-30)) AND playerid = -1 ==== lawless city
		// type == -10 AND playerid = -1 ==== target ruins
		// t > 0 === not valid city
		// t == -20 ==== target  anonymous
		// else its valid
	}
}
