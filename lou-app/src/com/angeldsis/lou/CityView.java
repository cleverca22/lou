package com.angeldsis.lou;

import com.angeldsis.lou.CityLayout.LayoutCallbacks;
import com.angeldsis.lou.SessionKeeper.Callbacks;
import com.angeldsis.lou.city.BuildMenu;
import com.angeldsis.louapi.LouVisData;
import com.angeldsis.louapi.RPC.UpgradeStarted;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class CityView extends SessionUser implements Callbacks, LayoutCallbacks {
	private static final String TAG = "CityView";
	CityUI mTest;
	boolean vis_data_loaded;
	private MenuItem build,upgrade,visible;
	private boolean build_on,upgrade_on,visible_on;

	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		Log.v(TAG,"onCreate");
		if (Build.VERSION.SDK_INT > 13) initApi14();
		setContentView(R.layout.city_layout);
		vis_data_loaded = false;
		build_on = upgrade_on = visible_on = false;
		mTest = new CityUI(this);
		ViewGroup vg = (ViewGroup) this.findViewById(R.id.test);
		vg.addView(mTest);
		((FrameLayout) findViewById(R.id.resource_bar)).addView(mTest.resource_bar.self);
	}
	void session_ready() {
		mTest.setState(session.state,session.rpc);
		if (session.state.currentCity != null) mTest.resource_bar.update(session.state.currentCity);
		session.state.enableVis();
	}
	protected void onStart() {
		super.onStart();
		Log.v(TAG,"onStart");
	}
	protected void onStop() {
		super.onStop();
		Log.v(TAG,"onStop");
	}
	public void tick() {
		// called from the network thread, needs to re-dir to main one
		Runnable resync = new Runnable() {
			public void run() {
				mTest.tick();
			}
		};
		this.runOnUiThread(resync);
	}
	public void visDataReset() {
		Log.v(TAG,"vis count "+session.rpc.state.currentCity.visData.size());
		mTest.mTest.visDataReset();
		if (!vis_data_loaded) gotVisDataInit();
	}
	void gotVisDataInit() {
		vis_data_loaded = true;
		mTest.gotVisData();
		Log.v(TAG,"added view");
	}
	@Override
	public void onVisObjAdded(LouVisData v) {
		mTest.mTest.onVisObjAdded(v,true);
	}
	public void gotCityData() {
		mTest.gotCityData();
	}
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.removeItem(R.id.city); // FIXME, switch to hide
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.city_menu, menu);
		build = menu.findItem(R.id.build);
		upgrade = menu.findItem(R.id.upgrade);
		visible = menu.findItem(R.id.clear);
		build.setEnabled(build_on);
		upgrade.setEnabled(upgrade_on);
		visible.setVisible(visible_on);
		return true;
	}
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.v(TAG,"click! "+item.getItemId());
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent i = new Intent(this,LouSessionMain.class);
			i.putExtras(acct.toBundle());
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			return true;
		case R.id.build:
			showBuildDialog();
			return true;
		case R.id.upgrade:
			int typeid = ((LouStructure)mTest.mTest.selected).typeid;
			session.rpc.UpgradeBuilding(session.state.currentCity, mTest.mTest.currentCoord.toCoord(),
					typeid, new UpgradeStarted() {
				@Override
				public void started() {
					build.setEnabled(false);
					upgrade.setEnabled(false);
					visible.setVisible(false);
					mTest.mTest.clearSelection();
				}
			});
			return true;
		case R.id.clear:
			showBuildMenu(false);
			showUpgradeMenu(false);
			mTest.mTest.clearSelection();
		}
		return super.onOptionsItemSelected(item);
	}
	void showBuildDialog() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
		if (prev != null) ft.remove(prev);
		ft.addToBackStack(null);
		DialogFragment newFragment = BuildMenu.newInstance(session.state.currentCity,
				mTest.mTest.currentCoord.toCoord());
		newFragment.show(ft, "dialog");
	}
	public void do_build(long cityid,int structureid,int coord) {
		if (cityid != session.state.currentCity.getCityid()) {
			Log.e(TAG,"wrong city!!!");
		}
		Log.v(TAG,String.format("%s %s", structureid,coord));
		session.rpc.UpgradeBuilding(session.state.currentCity, coord, structureid, new UpgradeStarted() {
			@Override
			public void started() {
				build.setEnabled(false);
				upgrade.setEnabled(false);
				visible.setVisible(false);
				mTest.mTest.clearSelection();
			}
		});
	}
	@Override
	public void showBuildMenu(boolean enabled) {
		if (build == null) {
			build_on = enabled;
			return;
		}
		build.setEnabled(enabled);
	}
	@Override
	public void showUpgradeMenu(boolean b) {
		if (upgrade == null) {
			upgrade_on = b;
			return;
		}
		upgrade.setEnabled(b);
	}
	@Override
	public void showClear(boolean b) {
		if (visible == null) {
			visible_on = b;
			return;
		}
		visible.setVisible(b);
	}
}
