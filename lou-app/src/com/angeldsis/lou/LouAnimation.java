package com.angeldsis.lou;

import android.content.Context;
import android.graphics.Canvas;

public class LouAnimation extends LouImage {
	int frames,width,height;
	int x,y;
	int hack = 0;
	LouAnimation(int res,int width,int height,int frames) {
		super(res,width*frames,height);
		this.frames = frames;
		this.height = height;
		this.width = width;
		x=y=0;
	}
	public LouAnimation(int res, int w, int h, int x, int y) {
		super(res,w*17,h);
		this.frames = 17;
		height = h;
		width = w;
		this.x = x;
		this.y = y;
	}
	public void draw(Canvas c,Context context) {
		c.save();
		int offset = hack++ * width;
		if (hack == 16) hack = 0;
		c.translate(x, y);// FIXME, isnt offsetting enough
		c.clipRect(0, 0, width, height);
		c.translate(-offset, 0);
		getImage(context).draw(c);
		c.restore();
	}
}
