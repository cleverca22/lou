package com.angeldsis.lou;

public class AndroidEnums {
	public static int getUnitImage(int type) {
		switch (type) {
		case 6: return R.drawable.icon_units_berserker;
		case 19: return R.drawable.icon_units_baron;
		case 23: return R.drawable.icon_npc_gargoyle;
		case 25: return R.drawable.icon_npc_orc;
		case 27: return R.drawable.icon_npc_etin;
		}
		return R.drawable.icon_lou_public_other_world;
	}
	
}
