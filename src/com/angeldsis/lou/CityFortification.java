package com.angeldsis.lou;

import android.content.Context;
import android.util.Log;

import com.angeldsis.LOU.CityBuilding;

public class CityFortification extends VisObject {
	public CityFortification(Context context, CityBuilding base) {
		super(context);
		x = base.x;
		y = base.y;
		int res = -1;
		switch (base.typeid) {
		case 50:
		case 51:
		case 52:
		case 53:
			width = 384;
			height = 240;
			break;
		default:
			width = 128;
			height = 128;
		}
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
		if (res != -1) setBackgroundResource(res);
		else Log.v("CityFortification","made "+base.typeid);
	}
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
	}
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
		 setMeasuredDimension(width, height);
	}
}
