package com.angeldsis.louapi.world;

import com.angeldsis.louapi.data.BaseLou;

public class PlayerMapping {
	public int shortid,id,Points,e,LOUState,shortAlliance,PeaceStart,PeaceDuration,CustomGfxId;
	public String name;
	public AllianceMapping allianceLink;
	public PlayerMapping(BaseLou y) throws Exception {
		shortid = y.read2Bytes(); // maps to Player in city data?
		id = y.readMultiBytes();
		Points = y.readMultiBytes();
		e = y.read3Bytes();
		LOUState = (e >> 2) & 3;
		shortAlliance = e >> 4;
		PeaceStart = 0;
		PeaceDuration = 0;
		if ((e & 1) != 0) {
			PeaceStart = y.readMultiBytes();
			PeaceDuration = y.readMultiBytes();
		}
		CustomGfxId = -1;
		if ((e & 2) != 0) {
			CustomGfxId = y.readMultiBytes();
		}
		name = y.readRest();
	}
}
