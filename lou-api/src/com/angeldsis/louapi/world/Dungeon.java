package com.angeldsis.louapi.world;

import com.angeldsis.louapi.LouState;
import com.angeldsis.louapi.world.WorldParser.MapItem;

public class Dungeon extends MapItem implements Comparable {
	// slot and state combine to select the image, from dungeons[type].i[slot]
	public int level,progress,type,startStep,slot;
	public boolean state;
	public double dist;
	public String getType() {
		switch (type) {
		case 3: return "hill dungeon";
		case 4: return "mountain dungeon";
		case 5: return "forest dungeon";
		default:
			return String.format("type%d",type);
		}
	}
	public int minloot() {
		switch (level) {
		case 1: return 150;
		case 2: return 600;
		case 3: return 2500;
		case 4: return 8500;
		case 5: return 32000;
		case 6: return 50000;
		case 7: return 120000;
		case 8: return 200000;
		case 9: return 350000;
		case 10: return 500000;
		}
		return 0;
	}
	public int getloot() {
		int min = minloot();
		if (type  == 4) min = (int) (((float)min) * 1.25);
		double mult = 1 + (0.015 * progress);
		return (int) (mult * min);
	}
	@Override
	public int compareTo(Object arg0) {
		Dungeon other = (Dungeon) arg0;
		if (other.dist < dist) return 1;
		else if (other.dist > dist) return -1;
		return 0;
	}
	public double getDistance() {
		return location.distance(stateObj.currentCity.location);
	}
	public double getTripsPerHour(float speed) {
		return 60/(getDistance() * speed * 2);
	}
	public double lootRate(float speed, int lootCapacity) {
		int loot = getloot();
		float maxtrips = (float)lootCapacity / (float)loot;
		if (maxtrips > 15) maxtrips = 15;
		return getTripsPerHour(speed) * loot * maxtrips;
	}
}
