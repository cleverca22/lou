package com.angeldsis.lounative;

import com.angeldsis.LOU.Account;
import com.angeldsis.LOU.HttpRequest;
import com.angeldsis.LOU.LouState;
import com.angeldsis.LOU.RPC;

public class RPCWrap extends RPC {
	public RPCWrap(Account acct, LouState state) {
		super(acct, state);
		// TODO Auto-generated constructor stub
	}
	public HttpRequest newHttpRequest() {
		return new HttpRequestWrap();
	}
	@Override
	public void visDataReset() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void tick() {
		// TODO Auto-generated method stub
		
	}
	public void gotCityData() {
	}
}
