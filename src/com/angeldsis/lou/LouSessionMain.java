package com.angeldsis.lou;

import org.json.JSONObject;
import com.angeldsis.LOU.Account;
import com.angeldsis.LOU.LouState;
import com.angeldsis.LOU.RPC;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class LouSessionMain extends Activity {
	static final String TAG = "LouSessionMain";
	Account acct;
	RPC rpc;
	LouState state;
	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		Intent msg = getIntent();
		Bundle args = msg.getExtras();
		acct = new Account(args);
		state = new LouState();
		rpc = new RPC(acct,state);
		rpc.OpenSession(true,rpc.new RPCDone() {
			public void requestDone(JSONObject reply) {
				// TODO Auto-generated method stub
				Log.v(TAG,"session opened");
				rpc.GetServerInfo(rpc.new RPCDone() {
					public void requestDone(JSONObject reply) {
						// TODO Auto-generated method stub
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
	}
}
