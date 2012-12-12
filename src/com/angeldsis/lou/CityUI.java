package com.angeldsis.lou;

import com.angeldsis.LOU.LouState;

import android.util.Log;
import android.view.ViewGroup;

public class CityUI extends ViewGroup {
	static final String TAG = "CityUI";
	CityLayout mTest;
	public CityUI(LouSessionMain context, LouState state) {
		super(context);
		mTest = new CityLayout(context, state);
		addView(mTest);
		context.getSupportFragmentManager().beginTransaction().add(R.id.resource_bar, mTest.resource_bar).commit();
	}
	protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
		mTest.layout(0, 0, getWidth(), getHeight());
	}
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),MeasureSpec.getSize(heightMeasureSpec));
	}
	public void gotVisData() {
		mTest.gotVisData();
	}
	public void setZoom(float f) {
		mTest.setZoom(f);
	}
}
