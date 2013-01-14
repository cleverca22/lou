package com.angeldsis.lou;

import android.os.Bundle;

import com.angeldsis.louapi.Account;

// helper methods to send an Account thru a Bundle
public class AccountWrap extends Account {
	public AccountWrap() {
	}
	public AccountWrap(Account a) {
		pathid = a.pathid;
		serverid = a.serverid;
		sessionid = a.sessionid;
		worldid = a.worldid;
	}
	public AccountWrap(Bundle args) {
		pathid = args.getString("pathid");
		serverid = args.getString("serverid");
		sessionid = args.getString("sessionid");
		worldid = args.getInt("worldid");
	}
	public Bundle toBundle() {
		Bundle args = new Bundle();
		args.putString("pathid", pathid);
		args.putString("serverid", serverid);
		args.putString("sessionid", sessionid);
		args.putInt("worldid", worldid);
		return args;
	}
}
