package com.angeldsis.lou;

import java.util.ArrayList;
import java.util.Iterator;

import org.json2.JSONException;
import org.json2.JSONObject;

import com.angeldsis.louapi.CityBuilding;
import com.angeldsis.louapi.CityResField;
import com.angeldsis.louapi.LouState;
import com.angeldsis.louapi.LouState.City;
import com.angeldsis.louapi.LouVisData;
import com.angeldsis.louapi.RPC;
import com.angeldsis.louapi.RPC.RPCDone;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.ViewGroup;

public class CityLayout extends ViewGroup implements OnScaleGestureListener, OnGestureListener {
	static final String TAG = "CityLayout";
	ArrayList<VisObject> buildings;
	float zoom;
	Drawable dirt, selection, hammer;
	RectF newBuilding;
	VisObject selected;
	LouState state;
	Context context;
	int maxx,maxy;
	ScaleGestureDetector sgd;
	GestureDetector gd;
	Handler h = new Handler();
	private RPC rpc;
	LayoutCallbacks callbacks;
	public CityLayout(CityView context) {
		super(context);
		callbacks = context;
		this.context = context;
		sgd = new ScaleGestureDetector(context,this);
		gd = new GestureDetector(context,this);
		zoom = 1;
		dirt = context.getResources().getDrawable(R.drawable.texture_bg_tile_big_city);
		dirt.setBounds(0, 0, 2944, 1840);
		selection = context.getResources().getDrawable(R.drawable.decal_select_building);
		selection.setBounds(-25, 15, 178 - 25, 144 + 15);
		hammer = context.getResources().getDrawable(R.drawable.decal_building_valid);
		hammer.setBounds(-25, 0, 178 - 25, 114);
		selected = null;
		// water.setBounds(0,0,896,560);
		buildings = new ArrayList<VisObject>();

		setHorizontalScrollBarEnabled(true);
		setVerticalScrollBarEnabled(true);
		//setScrollbarFadingEnabled(true);
		TypedArray a = context.obtainStyledAttributes(R.styleable.View);
		initializeScrollbars(a);
		a.recycle();
		setWillNotDraw(false);
		Log.v(TAG,"constructed");
	}
	public void setState(LouState state2, RPC rpc) {
		this.state = state2;
		this.rpc = rpc;
		City c = state.currentCity;
		if ((c != null) && c.hasVisData()) gotVisData();
	}
	void adjustMax() {
		maxx = (int) ((2944*zoom) - getWidth());
		maxy = (int) ((1840*zoom) - getHeight());
		if (maxx < 0) maxx = 0;
		if (maxy < 0) maxy = 0;
	}
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Log.v(TAG,"onLayout");
		adjustMax();
		int x;
		for (x = 0; x < buildings.size(); x++) {
			VisObject y = buildings.get(x);
			y.layout(zoom);
		}
	}
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
		Log.v(TAG,"onMeasure");
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),MeasureSpec.getSize(heightMeasureSpec));
	}
	public void scrollTo(int x,int y) {
		if (x > maxx) x = maxx;
		else if (x < 0) x = 0;
		if (y > maxy) y = maxy;
		else if (y < 0) y = 0;
		super.scrollTo(x, y);
	}
	float lastx,lasty;
	public boolean onTouchEvent(MotionEvent event) {
		sgd.onTouchEvent(event);
		if (sgd.isInProgress()) return true;
		gd.onTouchEvent(event);
		//Log.v(TAG,"motion "+event.getAction());
		switch (event.getAction()) {
		case 0: // down
			lastx = event.getX();
			lasty = event.getY();
			break;
		case 1: // up
		case 2: // move
			int xdiff = (int) (lastx - event.getX());
			int ydiff = (int) (lasty - event.getY());
			if ((xdiff > 10) || (ydiff > 10) || (xdiff < -10) || (ydiff < -10)) {
				scrollBy(xdiff, ydiff);
				awakenScrollBars(1000);
				lastx = event.getX();
				lasty = event.getY();
			}
			break;
		}
		return true;
	}
	protected int computeHorizontalScrollRange() {
		return (int) (2944 * zoom);
	}
	protected int computeVerticalScrollRange() {
		return (int) (1840 * zoom);
	}
	protected int computeHorizontalScrollExtent() {
		return (int) (getWidth() / zoom);
	}
	protected int computeVerticalScrollExtent() {
		return (int) (getWidth() / zoom);
	}
	protected void onDraw(Canvas c) {
		//long start = System.currentTimeMillis();
		//int skipped = 0;
		c.save();
		c.scale(zoom, zoom);
		
		dirt.draw(c);
		
		if (selected != null) {
			c.save();
			c.translate(selected.rect.left,selected.rect.top);
			selection.draw(c);
			c.restore();
		}
		if (newBuilding != null) {
			if (!c.quickReject(newBuilding, Canvas.EdgeType.BW)) {
				c.save();
				c.translate(newBuilding.left, newBuilding.top);
				hammer.draw(c);
				c.restore();
			}
		}
		
		int i,j;
		for (i = buildings.size() - 1; i >= 0; i--) {
			VisObject b = buildings.get(i);
			if (b.rect == null) Log.e(TAG,"rect isnt set on an instance of "+b.getType());
			if (c.quickReject(b.rect, Canvas.EdgeType.BW)) {
				//skipped++;
				//Log.v(TAG,"drawing "+b.getType());
				for (j = 0; j < b.images.length; j++ ) {
					//Log.v(TAG,"images:"+b.images);
					b.images[j].expire();
				}
				continue;
			}
			c.save();
			c.translate(b.rect.left,b.rect.top);
			if (b.images == null) {
				b.dumpInfo();
			} else {
				for (j = 0; j < b.images.length; j++ ) {
					b.images[j].draw(c);
				}
			}
			c.restore();
		}
		c.restore();
		//long end = System.currentTimeMillis();
		//skipped = z;
		//mStats.setText(getStats());
		//Log.v(TAG,"stats: "+getStats(end-start,skipped));
	}
	String getStats(float lastRunTime, int skipped) {
		float fps = 1 / (lastRunTime / 1000);
		return "render time: "+lastRunTime+" fps:" + fps+" skip:"+skipped;
	}
	public void setZoom(float f) {
		scrollTo((int)(getScrollX() * (f/zoom)),(int)(getScrollY() * (f/zoom)));
		zoom = f;
		adjustMax();
		invalidate();
		onLayout(false, 0, 0, 0, 0);
	}
	public void gotVisData() {
		int x;
		City self = state.currentCity;
		for (x = 0; x < self.visData.size(); x++) {
			LouVisData current = self.visData.get(x);
			switch (current.type) {
			case 4:
				LouStructure vg = new LouStructure(context,(CityBuilding)current,state);
				vg.addViews(this);
				buildings.add(vg);
				break;
			case 9:
				ResFieldUI vg3 = new ResFieldUI(context,(CityResField)current);
				buildings.add(vg3);
				break;
			case 10:
				CityFortification vg2 = new CityFortification(context,(CityBuilding)current);
				vg2.addViews(this);
				buildings.add(vg2);
				break;
			}
		}
		onLayout(false, 0, 0, 0, 0);
		requestLayout();
	}
	void onResume() {
		updateAll();
	}
	void updateAll() {
		Iterator<VisObject> i = buildings.iterator();
		while (i.hasNext()) {
			VisObject v = i.next();
			if (v instanceof LouStructure) ((LouStructure)v).updated();
		}
	}
	public void tick() {
		Iterator<VisObject> i = buildings.iterator();
		while (i.hasNext()) {
			VisObject v = i.next();
			if (v instanceof LouStructure) ((LouStructure)v).tick();
		}		
	}
	@Override
	public boolean onScale(ScaleGestureDetector arg0) {
		float x = arg0.getFocusX();
		float y = arg0.getFocusY();
		scrollBy((int) (lastx - x), (int) (lasty - y));
		awakenScrollBars(1000);
		lastx = x;
		lasty = y;
		invalidate();

		float change = arg0.getScaleFactor();
		if ((change > 0.98) && (change < 1.02)) return false;
		//Log.v(TAG,"onScale "+change+" x:"+x+" y:"+y);

		// the diff between viewpoint(0,0) and focus
		float x2 = x * zoom;
		float y2 = y * zoom;
		
		setZoom(zoom * change);
		
		float x3 = (x2 / zoom);
		float y3 = (y2 / zoom);
		
		int xdiff = (int) (x3 - x);
		int ydiff = (int) (y3 - y);
		Log.v(TAG,"x:"+x+" y:"+y+" x2:"+x2+" y2:"+y2+" x3:"+x3+" y3:"+y3+" xdiff:"+xdiff+" ydiff:"+ydiff);
		scrollBy(-xdiff,-ydiff);

		/*float ncX = arg0.getFocusX() * zoom;
		float Nw = getWidth() * zoom;
		float tX = ncX-(Nw/2);
		Log.v(TAG,"tX:"+tX+" Nw:"+Nw);
		scrollBy((int)tX,0);*/
		
		//scrollBy(-(int)(x / getWidth()), -(int) (y / getHeight()));
		return true;
	}
	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		Log.v(TAG,"onScaleBegin");
		lastx = detector.getFocusX();
		lasty = detector.getFocusY();
		return true;
	}
	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
		Log.v(TAG,"onScaleEnd");
		lastx = detector.getFocusX();
		lasty = detector.getFocusY();
	}
	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}
	Runnable lastevent = null;
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		if (lastevent != null) {
			h.removeCallbacks(lastevent);
			lastevent = null;
		}
		Log.v(TAG,"onSingleTapUp scrollx:"+getScrollX()+" scrolly:"+getScrollY()+" zoom:"+zoom+" width:"+getWidth()+" height:"+getHeight()+" maxx:"+maxx+" maxy:"+maxy);
		int x = (int) ((getScrollX() + e.getX()) / zoom);
		int y = (int) ((getScrollY() + e.getY()) / zoom);
		Log.v(TAG,"x:"+x+" y:"+y);
		Iterator<VisObject> i = buildings.iterator();
		while (i.hasNext()) {
			VisObject o = i.next();
			if (!o.rect.contains(x, y)) continue;
			selected = o;
			x -= x % 128;
			y -= y % 80;
			int row = (y/80)+512;
			int col = x/128;
			currentCoord = (row * 256) + col;
			o.selected();
			invalidate();
			callbacks.showUpgradeMenu(true);
			callbacks.showBuildMenu(false);
			if (o instanceof LouStructure) {
				final LouStructure s = (LouStructure)o;
				lastevent = new Runnable() {
					@Override
					public void run() {
						rpc.GetBuildingInfo(s.base, new RPCDone() {
							@Override
							public void requestDone(JSONObject reply) {
								try {
									Log.v(TAG,reply.toString(1));
									// reply.i == 20 (warehouse)
									// reply.ml[0].m, array of how much of each resource it can hold
									// reply.ml[1].m resources for next level
									// reply.t == upgrade time
									// reply.demo.t == domo time
									// reply.down.t == downgrade time
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						});
					}
				};
				h.postDelayed(lastevent, 5000);
			}
			newBuilding = null;
			return true;
		}
		// grid units: x=128, y=80
		x -= x % 128;
		y -= y % 80;
		makeMenu(x,y);
		invalidate();
		return false;
	}
	int currentCoord;
	void makeMenu(int x, int y) {
		newBuilding = new RectF(x,y,128,80);
		selected = null;
		int row = (y/80)+512;
		int col = x/128;
		currentCoord = (row * 256) + col;
		Log.v(TAG,"GetUpgradeInfo("+((y/80)+512)+","+(x/128)+","+currentCoord+")");
		callbacks.showBuildMenu(true);
		callbacks.showUpgradeMenu(false);
	}
	public interface LayoutCallbacks {
		void showBuildMenu(boolean enabled);
		void showUpgradeMenu(boolean b);
	}
}
