package com.angeldsis.louapi;

import org.json2.JSONArray;
import org.json2.JSONException;
import org.json2.JSONObject;

public class Report {
	public int fame,claimPower,oldClaimPower;
	public ReportHalf attacker, defender;
	public String share,objType;
	public ReportHeader reportHeader;
	public String debug;
	public static class types {
		public static class general {
			public static final int combat = 1, trade = 2, city = 3, alliance = 4, enlightenment = 5;
		}
		public static class combat {
			public static final int scout=1, plunder=2, assault=3, support=4, siege=5, raidDungeon=8, settle=9, raidBoss=10;
		}
	}
	public Report(JSONObject r) {
		debug = r.toString();
		fame = r.optInt("f");
		JSONArray a = r.optJSONArray("a");
		reportHeader = new ReportHeader(r.optJSONObject("h"));
		try {
			Log.v("Report",r.toString(1));
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			if (reportHeader.generalType == types.general.alliance) {
				Log.v("Report",r.toString(1));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// FIXME, a contains WAY WAY more then 2 when supporting troops are present
		attacker = new ReportHalf(a.optJSONObject(0));
		JSONObject defenderData = a.optJSONObject(1);
		if (defenderData != null) defender = new ReportHalf(defenderData);
		//Log.v("Report",(new Date(reportHeader.timestamp)).toString());
		int si = r.optInt("si");
		int cs = r.optInt("cs");
		int v = r.optInt("v");
		oldClaimPower = r.optInt("cpo");
		claimPower = r.optInt("cp");
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
			if (u != null) {
				units = new UnitInfo[u.length()];
				for (i=0; i < u.length(); i++) {
					units[i] = new UnitInfo(u.optJSONObject(i));
				}
			}
			int co = h.optInt("co");
			int ai = h.optInt("ai");
			int r = h.optInt("h");
			// city name+id "c": [{"n": "New world","i": 24510739}]
			JSONArray c1 = h.optJSONArray("c");
			if (c1 != null) {
				JSONObject c = c1.optJSONObject(0);
				if (c != null) {
					cityname = c.optString("n");
					coord = c.optInt("i");
				} else Log.v("Report", h.toString());
			} else Log.v("Report",h.toString());
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
