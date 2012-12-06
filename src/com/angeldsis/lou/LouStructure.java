package com.angeldsis.lou;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.TextView;

public class LouStructure extends ViewGroup {
	String TAG = "LouStructure";
	TextView level;
	public LouStructure(Context context) {
		super(context);
		setFocusable(true);
		setFocusableInTouchMode(true);
		this.setBackgroundResource(R.drawable.building_stonecutter);
		level = new TextView(context,null,android.R.attr.textAppearanceMedium);
		ViewGroup.LayoutParams layout = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
		addView(level,layout);
		level.setText("10");
		level.setBackgroundResource(R.drawable.building_level_display_bgr);
	}
/*	protected void onDraw(Canvas canvas){
		Log.v(TAG,"onDraw");
		Log.v(TAG,"done draw");
	}*/
	public boolean onTouchEvent(MotionEvent event) {
		Log.v(TAG,"Touch!");
		this.invalidate();
		return false;
	}
	protected void onFocusChanged(boolean x, int y, Rect z) {
		Log.v(TAG,"focus changed ");
	}
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
		 setMeasuredDimension(128, 128);
	}
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		level.layout(52, 100, 76, 128);
	}
}
