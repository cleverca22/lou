package com.angeldsis.louapi;

import java.util.Date;

import org.json2.JSONArray;
import org.json2.JSONObject;

public class Report {
	int fame;
	public ReportHalf attacker, defender;
	public String share;
	public long d;
	public Report(JSONObject r) {
		fame = r.optInt("f");
		JSONArray a = r.optJSONArray("a");
		attacker = new ReportHalf(a.optJSONObject(0));
		defender = new ReportHalf(a.optJSONObject(1));
		JSONObject h = r.optJSONObject("h"); // contains attacker data, including unix timestamp
		d = h.optLong("d");
		long d = h.optLong("d");
		Log.v("Report",(new Date(d)).toString());
		int si = r.optInt("si");
		int cs = r.optInt("cs");
		int v = r.optInt("v");
		int cp = r.optInt("cp");
		JSONArray structures = r.optJSONArray("s");
		int ap = r.optInt("ap");
		JSONArray resources = r.optJSONArray("r");
		JSONArray rs = r.optJSONArray("rs");
		int rcc = r.optInt("rcc");
		share = r.optString("sid");
	}
	public class ReportHalf {
		public UnitInfo[] units;
		public String player,cityname;
		public int coord;
		public ReportHalf(JSONObject h) {
			JSONArray u = h.optJSONArray("u");
			int i;
			units = new UnitInfo[u.length()];
			for (i=0; i < u.length(); i++) {
				units[i] = new UnitInfo(u.optJSONObject(i));
			}
			int co = h.optInt("co");
			int ai = h.optInt("ai");
			int r = h.optInt("h");
			// city name+id "c": [{"n": "New world","i": 24510739}]
			JSONObject c = h.optJSONArray("c").optJSONObject(0);
			if (c != null) {
				cityname = c.optString("n");
				coord = c.optInt("i");
			} else Log.v("Report", h.toString());
			int p = h.optInt("p");
			String alliance = h.optString("a");
			player = h.optString("pn");
			// ??? "m": [{"v": 6,"t":84},{"v":1,"t":105}]
		}
	}
	public class UnitInfo {
		// type codes
		// 1 city guard
		// 6 zerk
		// 8 scout
		// 10 paladin
		// s is trapped for attacker, fortified for defender
		public int type,s,ordered,survived;
		public UnitInfo(JSONObject u) {
			type = u.optInt("t");
			s = u.optInt("s");
			ordered = u.optInt("o");
			survived = u.optInt("l");
		}
	}
}
