package com.angeldsis.lou;

import java.util.ArrayList;
import com.angeldsis.lou.fragments.ResourceBar;
import com.angeldsis.louapi.CityBuilding;
import com.angeldsis.louapi.CityResField;
import com.angeldsis.louapi.LouState;
import com.angeldsis.louapi.LouState.City;
import com.angeldsis.louapi.LouVisData;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;

public class CityLayout extends ViewGroup {
	static final String TAG = "CityLayout";
	ArrayList<VisObject> buildings;
	float zoom;
	Drawable dirt;
	LouState state;
	Context context;
	ResourceBar resource_bar;
	int maxx,maxy;
	public CityLayout(Activity context, LouState state) {
		super(context);
		this.state = state;
		this.context = context;
		resource_bar = new ResourceBar(context);
		zoom = 1;
		dirt = context.getResources().getDrawable(R.drawable.texture_bg_tile_big_city);
		dirt.setBounds(0, 0, 2944, 1840);
		// water.setBounds(0,0,896,560);
		buildings = new ArrayList<VisObject>();

		setHorizontalScrollBarEnabled(true);
		setVerticalScrollBarEnabled(true);
		//setScrollbarFadingEnabled(true);
		TypedArray a = context.obtainStyledAttributes(R.styleable.View);
		initializeScrollbars(a);
		a.recycle();
		setWillNotDraw(false);
		Log.v(TAG,"constructed");
		if (state.currentCity.visData.size() > 0) gotVisData();
	}
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		maxx = 2944 - (r - l);
		maxy = 1840 - (b - t);
		// FIXME, internal scroll!
		int x;
		//maxx = 2944;
		//maxy = 1840;
		for (x = 0; x < buildings.size(); x++) {
			VisObject y = buildings.get(x);
			y.layout(getScrollX(),getScrollY(),zoom);
		}
	}
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),MeasureSpec.getSize(heightMeasureSpec));
	}
	public void scrollTo(int x,int y) {
		if (x > maxx) x = maxx;
		else if (x < 0) x = 0;
		if (y > maxy) y = maxy;
		else if (y < 0) y = 0;
		super.scrollTo(x, y);
	}
	float lastx,lasty;
	public boolean onTouchEvent(MotionEvent event) {
		//Log.v(TAG,"motion "+event.getAction());
		switch (event.getAction()) {
		case 0: // down
			lastx = event.getX();
			lasty = event.getY();
			break;
		case 1: // up
		case 2: // move
			this.scrollBy((int) (lastx - event.getX()), (int) (lasty - event.getY()));
			awakenScrollBars(1000);
			lastx = event.getX();
			lasty = event.getY();
			break;
		}
		return true;
	}
	protected int computeHorizontalScrollRange() {
		return 2944;
	}
	protected int computeVerticalScrollRange() {
		return 1840;
	}
	protected int computeHorizontalScrollExtent() {
		return (int) (getWidth() / zoom);
	}
	protected int computeVerticalScrollExtent() {
		return (int) (getWidth() / zoom);
	}
	protected void onDraw(Canvas c) {
		//long start = System.currentTimeMillis();
		//int skipped = 0;
		c.save();
		c.scale(zoom, zoom);
		
		dirt.draw(c);
		
		int i,j;
		for (i = buildings.size() - 1; i >= 0; i--) {
			VisObject b = buildings.get(i);
			if (b.rect == null) Log.e(TAG,"rect isnt set on an instance of "+b.getType());
			if (c.quickReject(b.rect, Canvas.EdgeType.BW)) {
				//skipped++;
				//Log.v(TAG,"drawing "+b.getType());
				for (j = 0; j < b.images.length; j++ ) {
					b.images[j].expire();
				}
				continue;
			}
			c.save();
			c.translate(b.rect.left,b.rect.top);
			if (b.images == null) {
				b.dumpInfo();
			} else {
				for (j = 0; j < b.images.length; j++ ) {
					b.images[j].draw(c);
				}
			}
			c.restore();
		}
		c.restore();
		//long end = System.currentTimeMillis();
		//skipped = z;
		//mStats.setText(getStats());
		//Log.v(TAG,"stats: "+getStats(end-start,skipped));
	}
	String getStats(float lastRunTime, int skipped) {
		float fps = 1 / (lastRunTime / 1000);
		return "render time: "+lastRunTime+" fps:" + fps+" skip:"+skipped;
	}
	public void setZoom(float f) {
		zoom = f;
		this.invalidate();
		this.onLayout(false, 0, 0, 0, 0);
	}
	public void gotVisData() {
		//resource_bar.setLevels(12, 34, 56, 78);
		int x;
		City self = state.currentCity;
		for (x = 0; x < self.visData.size(); x++) {
			LouVisData current = self.visData.get(x);
			switch (current.type) {
			case 4:
				LouStructure vg = new LouStructure(context,(CityBuilding)current);
				vg.addViews(this);
				buildings.add(vg);
				vg.setLevel(((CityBuilding)current).level);
				break;
			case 9:
				ResFieldUI vg3 = new ResFieldUI(context,(CityResField)current);
				buildings.add(vg3);
				break;
			case 10:
				CityFortification vg2 = new CityFortification(context,(CityBuilding)current);
				vg2.addViews(this);
				buildings.add(vg2);
				break;
			}
		}
	}
}
