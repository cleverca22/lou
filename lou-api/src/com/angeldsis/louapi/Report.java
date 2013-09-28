package com.angeldsis.louapi;

import org.json2.JSONArray;
import org.json2.JSONException;
import org.json2.JSONObject;

import com.angeldsis.louapi.data.Coord;

public class Report {
	private static final String TAG = "Report";
	public int fame,claimPower,oldClaimPower;
	public ReportHalf attacker, defender;
	public String share,objType;
	public ReportHeader reportHeader;
	public String debug;
	private int voidRes;
	private String defenderShare;
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
		JSONArray a = r.optJSONArray("a");
		reportHeader = new ReportHeader(r.optJSONObject("h"));
		try {
			Log.v(TAG,r.toString(1));
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
		Log.v(TAG,"a size:"+a.length());
		attacker = new ReportHalf(a.optJSONObject(0));
		JSONObject defenderData = a.optJSONObject(1);
		if (defenderData != null) defender = new ReportHalf(defenderData);
		//Log.v("Report",(new Date(reportHeader.timestamp)).toString());
		
		int attackPower = r.optInt("ap");

		int targetCityStateMask = r.optInt("cs");
		oldClaimPower = r.optInt("cpo");
		claimPower = r.optInt("cp");

		fame = r.optInt("f");

		int o = r.optInt("o"); // owner, same as o in ReportHeader
		
		JSONArray resources = r.optJSONArray("r");
		JSONArray rs = r.optJSONArray("rs");
		int rcc = r.optInt("rcc");
		
		int si = r.optInt("si");
		share = r.optString("sid");
		defenderShare = r.optString("sidd");
		JSONArray structures = r.optJSONArray("s");

		voidRes = r.optInt("v");
	}
	public class ReportHalf {
		public UnitInfo[] units;
		public String player,cityname;
		public Coord coord;
		public String alliance;
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
					coord = Coord.fromCityId(c.optInt("i"));
				} else Log.v("Report", h.toString());
			} else Log.v("Report",h.toString());
			int p = h.optInt("p");
			alliance = h.optString("a");
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
	public String formatShareString() {
		return String.format("%s-%s-%s-%s",share.substring(0, 4),share.substring(4, 8),share.substring(8, 12),share.substring(12, 16));
	}
}
