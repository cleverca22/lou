package com.angeldsis.louapi.world;

public class WorldCellRequest {
	public int row,col,version;
	public WorldCellRequest(int g, int h, int ver) {
		row = g;
		col = h;
		version = ver;
	}
}
