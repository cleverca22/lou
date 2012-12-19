package com.angeldsis.lou;

import com.angeldsis.LOU.Account;
import com.angeldsis.LOU.HttpRequest;
import com.angeldsis.LOU.LouState;
import com.angeldsis.LOU.RPC;

public class RPCWrap extends RPC {
	public RPCWrap(Account acct, LouState state, Callbacks callbacks) {
		super(acct, state, callbacks);
		// TODO Auto-generated constructor stub
	}
	@Override
	public
	HttpRequest newHttpRequest() {
		// TODO Auto-generated method stub
		return new doRPCasync();
	}
}
