package com.angeldsis.lou;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public class LouImage {
	Drawable img;
	LouImage(Context c,int resource,int width,int height) {
		img = c.getResources().getDrawable(resource);
		img.setBounds(0, 0, width, height);
	}
	public void draw(Canvas c) {
		img.draw(c);
	}
}
