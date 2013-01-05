package com.angeldsis.lou;

import com.angeldsis.lou.fragments.ResourceBar;
import com.angeldsis.louapi.LouState;

import android.view.ViewGroup;

public class CityUI extends ViewGroup {
	static final String TAG = "CityUI";
	CityLayout mTest;
	ResourceBar resource_bar;
	public CityUI(CityView cityView) {
		super(cityView);
		resource_bar = new ResourceBar(cityView);
		mTest = new CityLayout(cityView);
		addView(mTest);
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
	public void gotCityData() {
		resource_bar.update(mTest.state.currentCity);
	}
	public void tick() {
		resource_bar.update(mTest.state.currentCity);
		mTest.tick();
	}
	public void setState(LouState state) {
		// TODO Auto-generated method stub
		mTest.setState(state);
	}
}
