package com.angeldsis.lou;

import android.content.Context;
import android.graphics.Canvas;

public class LouAnimation extends LouImage {
	int frames,realwidth;
	int x,y;
	int hack = 0;
	LouAnimation(int res,int width,int height,int frames) {
		super(res,width*frames,height);
		this.frames = frames;
		realwidth = width;
		x=y=0;
	}
	public LouAnimation(int res, int w, int h, int x, int y) {
		super(res,w*17,h);
		this.frames = 17;
		realwidth = w;
		this.x = x;
		this.y = y;
	}
	public void draw(Canvas c,Context context) {
		c.save();
		int offset = hack++ * realwidth;
		if (hack == 16) hack = 0;
		c.translate(x, y);
		c.clipRect(0, 0, realwidth, height);
		c.translate(-offset, 0);
		getImage(context).draw(c);
		c.restore();
	}
}
