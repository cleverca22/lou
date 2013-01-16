package com.angeldsis.lou;

import com.angeldsis.lou.CityLayout.LayoutCallbacks;
import com.angeldsis.lou.SessionKeeper.Callbacks;
import com.angeldsis.lou.city.BuildMenu;

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
	private MenuItem build,upgrade;

	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		Log.v(TAG,"onCreate");
		if (Build.VERSION.SDK_INT > 13) initApi14();
		setContentView(R.layout.city_layout);
		vis_data_loaded = false;
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
		if (!vis_data_loaded) gotVisDataInit();
	}
	void gotVisDataInit() {
		vis_data_loaded = true;
		mTest.gotVisData();
		Log.v(TAG,"added view");
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
			session.rpc.UpgradeBuilding(session.state.currentCity, mTest.mTest.currentCoord, typeid);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	void showBuildDialog() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
		if (prev != null) ft.remove(prev);
		ft.addToBackStack(null);
		DialogFragment newFragment = BuildMenu.newInstance(session.state.currentCity,mTest.mTest.currentCoord);
		newFragment.show(ft, "dialog");
	}
	public void do_build(int structureid,int coord) {
		Log.v(TAG,String.format("%s %s", structureid,coord));
		session.rpc.UpgradeBuilding(session.state.currentCity, coord, structureid);
	}
	@Override
	public void showBuildMenu(boolean enabled) {
		build.setEnabled(enabled);
	}
	@Override
	public void showUpgradeMenu(boolean b) {
		upgrade.setEnabled(b);
	}
}
