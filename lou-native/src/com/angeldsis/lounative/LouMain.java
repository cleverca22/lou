package com.angeldsis.lounative;

import java.io.DataInputStream;
import java.util.Iterator;

import org.json.JSONObject;

import com.angeldsis.LOU.Account;
import com.angeldsis.LOU.LouSession;
import com.angeldsis.LOU.LouState;
import com.angeldsis.LOU.LouSession.result;
import com.angeldsis.LOU.RPC.Callbacks;

public class LouMain implements Callbacks {
	boolean cli;
	RPCWrap rpc;
	LouSession session;
	LouState state;
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
		String password = "FIXME";
		session = new LouSession();
		System.out.println("starting login");
		result reply = session.startLogin(username,password);
		if (reply.error) {
			System.out.println(reply.errmsg);
			throw reply.e;
		}
		if (reply.worked) System.out.println("worked");
		else return;
		System.out.println("found "+session.servers.size());
		Iterator<Account> i = session.servers.iterator();
		int j = 0;
		while (i.hasNext()) {
			Account a = i.next();
			System.out.println("account #"+j+" "+a.world);
			j++;
		}
		System.out.println("please pick one");
		DataInputStream in = new DataInputStream(System.in);
		int number = Integer.parseInt(in.readLine());
		Account a = session.servers.get(number);
		System.out.println("you picked "+a.world);
		state = new LouState();
		rpc = new RPCWrap(a,state,this);
		rpc.OpenSession(true,rpc.new RPCDone() {
			public void requestDone(JSONObject reply) {
				System.out.println("session opened");
				rpc.GetServerInfo(rpc.new RPCDone() {
					public void requestDone(JSONObject reply) {
						rpc.GetPlayerInfo(rpc.new RPCDone() {
							@Override
							public void requestDone(JSONObject reply) {
								// state variable now has some data populated
								rpc.startPolling();
							}
						});
					}
				});
			}
		}, 0);
	}
	@Override
	public void visDataReset() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void tick() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void gotCityData() {
		// TODO Auto-generated method stub
		
	}
}
