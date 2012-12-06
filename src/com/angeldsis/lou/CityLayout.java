package com.angeldsis.lou;

import java.util.ArrayList;

import com.angeldsis.LOU.CityBuilding;
import com.angeldsis.LOU.LouState;

import android.content.Context;
import android.util.Log;
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
}
