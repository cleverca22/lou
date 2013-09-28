package com.angeldsis.lou;

import com.angeldsis.louapi.ReportHeader;

public class AndroidEnums {
	private static final int[] units = {0,R.drawable.icon_units_cityguard,R.drawable.icon_units_ballista,
		R.drawable.icon_units_ranger,R.drawable.icon_units_guardian,R.drawable.icon_units_templar,
		R.drawable.icon_units_berserker,R.drawable.icon_units_mage,R.drawable.icon_units_scout,
		R.drawable.icon_units_crossbow,R.drawable.icon_units_paladin,R.drawable.icon_units_knight,
		R.drawable.icon_units_warlock,R.drawable.icon_units_ram,R.drawable.icon_units_catapult,
		R.drawable.icon_units_frigate,R.drawable.icon_units_barge,R.drawable.icon_units_wargalleon,0,
		R.drawable.icon_units_baron,R.drawable.icon_npc_skeleton,R.drawable.icon_npc_ghoul,
		R.drawable.icon_npc_kraken,R.drawable.icon_npc_gargoyle,R.drawable.icon_npc_demon,
		R.drawable.icon_npc_orc,R.drawable.icon_npc_troglodyte,R.drawable.icon_npc_etin,
		R.drawable.icon_npc_minotaur,R.drawable.icon_npc_spider,R.drawable.icon_npc_thief,
		R.drawable.icon_npc_centaur,R.drawable.icon_npc_troll,R.drawable.icon_npc_drake,
		R.drawable.icon_npc_hydra,R.drawable.icon_npc_moloch,R.drawable.icon_npc_drake,
		R.drawable.icon_npc_hydra,R.drawable.icon_npc_moloch,R.drawable.icon_npc_drake,
		R.drawable.icon_npc_hydra,R.drawable.icon_npc_moloch,R.drawable.icon_npc_drake,
		R.drawable.icon_npc_hydra,R.drawable.icon_npc_moloch,R.drawable.icon_npc_drake,
		R.drawable.icon_npc_hydra,R.drawable.icon_npc_moloch,R.drawable.icon_npc_drake,
		R.drawable.icon_npc_drake,R.drawable.icon_npc_drake,R.drawable.icon_npc_drake,
		R.drawable.icon_npc_drake,R.drawable.icon_npc_hydra,R.drawable.icon_npc_hydra,
		R.drawable.icon_npc_hydra,R.drawable.icon_npc_hydra,R.drawable.icon_npc_hydra,
		R.drawable.icon_npc_moloch,R.drawable.icon_npc_moloch,R.drawable.icon_npc_moloch,
		R.drawable.icon_npc_moloch,R.drawable.icon_npc_moloch,R.drawable.icon_npc_pirate_frigate,
		R.drawable.icon_npc_pirates_dhow,R.drawable.icon_npc_pirates,R.drawable.icon_npc_pirate_galleon,
		R.drawable.icon_npc_kraken,R.drawable.icon_npc_kraken,R.drawable.icon_npc_kraken,
		R.drawable.icon_npc_kraken,R.drawable.icon_npc_kraken,R.drawable.icon_npc_kraken,
		R.drawable.icon_npc_kraken,R.drawable.icon_npc_kraken,R.drawable.icon_npc_kraken,
		R.drawable.icon_npc_kraken};
	public static int getUnitImage(int type) {
		return units[type];
	}
	public static LouImage[] getStructureImage(int type,int level) {
		if (level == 0) {
			int res = -1;
			// level 0 with static image
			switch (type) {
			case 24: res = R.drawable.tower_construction_site;break;
			case 25: res = R.drawable.tower_construction_site;break;
			case 26: res = R.drawable.tower_construction_site;break;
			case 27: res = R.drawable.iron_destroy_resource;break;
			case 28: res = R.drawable.stone_destroy_resource;break;
			case 29: res = R.drawable.forrest_destroy_resource;break;
			case 30: res = R.drawable.lake_destroy_resource;break;
			case 38: res = R.drawable.tower_construction_site;break;
			case 39: res = R.drawable.tower_construction_site;break;
			case 40: res = R.drawable.tower_construction_site;break;
			case 41: res = R.drawable.tower_construction_site;break;
			case 42: res = R.drawable.tower_construction_site;break;
			case 43: res = R.drawable.tower_construction_site;break;
			case 44: res = R.drawable.tower_construction_site;break;
			case 45: res = R.drawable.tower_construction_site;break;
			case 46: res = R.drawable.tower_construction_site;break;
			case 60: res = R.drawable.iron_destroy_resource;break;
			case 61: res = R.drawable.stone_destroy_resource;break;
			case 62: res = R.drawable.forrest_destroy_resource;break;
			case 63: res = R.drawable.lake_destroy_resource;break;
			}
			if (res != -1) {
				LouImage[] ret = new LouImage[1];
				ret[0] = new LouImage(res,128,128);
				return ret;
			}
			// level 0 with animation
			LouImage[] images = null;
			switch (type) {
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:
			case 15:
			case 16:
			case 17:
			case 18:
			case 19:
			case 20:
			case 21:
			case 22:
			case 23:
			case 36:
			case 37:
			case 47:
			case 48:
			case 49:
			case 50:
			case 64:
			case 65:
			case 66:
			case 67:
			case 68:
			case 69:
				images = new LouImage[3];
				images[0] = new LouImage(R.drawable.building_construction_site,128,128);
				images[1] = new LouAnimation(R.drawable.animseq_construction_site,75,99,0,11);
				images[2] = new LouAnimation(R.drawable.animseq_dustcloud_01,75,99,0,11);
				break;
			case 51:
			case 52:
			case 53:
			case 54:
			case 55:
			case 56:
			case 57:
			case 58:
			case 59:
				images = new LouImage[5];
				images[0] = new LouImage(R.drawable.building_palace_construction,384,288);
				images[1] = new LouAnimation(R.drawable.animseq_construction_site,75,99,226,163);
				images[2] = new LouAnimation(R.drawable.animseq_dustcloud_01,75,99,226,174);
				images[3] = new LouAnimation(R.drawable.animseq_construction_site,75,99,16,103);
				images[4] = new LouAnimation(R.drawable.animseq_dustcloud_01,75,99,0,114);
				break;
			}
			return images;
		}
		// beyond level 0
		return null;
	}
	public static int getReportIcon(ReportHeader h) {
		switch (h.image) {
		case combat_defense:
			return R.drawable.combat_defense;
		case combat_defense_draw:
			return R.drawable.combat_defense_draw;
		case combat_defense_lost:
			return R.drawable.combat_defense_lost;
		case combat_defense_lost_defenseless:
			return R.drawable.combat_defense_lost_defenseless;
		case combat_defense_won:
			return R.drawable.combat_defense_won;
		case combat_defense_won_wiped:
			return R.drawable.combat_defense_won_wiped;
		case combat_defense_scout_lost_all:
			return R.drawable.combat_defense_scout_lost_all;
		case combat_defense_scout_lost_some:
			return R.drawable.combat_defense_scout_lost_some;
		case combat_defense_scout_won_wiped:
			return R.drawable.combat_defense_won_wiped;
		}
		return 0;
	}
}
