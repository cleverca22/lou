package com.angeldsis.lou;

import com.angeldsis.LOU.CityResField;

import android.content.Context;
import android.graphics.RectF;
import android.util.Log;

public class ResFieldUI extends VisObject {
	String TAG = "ResFieldUI";
	public ResFieldUI(Context context, CityResField base) {
		rect = new RectF(base.x,base.y,base.x+128,base.y+128);
		int imageid = -1;
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
		}
		bg = context.getResources().getDrawable(imageid);
		switch (base.subid) {
		case 0:
		case 1:
		case 2:
			bg.setBounds(0,0,150,128);
			break;
		case 3:
			// FIXME ignores animation
			bg.setBounds(0,0,128,90);
			break;
		}
	}
	@Override
	void addViews(CityLayout l) {
		// TODO Auto-generated method stub
	}
	@Override
	void dumpInfo() {
		// TODO Auto-generated method stub
		Log.v(TAG,"image missing");
	}
	String getType() {
		return "resource";
	}
}
