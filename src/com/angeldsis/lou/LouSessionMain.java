package com.angeldsis.lou;

import org.json.JSONObject;
import com.angeldsis.LOU.Account;
import com.angeldsis.LOU.LouState;
import com.angeldsis.LOU.RPC;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

public class LouSessionMain extends FragmentActivity implements RPC.Callbacks, RadioGroup.OnCheckedChangeListener {
	static final String TAG = "LouSessionMain";
	Account acct;
	RPC rpc;
	LouState state;
	CityUI mTest;
	boolean vis_data_loaded;
	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		Intent msg = getIntent();
		Bundle args = msg.getExtras();
		setContentView(R.layout.city_layout);
		RadioGroup rg = (RadioGroup)findViewById(R.id.zoom);
		rg.setOnCheckedChangeListener(this);
		vis_data_loaded = false;
		acct = new Account(args);
		state = new LouState();
		mTest = new CityUI(this,state);
		ViewGroup vg = (ViewGroup) this.findViewById(R.id.test);
		vg.addView(mTest);
		rpc = new RPC(acct,state,this);
		rpc.OpenSession(true,rpc.new RPCDone() {
			public void requestDone(JSONObject reply) {
				Log.v(TAG,"session opened");
				rpc.GetServerInfo(rpc.new RPCDone() {
					public void requestDone(JSONObject reply) {
						rpc.GetPlayerInfo(rpc.new RPCDone() {
							@Override
							public void requestDone(JSONObject reply) {
								// state variable now has some data populated
								rpc.startPolling();
							}
						});
					}
				});
			}
		}, 0);
	}
	protected void onStop() {
		super.onStop();
		rpc.stopPolling();
	}
	void gotVisDataInit() {
		vis_data_loaded = true;
		mTest.gotVisData();
		Log.v(TAG,"added view");
	}
	public void visDataReset() {
		Log.v(TAG,"vis count "+rpc.state.visData.size());
		if (!vis_data_loaded) gotVisDataInit();
	}
	@Override
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
	@Override
	public void gotCityData() {
		mTest.gotCityData();
	}
	@Override
	public void tick() {
		// called from the network thread, needs to re-dir to main one
		Runnable resync = new Runnable() {
			public void run() {
				mTest.tick();
			}
		};
		this.runOnUiThread(resync);
	}
}
