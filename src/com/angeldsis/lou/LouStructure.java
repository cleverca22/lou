package com.angeldsis.lou;

import com.angeldsis.LOU.CityBuilding;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.TextView;

public class LouStructure extends VisObject {
	String TAG = "LouStructure";
	TextView level;
	public LouStructure(Context context,CityBuilding base) {
		super(context);
		x = base.x;
		y = base.y;
		width = 128;
		height = 128;
		setFocusable(true);
		setFocusableInTouchMode(true);
		level = new TextView(context,null,android.R.attr.textAppearanceMedium);
		ViewGroup.LayoutParams layout = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
		addView(level,layout);
		level.setText("?");
		level.setBackgroundResource(R.drawable.building_level_display_bgr);
		
		if (base.typeid == 4) setBackgroundResource(R.drawable.building_cottage);
		else if (base.typeid == 47) setBackgroundResource(R.drawable.building_hut_new);
		else {
			int res = -1;
			switch (base.typeid) {
			case 5:
				res = R.drawable.building_market_place;
				break;
			case 7:
				res = R.drawable.building_lumber_mill;
				break;
			case 10:
				res = R.drawable.building_stonecutter;
				break;
			case 14:
				res = R.drawable.building_barracks;
				break;
			case 48:
				res = R.drawable.building_quarry_new;
				break;
			case 50:
				res = R.drawable.building_farm_new;
				break;
			case 297:
				res = R.drawable.wall_tower_ranger_t;
				break;
			case 553:
				res = R.drawable.wall_tower_ranger_b;
				break;
			case 550:
				res = R.drawable.wall_tower_lookout_b;
				break;
			case 809:
				res = R.drawable.wall_tower_ranger_l;
				break;
			}
			if (res == -1) {
				this.setBackgroundResource(R.drawable.building_stonecutter);
				Log.v(TAG,"unknown structure "+base.typeid);
				//level.setText(""+base.typeid);
			} else {
				setBackgroundResource(res);
			}
		}
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
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		level.layout(52, 100, 76, 128);
		//String crash = null;
		//Log.v(TAG,""+crash.length());
	}
	void setLevel(int level) {
		this.level.setText(""+level);
		//Log.v(TAG,"setting level to "+level);
		//this.level.invalidate();
	}
}
