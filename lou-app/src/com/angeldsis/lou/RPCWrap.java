package com.angeldsis.lou;

import com.angeldsis.LOU.Account;
import com.angeldsis.LOU.HttpRequest;
import com.angeldsis.LOU.LouState;
import com.angeldsis.LOU.RPC;

public class RPCWrap extends RPC {
	LouSessionMain activity;
	public RPCWrap(Account acct, LouState state,LouSessionMain activity) {
		super(acct, state);
		this.activity = activity;
		// TODO Auto-generated constructor stub
	}
	@Override
	public
	HttpRequest newHttpRequest() {
		// TODO Auto-generated method stub
		return new doRPCasync();
	}
	@Override
	public void visDataReset() {
		activity.visDataReset();
	}
	@Override
	public void tick() {
		activity.tick();
	}
	@Override
	public void gotCityData() {
		activity.gotCityData();
	}
}
