package com.angeldsis.lounative;

import com.angeldsis.LOU.LouSession;
import com.angeldsis.LOU.LouSession.result;

public class LouMain {
	boolean cli;
	RPCWrap rpc;
	LouSession session;
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		int i;
		boolean cli = true;
		for (i = 0; i < args.length; i++) {
			if (args[i].equals("-cli")) cli = true;
			else if (args[i].equals("-nocli")) cli = false;
		}
		LouMain start = new LouMain();
		start.init(cli);
	}
	private void init(boolean cli2) throws Exception {
		cli = cli2;
		String username = "cleverca22@gmail.com";
		String password = "EDITED";
		session = new LouSession();
		System.out.println("starting login");
		result reply = session.startLogin(username,password);
		if (reply.error) {
			System.out.println(reply.errmsg);
			throw reply.e;
		}
		if (reply.worked) System.out.println("worked");
	}
}
