package com.angeldsis.lou;

import android.content.Context;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.angeldsis.louapi.CityBuilding;

public class CityFortification extends VisObject {
	int id2;
	String TAG = "CirtFortification";
	public CityFortification(Context context, CityBuilding base) {
		int width,height;
		int res = -1;
		id2 = base.typeid;
		//Log.v(TAG,"typeid is "+base.typeid);
		switch (base.typeid) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 50:
		case 51:
		case 52:
		case 53:
			width = 384;
			height = 240;
			break;
		case 15:
		case 18:
		case 21:
		case 57:
		case 68:
			width = 128;
			height = 80;
			break;
		case 19:
		case 20:
		case 22:
		case 23:
		case 69:
		case 70:
		case 71:
		case 72:
		case 73:
			width = 192;
			height = 80;
			break;
		case 13:
		case 14:
		case 16:
		case 17:
			width = 128;
			height = 120;
			break;
		case 24:
			width = 255;
			height = 128;
			break;
		case 25:
			width = 128;
			height = 256;
			break;
		default:
			width = 128;
			height = 128;
		}
		rect = new RectF(base.x,base.y,base.x+width,base.y+height);
		// from the cityfortification array
		switch (base.typeid) {
		case 0:
			res = R.drawable.palisade_gatehouse_bl;
			break;
		case 1:
			res = R.drawable.palisade_gatehouse_br;
			break;
		case 2:
			res = R.drawable.palisade_gatehouse_tl;
			break;
		case 3:
			res = R.drawable.palisade_gatehouse_tr;
			break;
		case 4:
			res = R.drawable.palisade_wall_t;
			break;
		case 5:
			res = R.drawable.palisade_wall_b;
			break;
		case 6:
			res = R.drawable.palisade_wall_l;
			break;
		case 7:
			res = R.drawable.palisade_wall_r;
			break;
		case 12:
			res = R.drawable.palisade_wall_tjunction_t;
			break;
		case 13:
			res = R.drawable.palisade_wall_tjunction_t_center;
			break;
		case 14:
			res = R.drawable.palisade_wall_tjunction_t_outside;
			break;
		case 15:
			res = R.drawable.palisade_wall_tjunction_b;
			break;
		case 16:
			res = R.drawable.palisade_wall_tjunction_b_center;
			break;
		case 17:
			res = R.drawable.palisade_wall_tjunction_b_outside;
			break;
		case 18:
			res = R.drawable.palisade_wall_tjunction_l;
			break;
		case 19:
			res = R.drawable.palisade_wall_tjunction_l_center;
			break;
		case 20:
			res = R.drawable.palisade_wall_tjunction_l_outside;
			break;
		case 21:
			res = R.drawable.palisade_wall_tjunction_r;
			break;
		case 22:
			res = R.drawable.palisade_wall_tjunction_r_center;
			break;
		case 23:
			res = R.drawable.palisade_wall_tjunction_r_outside;
			break;
		case 24:
			res = R.drawable.palisade_wall_tower_watercity_b;
			break;
		case 25:
			res = R.drawable.palisade_wall_tower_watercity_r;
			break;
		case 50:
			res = R.drawable.gatehouse_bl;
			break;
		case 51:
			res = R.drawable.gatehouse_br;
			break;
		case 52:
			res = R.drawable.gatehouse_tl;
			break;
		case 53:
			res = R.drawable.gatehouse_tr;
			break;
		case 54:
			res = R.drawable.wall_t;
			break;
		case 55:
			res = R.drawable.wall_b;
			break;
		case 56:
			res = R.drawable.wall_l;
			break;
		case 57:
			res = R.drawable.wall_r;
			break;
		case 62:
			res = R.drawable.wall_tjunction_t;
			break;
		case 63:
			res = R.drawable.wall_tjunction_t_center;
			break;
		case 64:
			res = R.drawable.wall_tjunction_t_outside;
			break;
		case 65:
			res = R.drawable.wall_tjunction_b;
			break;
		case 66:
			res = R.drawable.wall_tjunction_b_center;
			break;
		case 67:
			res = R.drawable.wall_tjunction_b_outside;
			break;
		case 68:
			res = R.drawable.wall_tjunction_l;
			break;
		case 69:
			res = R.drawable.wall_tjunction_l_center;
			break;
		case 70:
			res = R.drawable.wall_tjunction_l_outside;
			break;
		case 71:
			res = R.drawable.wall_tjunction_r;
			break;
		case 72:
			res = R.drawable.wall_tjunction_r_center;
			break;
		case 73:
			res = R.drawable.wall_tjunction_r_outside;
			break;
		default:
			Log.e(TAG,"unknown wall type "+base.typeid);
		}
		if (res != -1) {
			images = new LouImage[1];
			images[0] = new LouImage(context,res,width,height);
		}
		else Log.v("CityFortification","made "+base.typeid);
	}
	public boolean onTouchEvent(MotionEvent event) {
		Log.v("Wall","Touch! "+id2);
		return false;
	}
	void addViews(CityLayout l) {
		// TODO Auto-generated method stub
	}
	void dumpInfo() {
		// TODO Auto-generated method stub
	}
	String getType() {
		return "wall";
	}
	@Override
	void selected() {
		Log.v(TAG,"wall selected");
	}
	@Override
	void delete(ViewGroup v) {
		// TODO Auto-generated method stub
		
	}
	@Override
	void measure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		
	}
}
