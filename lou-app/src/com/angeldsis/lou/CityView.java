package com.angeldsis.lou;

import com.angeldsis.lou.SessionKeeper.Callbacks;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class CityView extends SessionUser implements OnCheckedChangeListener, Callbacks {
	private static final String TAG = "CityView";
	CityUI mTest;
	boolean vis_data_loaded;

	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		Log.v(TAG,"onCreate");
		if (Build.VERSION.SDK_INT > 13) initApi14();
		setContentView(R.layout.city_layout);
		RadioGroup rg = (RadioGroup)findViewById(R.id.zoom);
		rg.setOnCheckedChangeListener(this);
		vis_data_loaded = false;
		mTest = new CityUI(this);
		ViewGroup vg = (ViewGroup) this.findViewById(R.id.test);
		vg.addView(mTest);
		((FrameLayout) findViewById(R.id.resource_bar)).addView(mTest.resource_bar.self);
	}
	void session_ready() {
		mTest.setState(session.state);
		mTest.resource_bar.update(session.state.currentCity);
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
	public void onCheckedChanged(RadioGroup arg0, int arg1) {
		switch (arg0.getCheckedRadioButtonId()) {
		case R.id.one:
			mTest.setZoom(1);
			break;
		case R.id.two:
			mTest.setZoom(0.5f);
			break;
		case R.id.three:
			mTest.setZoom(0.25f);
			break;
		}
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
		menu.removeItem(R.id.city);
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
		}
		return super.onOptionsItemSelected(item);
	}
}
