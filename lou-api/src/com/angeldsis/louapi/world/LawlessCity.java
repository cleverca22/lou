package com.angeldsis.louapi.world;

import com.angeldsis.louapi.world.WorldParser.MapItem;

public class LawlessCity extends MapItem {
	// 1==castle, 2==water, 4==abandoned-timeout, 8==ruin
	public int flags,points;
	public boolean canSettle() {
		return (flags & 4) == 0; // FIXME, verify this is correct
	}
}
