package com.angeldsis.lou;

import android.graphics.RectF;

class StructureId {
	int row;
	int col;
	public StructureId(int col, int row) {
		this.row = row;
		this.col = col;
	}
	static StructureId fromXY(int x,int y) {
		x -= x % 128;
		y -= y % 80;
		return new StructureId(x/128,y/80);
	}
	int toCoord() {
		return ((row+512) * 256) + col;
	}
	RectF toRectF() {
		return new RectF(col*128,row*80,(col*128)+128,(row*80)+80);
	}
	public StructureId down() {
		if (row == 22) return null;
		return new StructureId(col,row+1);
	}
	public StructureId up() {
		if (row == 0) return null;
		return new StructureId(col,row-1);
	}
	public StructureId right() {
		if (col == 22) return null;
		return new StructureId(col+1,row);
	}
	public StructureId left() {
		if (col == 0) return null;
		return new StructureId(col-1,row);
	}
	public String toString() {
		return String.format("StructureId(%d,%d)",col,row);
	}
}