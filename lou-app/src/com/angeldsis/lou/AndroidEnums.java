package com.angeldsis.lou;

public class AndroidEnums {
	public static int getUnitImage(int type) {
		switch (type) {
		case 2: return R.drawable.icon_units_ballista;
		case 3: return R.drawable.icon_units_ranger;
		case 4: return R.drawable.icon_units_guardian;
		case 5: return R.drawable.icon_units_templar;
		case 6: return R.drawable.icon_units_berserker;
		case 8: return R.drawable.icon_units_scout;
		case 9: return R.drawable.icon_units_crossbow;
		case 10: return R.drawable.icon_units_paladin;
		case 11: return R.drawable.icon_units_knight;
		case 13: return R.drawable.icon_units_ram;
		case 14: return R.drawable.icon_units_catapult;
		case 19: return R.drawable.icon_units_baron;
		case 23: return R.drawable.icon_npc_gargoyle;
		case 25: return R.drawable.icon_npc_orc;
		case 27: return R.drawable.icon_npc_etin;
		}
		return R.drawable.icon_lou_public_other_world;
	}
	
}
