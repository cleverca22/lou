package com.angeldsis.lou;

import java.util.ArrayList;

import com.angeldsis.LOU.CityBuilding;
import com.angeldsis.LOU.LouState;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;

public class CityLayout extends ViewGroup {
	String TAG = "CityLayout";
	ArrayList<VisObject> buildings;
	public CityLayout(Context context, LouState state) {
		super(context);
		this.setBackgroundResource(R.drawable.texture_bg_tile_big_city);
		buildings = new ArrayList<VisObject>();
		int x;
		for (x = 0; x < state.visData.size(); x++) {
			switch (state.visData.get(x).type) {
			case 4:
				LouStructure vg = new LouStructure(context,(CityBuilding)state.visData.get(x));
				addView(vg);
				buildings.add(vg);
				vg.setLevel(((CityBuilding)state.visData.get(x)).level);
				break;
			case 10:
				CityFortification vg2 = new CityFortification(context,(CityBuilding)state.visData.get(x));
				addView(vg2);
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
		int xoffset = 0;
		int yoffset = 0;
		int x;
		for (x = 0; x < buildings.size(); x++) {
			VisObject y = buildings.get(x);
			//Log.v("CityLayout","building #"+x+" is at "+y.x+","+y.y);
			int realx = y.x - xoffset;
			int realy = y.y - yoffset;
			y.layout(realx, realy, realx+y.width, realy+y.height);
		}
	}
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),MeasureSpec.getSize(heightMeasureSpec));
	}
	public void up() {
		scrollBy(0,-50);
		awakenScrollBars();
	}
	public void down() {
		scrollBy(0,50);
		awakenScrollBars();
	}
	public void left() {
		scrollBy(-50,0);
		awakenScrollBars();
	}
	public void right () {
		scrollBy(50,0);
		awakenScrollBars();
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
		Log.v(TAG,"x "+getScrollX()+" y "+getScrollY());
		return 1600;
	}
}
