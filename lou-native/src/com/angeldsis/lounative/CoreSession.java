package com.angeldsis.lounative;

import org.eclipse.swt.widgets.Display;
import org.json2.JSONObject;

import com.angeldsis.louapi.Account;
import com.angeldsis.louapi.Log;
import com.angeldsis.louapi.LouState;
import com.angeldsis.louapi.RPC.RPCDone;

public class CoreSession {
	static final String TAG = "CoreSession";
	LouState state;
	RPCWrap rpc;
	MainWindow mw;
	SubsList subListWindow;
	Display display;
	IdleTroops idleTroops;

	public CoreSession(Account a, Display display) {
		this.display = display;
		System.out.println("you picked "+a.world);
		state = new LouState();
		rpc = new RPCWrap(a,state);
		state.setRPC(rpc);
		rpc.OpenSession(true,new RPCDone() {
			public void requestDone(JSONObject reply) {
				Log.v(TAG,"session opened");
				rpc.GetServerInfo(new RPCDone() {
					public void requestDone(JSONObject reply) {
						rpc.GetPlayerInfo(new RPCDone() {
							@Override
							public void requestDone(JSONObject reply) {
								// state variable now has some data populated
								rpc.startPolling();
							}
						});
					}
				});
			}
		});
		rpc.setChat(new ChatWindow(display,rpc));
		rpc.setCoreSession(this);
		mw = new MainWindow(display,this);
	}
	public void openCity() {
		// TODO Auto-generated method stub
	}
	public void onSubListChanged() {
		if (subListWindow == null) {
			//subListWindow = new SubsList(rpc,display);
			//subListWindow.open();
		}
		//subListWindow.onSubListChanged();
	}
	public void openIdleTroops() {
		if (idleTroops == null) {
			idleTroops = new IdleTroops(rpc);
			idleTroops.open();
		} else {
			idleTroops.forceFocus();
		}
	}
	public void onDefenseOverviewUpdate() {
		if (idleTroops != null) idleTroops.onDefenseOverviewUpdate();
	}
	public void cityChanged() {
		if (idleTroops != null) idleTroops.cityChanged();
	}
}
