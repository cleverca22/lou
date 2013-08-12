package com.angeldsis.lou;

import android.os.Bundle;

import com.angeldsis.louapi.Account;
import com.angeldsis.louapi.ServerInfo;

// helper methods to send an Account thru a Bundle
public class AccountWrap extends Account {
	public int id = -1; // the id of a SessionKeeper.Session instance
	public AccountWrap() {
	}
	public AccountWrap(Account a) {
		pathid = a.pathid;
		serverid = a.serverid;
		sessionid = a.sessionid;
		worldid = a.worldid;
		world = a.world;
	}
	public AccountWrap(Bundle args) {
		pathid = args.getInt("pathid");
		serverid = args.getString("serverid");
		sessionid = args.getString("sessionid");
		worldid = args.getInt("worldid");
		world = args.getString("world");
		id = args.getInt("id", -1);
	}
	public AccountWrap(ServerInfo a) {
		serverid = a.serverid;
		pathid = a.pathid;
		sessionid = a.sessionId;
		world = a.servername;
		worldid = a.worldid;
	}
	public Bundle toBundle() {
		Bundle args = new Bundle();
		args.putInt("pathid", pathid);
		args.putString("serverid", serverid);
		args.putString("sessionid", sessionid);
		args.putInt("worldid", worldid);
		args.putString("world", world);
		args.putInt("id", id);
		return args;
	}
}
