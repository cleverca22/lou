package com.angeldsis.lou;

import android.content.Context;
import android.graphics.Canvas;

public class LouAnimation extends LouImage {
	int frames,width,height;
	LouAnimation(int res,int width,int height,int frames) {
		super(res,width*frames,height);
		this.frames = frames;
		this.height = height;
		this.width = width;
	}
	public LouAnimation(int res, int w, int h, int x, int y) {
		super(res,w*10,h);
		this.frames = 10;
		height = h;
		width = w;
		// FIXME use x/y offset
	}
	public void draw(Canvas c,Context context) {
		c.save();
		int offset = 0;
		c.clipRect(0, 0, width, height);
		c.translate(-offset, 0);
		getImage(context).draw(c);
		c.restore();
	}
}
