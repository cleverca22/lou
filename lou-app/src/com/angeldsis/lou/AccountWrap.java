package com.angeldsis.lou;

import android.os.Bundle;

import com.angeldsis.LOU.Account;

// helper methods to send an Account thru a Bundle
public class AccountWrap extends Account {
	public AccountWrap(Bundle args) {
		this.pathid = args.getString("pathid");
		this.serverid = args.getString("serverid");
		this.sessionid = args.getString("sessionid");
	}
	public Bundle toBundle() {
    	Bundle args = new Bundle();
    	args.putString("pathid", this.pathid);
    	args.putString("serverid", this.serverid);
    	args.putString("sessionid", this.sessionid);
    	return args;
	}
}
