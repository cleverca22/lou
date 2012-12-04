package com.angeldsis.LOU;

import android.os.Bundle;

public class Account {
	public String world, pathid, serverid, sessionid;
	public boolean offline;
	public Account(Bundle args) {
		this.pathid = args.getString("pathid");
		this.serverid = args.getString("serverid");
		this.sessionid = args.getString("sessionid");
	}
	public Account() {
	}
	public Bundle toBundle() {
    	Bundle args = new Bundle();
    	args.putString("pathid", this.pathid);
    	args.putString("serverid", this.serverid);
    	args.putString("sessionid", this.sessionid);
    	return args;
	}
}