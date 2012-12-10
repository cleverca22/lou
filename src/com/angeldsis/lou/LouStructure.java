package com.angeldsis.lou;

import com.angeldsis.LOU.CityBuilding;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

public class LouStructure extends VisObject {
	String TAG = "LouStructure";
	TextView level;
	public LouStructure(Context context,CityBuilding base) {
		rect = new RectF(base.x,base.y,base.x+128,base.y+128);
		//setFocusable(true);
		//setFocusableInTouchMode(true);
		level = new TextView(context,null,android.R.attr.textAppearanceMedium);
		//ViewGroup.LayoutParams layout = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
		//addView(level,layout);
		level.setText("?");
		level.setBackgroundResource(R.drawable.building_level_display_bgr);
		
		int res = -1;
		switch (base.typeid) {
		case 4:
			res = R.drawable.building_cottage;
			break;
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
		case 47:
			res = R.drawable.building_hut_new;
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
			bg = context.getResources().getDrawable(R.drawable.building_stonecutter);
			Log.v(TAG,"unknown structure "+base.typeid);
			//level.setText(""+base.typeid);
		} else {
			bg = context.getResources().getDrawable(res);
		}
		bg.setBounds(0,0,128,128);
	}
/*	protected void onDraw(Canvas canvas){
		Log.v(TAG,"onDraw");
		Log.v(TAG,"done draw");
	}*/
	public boolean onTouchEvent(MotionEvent event) {
		Log.v(TAG,"Touch!");
		return false;
	}
	protected void onFocusChanged(boolean x, int y, Rect z) {
		Log.v(TAG,"focus changed ");
	}
	public void layout(int x,int y,float zoom) {
		// position of structure on screen, including scroll and scale
		int selfx = (int) ((x + rect.left) * zoom);
		int selfy = (int) ((y + rect.top) * zoom);
		// position of level within structure, including scale
		int left = (int) (52 * zoom);
		int top = (int) (100 * zoom);
		int right = (int) (76 * zoom);
		int bottom = (int) (128 * zoom);
		level.layout(selfx + left, selfy + top,
				selfx + right, selfy + bottom);
		//String crash = null;
		//Log.v(TAG,""+crash.length());
	}
	void setLevel(int level) {
		this.level.setText(""+level);
		//Log.v(TAG,"setting level to "+level);
		//this.level.invalidate();
	}
	@Override
	void addViews(CityLayout l) {
		l.addView(level);
	}
	@Override
	void dumpInfo() {
		// TODO Auto-generated method stub
		
	}
	String getType() {
		return "building";
	}
}
