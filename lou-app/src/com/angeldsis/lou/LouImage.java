package com.angeldsis.lou;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public class LouImage {
	int imageId,width,height;
	Drawable image_cache;
	LouImage(int resource,int width,int height) {
		imageId = resource;
		this.width = width;
		this.height = height;
	}
	Drawable getImage(Context context) {
		if (image_cache != null) return image_cache;
		Drawable img;
		img = context.getResources().getDrawable(imageId);
		img.setBounds(0, 0, width, height);
		image_cache = img;
		return img;
	}
	void expire() {
		image_cache = null;
	}
	public void draw(Canvas c,Context context) {
		getImage(context).draw(c);
	}
}
