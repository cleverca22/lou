package com.angeldsis.louapi.world;

import com.angeldsis.louapi.world.WorldParser.MapItem;

public class Boss extends MapItem {
	public static final int Dragon = 6,
			Moloch = 7,
			Hydra = 8,
			Octopus = 12;
	public boolean state;
	public int bossType;
	public int bossLevel;
	public int slot;
	public int startStep;
	public String getType() {
		switch (bossType) {
		case Dragon:
			return "Dragon";
		case Hydra:
			return "Hydra";
		case Moloch:
			return "Moloch";
		case Octopus:
			return "Octopus";
		default:
			return "other"+bossType;
		}
	}
	// http://louaid.com/game-charts/killing-bosses.aspx
	static final int DragonZerks[] = { 50,300,2000,4000,10000,15000,20000,30000,45000,60000 };
	static final int HydraZerks[] = { 34, 200, 1360, 2640, 6640, 10000, 13600, 20000, 30000, 40000 };
	static final int MolochZerks[] = { 50, 300, 2000, 4000, 10000, 15000, 20000, 30000, 45000, 60000 };
	public int getZerks() {
		switch (bossType) {
		case Dragon:
			return DragonZerks[bossLevel];
		case Hydra:
			return HydraZerks[bossLevel];
		case Moloch:
			return MolochZerks[bossLevel];
		default:
			return -1;
		}
	}
}
