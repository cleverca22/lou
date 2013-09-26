package com.angeldsis.louapi;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json2.JSONObject;

public class ReportHeader {
	public long timestamp;
	String l,p,objType;
	public int id,generalType,combatType;
	public boolean read,shared;
	private int combatResult; // 0grey, 1green/blue?wtf, 2 red
	private boolean initiatingPlayer;
	private String state;
	public String code;
	static Pattern pattern = Pattern.compile("(.*):(\\d)");
	public enum Image {
		combat_defense, combat_defense_won_wiped,
		combat_defense_lost_defenseless,
		combat_defense_won,combat_defense_lost,
		combat_defense_draw, combat_defense_scout_won_wiped,
		combat_defense_scout_lost_all, combat_defense_scout_lost_some
	}
	public Image image;
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
		code = objType.substring(1, 5);
		combatResult = Integer.parseInt(objType.substring(0, 1),10);
		generalType = Integer.parseInt(objType.substring(1,2),16);
		initiatingPlayer = objType.substring(2, 3).equals("0");
		state = objType.substring(3, 4);
		combatType = Integer.parseInt(objType.substring(4, 5), 16);
		int bitfield = Integer.parseInt(objType.substring(5, 6), 16);
		boolean bit0 = (bitfield & (1 << 0)) != 0;
		boolean bit1 = (bitfield & (1 << 1)) != 0;
		boolean bit2 = (bitfield & (1 << 2)) != 0;
		boolean bit3 = (bitfield & (1 << 3)) != 0;
		// refer to webfrontend.Util.js getReportSubject()
		switch (generalType) {
		case Report.types.general.combat:
			switch (combatType) {
			case Report.types.combat.scout:
				if (!bit2) {
					if (bit1) image = Image.combat_defense_scout_won_wiped;
					else if (bit3) image = Image.combat_defense_scout_lost_all;
					else image = Image.combat_defense_scout_lost_some;
				}
				break;
			case Report.types.combat.plunder:
			case Report.types.combat.assault:
			case Report.types.combat.siege:
				if (bit2) image = Image.combat_defense;
				else if (bit1) image = Image.combat_defense_won_wiped;
				else if (bit0) image = Image.combat_defense_lost_defenseless;
				else if (combatResult != 0) {
					switch (combatResult) {
					case 1:
						image = Image.combat_defense_won;
						break;
					case 2:
						image = Image.combat_defense_lost;
						break;
					case 3:
						image = Image.combat_defense_draw;
						break;
					}
				} else image = Image.combat_defense;
				break;
			}
		}
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
			case Report.types.combat.siege:
				if (state.equals("0")) {
					return String.format("Siege: %s (%s) canceled",l,p);
				} else {
					return String.format("%s: Sieged by %s",l,p);
				}
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
		case Report.types.general.trade:
			return generalType+" "+combatType+" "+l+": trade something "+p;
		}
		return generalType+" "+combatType+" "+l+": unknown type "+p;
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
