package com.angeldsis.lou;

import android.graphics.RectF;

public abstract class VisObject {
	public RectF rect;
	public LouImage[] images;
	public VisObject() {
	}
	abstract void addViews(CityLayout l);
	public void layout(float zoom){}
	abstract void dumpInfo();
	abstract String getType();
	abstract void selected();
}
