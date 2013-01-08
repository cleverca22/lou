package com.angeldsis.lou;

import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public abstract class VisObject {
	public RectF rect;
	public LouImage[] images;
	public VisObject() {
	}
	abstract void addViews(CityLayout l);
	public void layout(int x, int y, float zoom){
	}
	abstract void dumpInfo();
	abstract String getType();
	abstract void selected();
}
