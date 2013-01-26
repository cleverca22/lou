package com.angeldsis.louapi.world;

import com.angeldsis.louapi.data.BaseLou;

public class CityMapping {
	public int type,f,PalaceLevel,PalaceType,shortplayer,EnlightmentStep,PlunderProtection,
	PalaceDamage,Points;
	public boolean Castle,Water,Enlighted,PalaceUpgradeing;
	public String name;
	public int i;
	public CityMapping(int i, BaseLou y) throws Exception {
		this.i = i;
		type = 1;
		f = y.read4Bytes();
		Castle = ((f & 1) != 0);
		Water = ((f & 2) != 0);
		Enlighted = ((f & 4) != 0);
		PalaceUpgradeing = (f & 8) != 0;
		PalaceLevel = ((f >> 7) & 15);
		PalaceType = (((Enlighted | PalaceUpgradeing) | (PalaceLevel > 0)) ? (((f >> 11) & 7) + 1) : 0);
		shortplayer = (f >> 14) & 0x3ff;
		EnlightmentStep = 0;
		if (Enlighted) EnlightmentStep = y.readMultiBytes();
		PlunderProtection = 0;
		if ((f & 0x10) != 0) PlunderProtection = y.readMultiBytes();
		PalaceDamage = 0;
		if ((f & 0x20) != 0) PalaceDamage = y.read2Bytes();
		Points = y.readMultiBytes();
		name = y.readRest();
	}

}
