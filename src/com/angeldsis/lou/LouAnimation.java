package com.angeldsis.lou;

import android.content.Context;
import android.graphics.Canvas;

public class LouAnimation extends LouImage {
	int frames,width,height;
	LouAnimation(Context c,int res,int width,int height,int frames) {
		super(c,res,width*frames,height);
		this.frames = frames;
		this.height = height;
		this.width = width;
	}
	public void draw(Canvas c) {
		c.save();
		int offset = 0;
		c.clipRect(0, 0, width, height);
		c.translate(-offset, 0);
		img.draw(c);
		c.restore();
	}
}
