package com.angeldsis.lou;

import com.angeldsis.louapi.CityResField;

import android.content.Context;
import android.graphics.RectF;
import android.util.Log;
import android.view.ViewGroup;

public class ResFieldUI extends VisObject {
	String TAG = "ResFieldUI";
	CityResField base;
	public ResFieldUI(CityResField base) {
		this.base = base;
		rect = new RectF(base.x,base.y,base.x+128,base.y+128);
		int imageid = -1;
		// refer to webfrontend.vis.CityResField.js
		switch (base.subid) {
		default: // FIXME
			Log.e(TAG,"unknown subtype "+base.subid);
		case 0:
			switch (base.typeid) {
			case 0:
			default: // FIXME
				imageid = R.drawable.stone_m_1x1_01;
				break;
			}
			break;
		case 1:
			switch (base.typeid) {
			case 0:
				imageid = R.drawable.forrest_mid_01;
				break;
			case 1:
				imageid = R.drawable.forrest_big_01;
				break;
			case 2:
				imageid = R.drawable.forrest_big_02;
				break;
			}
			break;
		case 2:
			switch (base.typeid) {
			case 0:
				imageid = R.drawable.iron_m_1x1_01;
				break;
			case 1:
				imageid = R.drawable.iron_r_1x1_02;
				break;
			case 2:
				imageid = R.drawable.iron_r_1x1_03;
				break;
			}
			break;
		case 3:
			switch (base.typeid) {
			case 0:
				imageid = R.drawable.animseq_lake_l_1x1_01;
				break;
			case 1:
				imageid = R.drawable.animseq_lake_m_1x1_01;
				break;
			case 2:
				imageid = R.drawable.animseq_lake_s_1x1_01;
				break;
			}
			break;
		case 6:
			imageid = R.drawable.iron_magic_resource;
			break;
		}
		images = new LouImage[1];
		switch (base.subid) {
		case 0:
		case 1:
		case 2:
		case 6:
			images[0] = new LouImage(imageid,150,128);
			break;
		case 3:
			// FIXME only renders 1st frame
			images[0] = new LouAnimation(imageid,128,90,15);
			break;
		default:
			Log.e(TAG,"image not configured right "+base.subid+" "+base.typeid);
		}
	}
	@Override
	void addViews(CityLayout l) {
		// TODO Auto-generated method stub
	}
	@Override
	void dumpInfo() {
		// TODO Auto-generated method stub
		Log.v(TAG,"image missing "+base.subid+" "+base.typeid);
	}
	String getType() {
		return "resource";
	}
	@Override
	void selected() {
		Log.v(TAG,"resource selected");
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
