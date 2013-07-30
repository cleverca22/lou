package com.angeldsis.lou;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class MyTableRow extends LinearLayout {
	private static final String TAG = "MyTableRow";
	LayoutParameters params;
	public static class LayoutParameters {
		int[] widths = new int[15];
		private int getWidth(int i, int width) {
			if (width > widths[i]) widths[i] = width;
			//Log.v(TAG,"width: "+widths[i]);
			return widths[i] + 10;
		}}
	public MyTableRow(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setClickable(false); // FIXME, use attribute set?
	}
	public void bind(LayoutParameters grid) {
		params = grid;
	}
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		//Log.v(TAG,"onMeasure "+MeasureSpec.toString(widthMeasureSpec)+" "+MeasureSpec.toString(heightMeasureSpec));
		int availwidth = MeasureSpec.getSize(widthMeasureSpec);
		int usedwidth = 0;
		int highest = 0;
		
		int max = getChildCount();
		int i;
		for (i=0; i<max; i++) {
			View child = getChildAt(i);
			child.measure(availwidth, heightMeasureSpec);
			int height = child.getMeasuredHeight();
			int width = child.getMeasuredWidth();
			if (params != null) width = params.getWidth(i,width);
			usedwidth += width;
			if (height > highest) highest = height;
		}
		int targetwidth = MeasureSpec.makeMeasureSpec(usedwidth, MeasureSpec.UNSPECIFIED);
		setMeasuredDimension(targetwidth,highest);
	}
	@Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int max = getChildCount();
		int i;
		int widthused = 0;
		for (i=0; i<max; i++) {
			View child = getChildAt(i);
			int width = child.getMeasuredWidth();
			//Log.v(TAG,String.format("%d %d %d %d",widthused,0,width,child.getMeasuredHeight()));
			child.layout(widthused, 0, widthused + width, child.getMeasuredHeight());

			if (params != null) width = params.getWidth(i,width);
			widthused += width;
		}
	}
}
