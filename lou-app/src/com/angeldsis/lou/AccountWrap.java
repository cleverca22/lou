package com.angeldsis.lou;

import android.os.Bundle;

import com.angeldsis.LOU.Account;

// helper methods to send an Account thru a Bundle
public class AccountWrap extends Account {
	public AccountWrap() {
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
