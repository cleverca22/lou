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
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
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
	StructureId currentCoord;
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
		setFocusable(true);
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
		//Log.v(TAG,"onLayout");
		adjustMax();
		int x;
		for (x = 0; x < buildings.size(); x++) {
			VisObject y = buildings.get(x);
			y.layout(zoom);
		}
	}
	@Override protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
		Log.v(TAG,"onMeasure");
		setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),MeasureSpec.getSize(heightMeasureSpec));
		for (VisObject v : buildings) {
			// FIXME, give the width/height of the structure, at current zoom
			int maxwidth = View.MeasureSpec.makeMeasureSpec(100, View.MeasureSpec.AT_MOST);
			int maxheight = View.MeasureSpec.makeMeasureSpec(100, View.MeasureSpec.AT_MOST);
			v.measure(maxwidth,maxheight);
		}
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
		/*if (newBuilding != null) {
			if (!c.quickReject(newBuilding, Canvas.EdgeType.BW)) {
				c.save();
				c.translate(newBuilding.left, newBuilding.top);
				hammer.draw(c);
				c.restore();
			}
		}*/
		if (currentCoord != null) {
			c.save();
			RectF focus = currentCoord.toRectF();
			c.translate(focus.left,focus.top);
			coord_type t = getCoordType(currentCoord);
			if (t == coord_type.empty) hammer.draw(c);
			else selection.draw(c);
			c.restore();
		}
		
		int i,j;
		for (i = buildings.size() - 1; i >= 0; i--) {
			VisObject b = buildings.get(i);
			if (b.rect == null) Log.e(TAG,"rect isnt set on an instance of "+b.getType());
			if (c.quickReject(b.rect, Canvas.EdgeType.BW)) {
				//skipped++;
				//Log.v(TAG,"drawing "+b.getType());
				if (b.images[0] == null) b.dumpInfo();
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
					b.images[j].draw(c,context);
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
		City self = state.currentCity;
		LouVisData[] changes = self.visData.toArray(new LouVisData[self.visData.size()]);
		onVisObjAdded(changes,false);
		onLayout(false, 0, 0, 0, 0);
		requestLayout();
	}
	public void visDataReset() {
		Iterator<VisObject> i = buildings.iterator();
		while (i.hasNext()) {
			VisObject v = i.next();
			v.delete(this);
			i.remove();
		}
	}
	/** called when a new LouVisData is added by api, or at startup, on the array of them
	 * @param v
	 */
	public void onVisObjAdded(LouVisData[] input, boolean doLayout) {
		for (LouVisData v : input) {
			if (v == null) continue;
			switch (v.type) {
			case 4:
				LouStructure vg = new LouStructure(context,(CityBuilding)v,state);
				vg.addViews(this);
				buildings.add(vg);
				break;
			case 9:
				ResFieldUI vg3 = new ResFieldUI((CityResField)v);
				buildings.add(vg3);
				break;
			case 10:
				CityFortification vg2 = new CityFortification((CityBuilding)v);
				vg2.addViews(this);
				buildings.add(vg2);
				break;
			}
			if (doLayout) {
				onLayout(false, 0, 0, 0, 0);
				requestLayout();
			}
		}
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
		//Log.v(TAG,"x:"+x+" y:"+y+" x2:"+x2+" y2:"+y2+" x3:"+x3+" y3:"+y3+" xdiff:"+xdiff+" ydiff:"+ydiff);
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
			currentCoord = StructureId.fromXY(x, y);
			o.selected();
			invalidate();
			requestFocusFromTouch();
			if (o instanceof LouStructure) {
				final LouStructure s = (LouStructure)o;
				if (s.base.level < 10) {
					callbacks.showUpgradeMenu(true);
					callbacks.showBuildMenu(false);
					callbacks.showClear(true);
				} else {
					callbacks.showUpgradeMenu(false);
					callbacks.showBuildMenu(false);
					callbacks.showClear(true);
				}
				lastevent = new Runnable() {
					@Override
					public void run() {
						/*rpc.GetBuildingInfo(s.base, new RPCDone() {
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
						})*/;
					}
				};
				h.postDelayed(lastevent, 5000);
			}
			newBuilding = null;
			return true;
		}
		// grid units: x=128, y=80
		makeMenu(x,y);
		return false;
	}
	void makeMenu(int x, int y) {
		selectCoord(StructureId.fromXY(x, y));
		selected = null;
		Log.v(TAG,"GetUpgradeInfo("+((y/80)+512)+","+(x/128)+","+currentCoord+")");
		callbacks.showBuildMenu(true);
		callbacks.showUpgradeMenu(false);
		callbacks.showClear(true);
	}
	enum coord_type {
		invalid, empty, wall, building, tower;
	}
	private coord_type getCoordType(StructureId in) {
		if ((in.row == 0) || (in.row == 22)) return coord_type.invalid;
		if ((in.col == 0) || (in.col == 22)) return coord_type.invalid;
		
		if ((in.row == 1) || (in.row == 21) || (in.col == 1) || (in.col == 21)) {
			// FIXME use row when matching by col
			switch (in.col) {
			case 1:
			case 2:
				return coord_type.invalid;
			case 3:
			case 5:
			case 6:
			case 7:
			case 9:
			case 10:
			case 11:
			case 12:
			case 13:
			case 15:
			case 16:
			case 17:
			case 19:
				return coord_type.wall;
			case 4:
			case 8:
			case 14:
			case 18:
				return coord_type.tower;
			}
		}
		if ((in.row == 2) && (in.col == 2)) return coord_type.wall;
		if ((in.row == 11) || (in.col == 11)) {
			// FIXME use row when matching by col
			switch (in.col) {
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 16:
			case 17:
			case 18:
			case 19:
			case 20:
				return coord_type.wall;
			}
		}
		return coord_type.empty;
	}
	private void selectCoord(StructureId in) {
		boolean empty_slot = true;
		if (empty_slot) {
			newBuilding = in.toRectF();
			if (state.currentCity.queue.length == 16) {
				// the build queue is full, you cant select empty cells
				clearSelection();
				return;
			}
		}
		currentCoord = in;
		requestFocusFromTouch();
		RectF viewport = new RectF(getScrollX()/zoom,getScrollY()/zoom,
				(getScrollX()/zoom)+(getWidth()/zoom),(getScrollY()/zoom)+(getHeight()/zoom));
		Log.v(TAG,viewport.toString());
		if (!viewport.contains(newBuilding)) {
			if (newBuilding.bottom > viewport.bottom) scrollBy(0,(int)((newBuilding.bottom - viewport.bottom)* zoom));
			if (newBuilding.top < viewport.top) scrollBy(0,(int)((newBuilding.top - viewport.top)* zoom));
			if (newBuilding.right > viewport.right) scrollBy((int)((newBuilding.right - viewport.right) * zoom),0);
			if (newBuilding.left < viewport.left) scrollBy((int)((newBuilding.left - viewport.left)*zoom),0);
		}
		invalidate();
		Log.v(TAG,String.format("selectCoord(%s)",in.toString()));
	}
	public interface LayoutCallbacks {
		void showBuildMenu(boolean enabled);
		void showUpgradeMenu(boolean b);
		void showClear(boolean b);
	}
	public void clearSelection() {
		selected = null;
		newBuilding = null;
		invalidate();
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.v(TAG,"onKeyDown("+keyCode+","+event+")");
		StructureId target = null;
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_DOWN:
			if (currentCoord == null) {
				target = new StructureId(0,0);
			} else {
				target = currentCoord.down();
			}
			break;
		case KeyEvent.KEYCODE_DPAD_UP:
			target = currentCoord.up();
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			target = currentCoord.right();
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			target = currentCoord.left();
			break;
		default:
			return false;
		}
		if (target != null) {
			selectCoord(target);
			return true;
		} else clearSelection();
		return false;
	}
	@Override
	protected void onFocusChanged (boolean gainFocus, int direction, Rect previouslyFocusedRect) {
		Log.v(TAG,String.format("onFocusChanged(%s,%d,%s)",gainFocus ? "true":"false",direction, previouslyFocusedRect));
	}
	public void onStop() {
		Iterator<VisObject> i = buildings.iterator();
		while (i.hasNext()) {
			VisObject v = i.next();
			if (v instanceof LouStructure) ((LouStructure)v).onStop();
		}
		buildings.clear();
		Log.v(TAG,"hooks and buildings cleared");
	}
}
