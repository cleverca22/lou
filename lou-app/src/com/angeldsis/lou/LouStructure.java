package com.angeldsis.lou;

import com.angeldsis.louapi.CityBuilding;
import com.angeldsis.louapi.LouState;
import com.angeldsis.louapi.LouVisData.Hook;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.TextView;

public class LouStructure extends VisObject implements Hook {
	String TAG = "LouStructure";
	TextView level;
	ViewGroup progress1;
	CityBuilding base;
	LouState state;
	int typeid;
	public LouStructure(Context context,CityBuilding base,LouState state) {
		this.state = state;
		this.base = base;
		base.hook = this;
		rect = new RectF(base.x,base.y,base.x+128,base.y+128);
		//setFocusable(true);
		//setFocusableInTouchMode(true);
		level = new TextView(context,null,android.R.attr.textAppearanceMedium);
		//ViewGroup.LayoutParams layout = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
		//addView(level,layout);
		updated(); // sets level text
		level.setBackgroundResource(R.drawable.building_level_display_bgr);

		this.typeid = base.typeid;
		images = AndroidEnums.getStructureImage(base.typeid, base.level);
		if (images != null) return;
		else if (base.level == 0) Log.w(TAG,"using fallback code "+base.typeid+" "+base.level); // FIXME

		int res = -1;
		switch (base.typeid) {
		case CityBuilding.COTTAGE:
			res = R.drawable.building_cottage;
			break;
		case 5:
			res = R.drawable.building_market_place;
			break;
		case 7:
			res = R.drawable.building_lumber_mill;
			break;
		case 9:
			res = R.drawable.building_hideout;
			break;
		case 10:
			res = R.drawable.building_stonecutter;
			break;
		case 11:
			res = R.drawable.building_iron_furnace;
			break;
		case 12:
			res = R.drawable.building_townhall;
			break;
		case 13:
			res = R.drawable.building_townhouse;
			break;
		case 14:
			res = R.drawable.building_barracks;
			break;
		case 15:
			res = R.drawable.building_cityguard_house;
			break;
		case 16:
			res = R.drawable.building_casern;
			break;
		case 17:
			res = R.drawable.building_stables;
			break;
		case 20:
			res = R.drawable.building_storage;
			break;
		case 21:
			res = R.drawable.building_stronghold;
			break;
		case 36:
			res = R.drawable.building_mage_tower_large;
			break;
		case 37:
			res = R.drawable.building_temple;
			break;
		case 47:
			res = R.drawable.building_hut_new;
			break;
		case 48:
			res = R.drawable.building_quarry_new;
			break;
		case 49:
			res = R.drawable.building_ore_mine_new;
			break;
		case 50:
			res = R.drawable.building_farm_new;
			break;
		case 297: // 0x129
			res = R.drawable.wall_tower_ranger_t;
			break;
		case 553: // 0x229
			res = R.drawable.wall_tower_ranger_b;
			break;
		case 550:
			res = R.drawable.wall_tower_lookout_b;
			break;
		case 809: // 0x329
			res = R.drawable.wall_tower_ranger_l;
			break;
		case 1065: // 0x429
			res = R.drawable.wall_tower_ranger_r;
			break;
		}
		if (res == -1) {
			res = R.drawable.building_stonecutter;
			Log.v(TAG,"unknown structure "+base.typeid);
			level.setText(""+base.typeid);
			//level.setText(""+base.typeid);
		}
		images = new LouImage[1];
		images[0] = new LouImage(res,128,128);
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
	public void layout(float zoom) {
		//Log.v(TAG,"x:"+x+" y:"+y+" z:"+zoom);
		// position of structure on screen, including scroll and scale
		int selfx = (int) (rect.left * zoom);
		int selfy = (int) (rect.top * zoom);
		// position of level within structure, including scale
		int left = (int) (52 * zoom);
		int top = (int) (100 * zoom);
		int right = (int) (90 * zoom);
		int bottom = (int) (128 * zoom);
		int width2 = level.getMeasuredWidth();
		int height2 = level.getMeasuredHeight();
		if ((width2 > 0) && (height2 > 0)) {
			level.layout(selfx + left, selfy + top, selfx + left + width2, selfy + top + height2);
		} else {
			level.layout(selfx + left, selfy + top,
				selfx + right, selfy + bottom);
		}
		//String crash = null;
		//Log.v(TAG,""+crash.length());
	}
	/*void setLevel(int level) {
		this.level.setText(""+level);
		//Log.v(TAG,"setting level to "+level);
		//this.level.invalidate();
	}*/
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
	@Override
	public void updated() {
		if (base.s == 1) {
			level.setText("X");
			// FIXME progress bar
			Log.v(TAG,"time left:"+(base.se - state.getServerStep()));
		}
		else level.setText(""+base.level);
	}
	public void tick() {
		// FIXME, progress bar
		if (base.s == 1) {
			//if (base.ss > 0) Log.v(TAG,"time left:"+(base.se - state.getServerStep()));
			//else Log.v(TAG,"in queue");
		}
	}
	@Override
	void selected() {
		Log.v(TAG,"structure selected");
	}
	@Override
	public void delete(ViewGroup v) {
		v.removeView(level);
	}
	@Override
	void measure(int widthMeasureSpec, int heightMeasureSpec) {
		level.measure(widthMeasureSpec, heightMeasureSpec);
	}
	public void onStop() {
		if (base.hook == this) base.hook = null;
	}
}
