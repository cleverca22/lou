package com.angeldsis.lou;

import android.content.Context;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

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
		case 50:
		case 51:
		case 52:
		case 53:
			width = 384;
			height = 240;
			break;
		case 57:
		case 68:
			width = 128;
			height = 80;
			break;
		case 69:
		case 70:
		case 71:
		case 72:
		case 73:
			width = 192;
			height = 80;
			break;
		default:
			width = 128;
			height = 128;
		}
		rect = new RectF(base.x,base.y,base.x+width,base.y+height);
		switch (base.typeid) {
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
}
