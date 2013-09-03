package com.angeldsis.lou;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class FragmentSplitter extends LinearLayout {
	private static final String TAG = "FragmentSplitter";
	public FragmentSplitter(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public FragmentSplitter(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public FragmentSplitter(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int max = getChildCount();
		if (max != 2) throw new IllegalStateException("this view must have 2 children");
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		int orientation = this.getOrientation();
		ViewGroup secondary = (ViewGroup) getChildAt(0);
		ViewGroup main = (ViewGroup) getChildAt(1);
		
		if (secondary.getChildCount() == 0) {
			main.measure(widthMeasureSpec, heightMeasureSpec);
		} else {
			if (orientation == HORIZONTAL) {
				secondary.measure(MeasureSpec.makeMeasureSpec(width/2,MeasureSpec.EXACTLY), heightMeasureSpec);
				main.measure(MeasureSpec.makeMeasureSpec(width/2,MeasureSpec.EXACTLY), heightMeasureSpec);
			} else {
				secondary.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height/2,MeasureSpec.EXACTLY));
				main.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height/2,MeasureSpec.EXACTLY));
			}
		}
		setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);
	}
	@Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
		//Log.v(TAG,String.format("%d %d %d %d",l,t,r,b));
		int width = r - l;
		int height = b - t;
		int orientation = this.getOrientation();
		ViewGroup secondary = (ViewGroup) getChildAt(0);
		View main = getChildAt(1);
		
		if (secondary.getChildCount() == 0) {
			main.layout(0, 0, width, height);
		} else {
			if (orientation == HORIZONTAL) {
				secondary.layout(0, 0, width/2, height);
				main.layout(width/2, 0, width,height);
			} else {
				secondary.layout(0, 0, r, b/2);
				main.layout(0, b/2, r, b);
			}
		}
	}
}
