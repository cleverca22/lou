package com.angeldsis.louapi;

import java.util.Date;

import org.json2.JSONObject;

public class ReportHeader {
	long timestamp;
	String l,p;
	public int id;
	public ReportHeader(JSONObject h) {
		timestamp = h.optLong("d");
		id = h.optInt("i");
		l = h.optString("l");
		int o = h.optInt("o");
		String on = h.optString("on");
		p = h.optString("p");
		int r = h.optInt("r");
		int s = h.optInt("s");
		int t = h.optInt("t");
	}
	public String toString() {
		return l+": Plundered by "+p;
		// if it was a raid
		// return "Raid: "+l; // reformat l from "Mountain:6" to "Mountain Dungeon (6)"
	}
	public Date getTime() {
		return new Date(timestamp);
	}
}
