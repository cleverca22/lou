package com.angeldsis.lou;

import java.util.ArrayList;

import com.angeldsis.LOU.CityBuilding;
import com.angeldsis.LOU.LouState;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;

public class CityLayout extends ViewGroup {
	String TAG = "CityLayout";
	ArrayList<VisObject> buildings;
	float zoom;
	Drawable dirt;
	long lastRunTime;
	int skipped;
	public CityLayout(Context context, LouState state) {
		super(context);
		zoom = 1;
		dirt = context.getResources().getDrawable(R.drawable.texture_bg_tile_big_city);
		dirt.setBounds(0, 0, dirt.getIntrinsicWidth(), dirt.getIntrinsicHeight());
		buildings = new ArrayList<VisObject>();
		int x;
		for (x = 0; x < state.visData.size(); x++) {
			switch (state.visData.get(x).type) {
			case 4:
				LouStructure vg = new LouStructure(context,(CityBuilding)state.visData.get(x));
				vg.addViews(this);
				buildings.add(vg);
				vg.setLevel(((CityBuilding)state.visData.get(x)).level);
				break;
			case 10:
				CityFortification vg2 = new CityFortification(context,(CityBuilding)state.visData.get(x));
				vg2.addViews(this);
				buildings.add(vg2);
				break;
			}
		}
		setHorizontalScrollBarEnabled(true);
		setVerticalScrollBarEnabled(true);
		//setScrollbarFadingEnabled(true);
		TypedArray a = context.obtainStyledAttributes(R.styleable.View);
		initializeScrollbars(a);
		a.recycle();
		setWillNotDraw(false);
	}
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// FIXME, internal scroll!
		int x;
		for (x = 0; x < buildings.size(); x++) {
			VisObject y = buildings.get(x);
			y.layout(getScrollX(),getScrollY(),zoom);
		}
	}
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),MeasureSpec.getSize(heightMeasureSpec));
	}
	public void up() {
		scrollBy(0,-50);
		awakenScrollBars(1000);
	}
	public void down() {
		scrollBy(0,50);
		awakenScrollBars(1000);
	}
	public void left() {
		scrollBy(-50,0);
		awakenScrollBars(1000);
	}
	public void right () {
		scrollBy(50,0);
		awakenScrollBars(1000);
	}
	public void scrollTo(int x,int y) {
		if (x > 2650) x = 2650;
		if (y > 1600) y = 1600;
		super.scrollTo(x, y);
	}
	public boolean onTouchEvent(MotionEvent event) {
		Log.v(TAG,"motion "+event.getAction());
		return false;
	}
	protected int computeHorizontalScrollRange() {
		return 2650;
	}
	protected int computeVerticalScrollRange() {
		return 1600;
	}
	protected int computeHorizontalScrollExtent() {
		return (int) (getWidth() / zoom);
	}
	protected int computeVerticalScrollExtent() {
		return (int) (getWidth() / zoom);
	}
	protected void onDraw(Canvas c) {
		long start = System.currentTimeMillis();
		int i,z=0;
		c.save();
		c.scale(zoom, zoom);
		
		c.save();
		c.scale(1.5f,1.5f);
		dirt.draw(c);
		c.restore();
		
		for (i = 0; i < buildings.size(); i++) {
			VisObject b = buildings.get(i);
			if (c.quickReject(b.rect, Canvas.EdgeType.BW)) {
				z++;
				continue;
			}
			c.save();
			c.translate(b.rect.left,b.rect.top);
			b.bg.draw(c);
			c.restore();
		}
		c.restore();
		long end = System.currentTimeMillis();
		lastRunTime = end - start;
		skipped = z;
		//mStats.setText(getStats());
		Log.v(TAG,"stats: "+getStats());
	}
	String getStats() {
		float fps = 1 / (((float)lastRunTime) / 1000);
		return "fps:" + fps+" skip:"+skipped;
	}
	public void setZoom(float f) {
		zoom = f;
		this.invalidate();
		this.onLayout(false, 0, 0, 0, 0);
	}
}
