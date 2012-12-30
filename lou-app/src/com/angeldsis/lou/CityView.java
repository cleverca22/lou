package com.angeldsis.lou;

import java.util.ArrayList;

import org.json.JSONArray;

import com.angeldsis.LOU.ChatMsg;
import com.angeldsis.lou.SessionKeeper.Callbacks;
import com.angeldsis.lou.SessionKeeper.MyBinder;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class CityView extends FragmentActivity implements OnCheckedChangeListener, Callbacks {
	private static final String TAG = "CityView";
	CityUI mTest;
	boolean vis_data_loaded;
	SessionKeeper mService;
	boolean mBound;
	AccountWrap acct;
	SessionKeeper.Session session;

	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		Log.v(TAG,"onCreate");
		Intent msg = getIntent();
		Bundle args = msg.getExtras();
		setContentView(R.layout.city_layout);
		RadioGroup rg = (RadioGroup)findViewById(R.id.zoom);
		rg.setOnCheckedChangeListener(this);
		vis_data_loaded = false;
		acct = new AccountWrap(args);
		Intent intent = new Intent(this,SessionKeeper.class);
		startService(intent);
	}
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.v(TAG,"onServiceConnected");
			MyBinder binder = (MyBinder)service;
			mService = binder.getService();
			mBound = true;
			CityView.this.check_state();
		}
		public void onServiceDisconnected(ComponentName arg0) {
			Log.v(TAG,"onServiceDisconnected");
			mBound = false;
		}
	};
	void check_state() {
		Log.v(TAG,"check_state");
		if (session == null) {
			session = mService.getSession(acct);
			session.setCallback(this);
			mTest = new CityUI(this,session.state);
			ViewGroup vg = (ViewGroup) this.findViewById(R.id.test);
			vg.addView(mTest);
		}
	}
	public void startActivity (Intent intent, Bundle options) {
		Log.v(TAG,"resuming not finished");
		acct = new AccountWrap(options);
	}
	protected void onStart() {
		super.onStart();
		Log.v(TAG,"onStart");
		Intent intent2 = new Intent(this,SessionKeeper.class);
		bindService(intent2,mConnection,BIND_AUTO_CREATE);
	}
	protected void onStop() {
		super.onStop();
		Log.v(TAG,"onStop");
		if (mBound) {
			if (session != null) session.unsetCallback(this);
			unbindService(mConnection);
			mBound = false;
		}
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
		Log.v(TAG,"vis count "+session.rpc.state.visData.size());
		if (!vis_data_loaded) gotVisDataInit();
	}
	public void onChat(ArrayList<ChatMsg> d) {
		// TODO Auto-generated method stub
		
	}
	void gotVisDataInit() {
		vis_data_loaded = true;
		mTest.gotVisData();
		Log.v(TAG,"added view");
	}
	public void gotCityData() {
		mTest.gotCityData();
	}
	@Override
	public void onPlayerData() {
		// TODO Auto-generated method stub
		
	}
}