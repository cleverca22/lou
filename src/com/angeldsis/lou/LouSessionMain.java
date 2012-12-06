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

public class LouSessionMain extends Activity {
	static final String TAG = "LouSessionMain";
	Account acct;
	RPC rpc;
	LouState state;
	LouStructure mTest;
	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		Intent msg = getIntent();
		Bundle args = msg.getExtras();
		acct = new Account(args);
		state = new LouState();
		rpc = new RPC(acct,state);
		rpc.OpenSession(true,rpc.new RPCDone() {
			public void requestDone(JSONObject reply) {
				Log.v(TAG,"session opened");
				rpc.GetServerInfo(rpc.new RPCDone() {
					public void requestDone(JSONObject reply) {
						rpc.GetPlayerInfo(rpc.new RPCDone() {
							@Override
							public void requestDone(JSONObject reply) {
								// state variable now has some data populated
								rpc.Poll();
							}
						});
					}
				});
			}
		});
		setContentView(R.layout.city_layout);
		ViewGroup vg = (ViewGroup) this.findViewById(R.id.test);
		mTest = new LouStructure(this);
		vg.addView(mTest);
	}
}
