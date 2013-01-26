package com.angeldsis.louapi;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json2.JSONObject;

public class ReportHeader {
	public long timestamp;
	String l,p,objType;
	public int id,generalType,combatType;
	public boolean read,shared;
	static Pattern pattern = Pattern.compile("(.*):(\\d)");
	public ReportHeader(JSONObject h) {
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
		Matcher m;
		switch (generalType) {
		case Report.types.general.combat:
			switch (combatType) {
			case Report.types.combat.scout:
				return l+": Scouted by "+p;
			case Report.types.combat.plunder:
				return l+": Plundered by "+p;
			case Report.types.combat.assault:
				return l+": Assaulted by "+p;
			case Report.types.combat.support:
				return l+": Support from "+p+" arrived";
			case Report.types.combat.seige:
				return l+": Seigged by "+p;
			case Report.types.combat.settle:
				return "Settle something something "+objType;
			case Report.types.combat.raidDungeon:
				m = pattern.matcher(l);
				if (m.find()) {
					return "Raid: "+m.group(1)+" Dungeon ("+m.group(2)+")";
				}
				return "Raid: "+l;
			case Report.types.combat.raidBoss:
				m = pattern.matcher(l);
				if (m.find()) {
					String c = m.group(1);
					String name = "unknown "+c;
					if (c.equals("Boss Hill")) name = "Moloch";
					return "Raid: "+name+" ("+m.group(2)+")";
				}
				return "Raid: "+l;
			}
			break;
		case Report.types.general.alliance:
			return "alliance something "+objType;
		case Report.types.general.city:
			return l+": city something";
		}
		return generalType+" "+combatType+" "+l+": Plundered by "+p;
	}
	public Date getTime() {
		return new Date(timestamp);
	}
	public String formatTime(TimeZone tz) {
		Calendar c = Calendar.getInstance(tz);
		c.setTime(getTime());
		return String.format("%02d.%02d %02d:%02d:%02d",c.get(Calendar.MONTH)+1,
				c.get(Calendar.DAY_OF_MONTH),
				c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),
				c.get(Calendar.SECOND));
	}
}
