package com.angeldsis.lounative;

import java.util.ArrayList;

import org.eclipse.swt.widgets.Display;
import org.json2.JSONObject;

import com.angeldsis.louapi.Account;
import com.angeldsis.louapi.Log;
import com.angeldsis.louapi.LouState;
import com.angeldsis.louapi.RPC.RPCDone;
import com.angeldsis.louapi.ServerInfo;

public class CoreSession {
	static final String TAG = "CoreSession";
	LouState state;
	RPCWrap rpc;
	MainWindow mw;
	SubsList subListWindow;
	Display display;
	IdleTroops idleTroops;
	static ArrayList<CoreSession> sessions = new ArrayList<CoreSession>();

	public CoreSession(ServerInfo si, Display display) {
		this.display = display;
		System.out.println("you picked "+si.servername);
		state = new LouState();
		Account a = new Account();
		a.world = si.servername;
		a.serverid = si.serverid;
		a.pathid = si.pathid;
		a.worldid = si.worldid;
		a.sessionid = si.sessionId;
		rpc = new RPCWrap(a,state,this);
		state.setRPC(rpc);
		sessions.add(this);
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
		if (!rpc.passive) rpc.setChat(new ChatWindow(display,rpc));
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
	public void openMail() {
		rpc.setMail(new MailWindow(display,rpc));
	}
	public void onEjected() {
		sessions.remove(this);
	}
}
