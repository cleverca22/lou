package com.angeldsis.louapi;

import java.util.ArrayList;

import org.json2.JSONArray;
import org.json2.JSONException;
import org.json2.JSONObject;

public class AllianceAttackMonitor {
	private static final String TAG = "AllianceAttackMonitor";
	int ia,oa,new_ia,new_oa;
	RPC rpc;
	AllianceAttackMonitor(RPC rpc) {
		this.rpc = rpc;
		ia = oa = 0;
	}
	public void countsUpdated(int ia, int oa) {
		if (this.ia != ia) {
			Log.v(TAG,"ia changed");
			new_ia = ia;
		}
	}
	public String getRequestDetails() {
		if (ia != new_ia) {
			return "\fALL_AT:a";
		}
		return "";
	}
	public void parseReply(JSONObject d) {
		int v = d.optInt("v");
		Log.v(TAG,"alliance attack data v:"+v);
		JSONArray a = d.optJSONArray("a");
		int i;
		synchronized (rpc.state) {
			ArrayList<IncomingAttack> newlist = new ArrayList<IncomingAttack>();
			for (i = 0; i < a.length(); i++) {
				JSONObject attack = a.optJSONObject(i);
				System.out.println(attack.toString());
				int id = attack.optInt("i");
				IncomingAttack att = rpc.state.findById(id,rpc.state.incomingAllianceAttacks);
				if (att == null) { // its new
					att = new IncomingAttack(rpc.state,id);
					att.updateAllianceType(attack);
					rpc.onNewAttack(att);
				} else {
					att.updateAllianceType(attack);
				}
				newlist.add(att);
			}
			rpc.state.incomingAllianceAttacks = newlist;
		}
		ia = a.length();
	}
}