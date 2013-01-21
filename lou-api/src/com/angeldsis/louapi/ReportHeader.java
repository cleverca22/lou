package com.angeldsis.louapi;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.json2.JSONObject;

public class ReportHeader {
	public long timestamp;
	String l,p,objType;
	public int id,generalType,combatType;
	public boolean read,shared;
	LouState state;
	public ReportHeader(LouState state, JSONObject h) {
		this.state = state;
		timestamp = h.optLong("d");
		id = h.optInt("i");
		l = h.optString("l");
		int o = h.optInt("o");
		String on = h.optString("on");
		p = h.optString("p");
		read = h.optInt("r") == 1;
		shared = h.optInt("s") == 1;
		int t = h.optInt("t");
		//Log.v("ReportHeader",h.toString());
		objType = h.optString("t");
		generalType = Integer.parseInt(objType.substring(1,2),16);
		combatType = Integer.parseInt(objType.substring(4, 5), 16);
	}
	public String toString() {
		switch (generalType) {
		case Report.types.general.combat:
			switch (combatType) {
			case Report.types.combat.scout:
				return l+": Scouted by "+p;
			case Report.types.combat.plunder:
				return l+": Plundered by "+p;
			case Report.types.combat.assault:
				return l+": Assaulted by "+p;
			case Report.types.combat.settle:
				return "Settle something something "+objType;
			case Report.types.combat.raidDungeon:
			case Report.types.combat.raidBoss:
				// return "Raid: "+l; // reformat l from "Mountain:6" to "Mountain Dungeon (6)"
				return "Raid: "+l;
			}
			break;
		case Report.types.general.alliance:
			return "alliance something "+objType;
		}
		return generalType+" "+combatType+" "+l+": Plundered by "+p;
	}
	public Date getTime() {
		return new Date(timestamp);
	}
	public String formatTime() {
		Calendar c = Calendar.getInstance(state.tz);
		c.setTime(getTime());
		return String.format("%02d.%02d %02d:%02d:%02d",c.get(Calendar.MONTH)+1,
				c.get(Calendar.DAY_OF_MONTH),
				c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),
				c.get(Calendar.SECOND));
	}
}
