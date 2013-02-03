package com.angeldsis.louapi.world;

public class Dungeon implements Comparable {
	// slot and state combine to select the image, from dungeons[type].i[slot]
	public int col,row,level,progress,type,startStep,slot;
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
	@Override
	public int compareTo(Object arg0) {
		Dungeon other = (Dungeon) arg0;
		if (other.dist < dist) return 1;
		else if (other.dist > dist) return -1;
		return 0;
	}
}
