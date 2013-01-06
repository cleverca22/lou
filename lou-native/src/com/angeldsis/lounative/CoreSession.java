package com.angeldsis.lounative;

import org.eclipse.swt.widgets.Display;
import org.json.JSONObject;

import com.angeldsis.louapi.Account;
import com.angeldsis.louapi.Log;
import com.angeldsis.louapi.LouState;

public class CoreSession {
	static final String TAG = "CoreSession";
	LouState state;
	RPCWrap rpc;
	MainWindow mw;

	public CoreSession(Account a, Display display) {
		System.out.println("you picked "+a.world);
		state = new LouState();
		rpc = new RPCWrap(a,state,display);
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
		rpc.setChat(new ChatWindow(display,rpc));
		mw = new MainWindow(display,this);
	}

	public void openCity() {
		// TODO Auto-generated method stub
	}
}
