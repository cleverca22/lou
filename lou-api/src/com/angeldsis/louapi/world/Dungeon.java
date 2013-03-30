package com.angeldsis.louapi.world;

import com.angeldsis.louapi.data.Coord;

public class Dungeon implements Comparable {
	// slot and state combine to select the image, from dungeons[type].i[slot]
	public Coord location;
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
		case 4: return 8250;
		case 6: return 56850;
		case 7: return 117175;
		case 8: return 388593;
		case 9: return 294500;
		case 10: return 934310;
		}
		return 0;
	}
	private int maxloot() {
		switch (type) {
		case 3: // hill
			switch (level) {
			case 9: return 603048;
			}
		case 4: // mountain
			switch (level) {
			case 9: return 822160;
			}
		case 5: // forest
			switch (level) {
			case 9: return 887680;
			}
		}
		switch (level) {
		case 1: return 320;
		case 2: return 977;
		case 3: return 2000;
		case 4: return 15488;
		case 5: return 57330;
		case 6: return 113730;
		case 7: return 318920;
		case 8: return 555535;
		case 9: return 898590;
		case 10: return 1074520;
		}
		return 0;
	}
	public int getloot() {
		int min = minloot();
		int max = maxloot();
		if (min == 0) return max;
		int diff = max - min;
		return ((diff * progress)/100) + min;
	}
	@Override
	public int compareTo(Object arg0) {
		Dungeon other = (Dungeon) arg0;
		if (other.dist < dist) return 1;
		else if (other.dist > dist) return -1;
		return 0;
	}
}
