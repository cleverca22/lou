package com.angeldsis.lou;

import org.json.JSONObject;
import com.angeldsis.LOU.Account;
import com.angeldsis.LOU.LouState;
import com.angeldsis.LOU.RPC;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

public class LouSessionMain extends Activity implements RPC.Callbacks {
	static final String TAG = "LouSessionMain";
	Account acct;
	RPC rpc;
	LouState state;
	CityLayout mTest;
	boolean vis_data_loaded;
	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		Intent msg = getIntent();
		Bundle args = msg.getExtras();
		vis_data_loaded = false;
		acct = new Account(args);
		state = new LouState();
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
		setContentView(R.layout.city_layout);
	}
	protected void onStop() {
		super.onStop();
		rpc.stopPolling();
	}
	void gotVisDataInit() {
		vis_data_loaded = true;
		ViewGroup vg = (ViewGroup) this.findViewById(R.id.test);
		mTest = new CityLayout(this,state);
		vg.addView(mTest);
	}
	public void visDataReset() {
		Log.v(TAG,"vis count "+rpc.state.visData.size());
		if (!vis_data_loaded) gotVisDataInit();
	}
}
