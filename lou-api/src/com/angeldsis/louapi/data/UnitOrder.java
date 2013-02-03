package com.angeldsis.louapi.data;

import com.google.gson.annotations.SerializedName;

public class UnitOrder {
	@SerializedName("i") public int id;
	@SerializedName("a") int allianceId;
	@SerializedName("an") String allianceName;
	//@SerializedName("p") int playerId;
	@SerializedName("pn") public String playerName;
	@SerializedName("u") UnitData[] units;
	@SerializedName("ss") public int startStep;
	@SerializedName("es") public int endStep;
	@SerializedName("t") public int type;
	@SerializedName("s") public int state; // 1==leaving, 2==return?, 5==seiging?
	@SerializedName("flag") int flag;
	int x,y;
	public String toString() {
		return String.format("%d %s %s id:%d loc: %d:%d %s, %d units",flag,getState(),getType(),id,x,y,playerName,units == null ? -1 : units.length);
	}
	public String getType() {
		switch (type) {
		case 1: return "Scout";
		case 2: return "Plunder";
		case 3: return "assault";
		case 4: return "support";
		case 5: return "Siege";
		case 6: return "reportDefend";
		case 7: return "reportSupport";
		case 8: return "Raid";
		case 9: return "foundcity";
		case 10: return "raid boss";
		default:
			return String.format("unknown#%d",type);
		}
	}
	public String getState() {
		switch (state) {
		case 1: return "working";
		case 2: return "return";
		case 3: return "done";
		case 4: return "waiting";
		case 5: return "looping";
		default:
			return String.format("unknown#%d",state);
		}
	}
}
