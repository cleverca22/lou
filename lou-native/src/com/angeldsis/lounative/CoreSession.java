package com.angeldsis.lounative;

import org.eclipse.swt.widgets.Display;
import org.json.JSONObject;

import com.angeldsis.LOU.Account;
import com.angeldsis.LOU.LouState;

public class CoreSession {
	LouState state;
	RPCWrap rpc;

	public CoreSession(Account a, Display display) {
		System.out.println("you picked "+a.world);
		state = new LouState();
		rpc = new RPCWrap(a,state,display);
		rpc.OpenSession(true,rpc.new RPCDone() {
			public void requestDone(JSONObject reply) {
				System.out.println("session opened");
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
	}
}
