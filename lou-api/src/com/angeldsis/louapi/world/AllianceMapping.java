package com.angeldsis.louapi.world;

import com.angeldsis.louapi.data.BaseLou;

public class AllianceMapping {
	public int shortid,id,points;
	public String name;
	public AllianceMapping(BaseLou y) throws Exception {
		shortid = y.read2Bytes();
		id = y.readMultiBytes();
		points = y.readMultiBytes();
		name = y.readRest();
	}
}
