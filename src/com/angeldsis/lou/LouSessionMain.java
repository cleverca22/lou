package com.angeldsis.lou;

import org.json.JSONObject;
import com.angeldsis.LOU.Account;
import com.angeldsis.LOU.LouState;
import com.angeldsis.LOU.RPC;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
	public void up(View v) {
		mTest.up();
	}
	public void down(View v) {
		mTest.down();
	}
	public void left(View v) {
		mTest.left();
	}
	public void right(View v) {
		mTest.right();
	}
	void gotVisDataInit() {
		vis_data_loaded = true;
		ViewGroup vg = (ViewGroup) this.findViewById(R.id.test);
		TextView stats = (TextView) this.findViewById(R.id.stats);
		mTest = new CityLayout(this,state,stats);
		vg.addView(mTest);
	}
	public void visDataReset() {
		Log.v(TAG,"vis count "+rpc.state.visData.size());
		if (!vis_data_loaded) gotVisDataInit();
	}
}
