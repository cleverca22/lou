package com.angeldsis.lounative;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Scanner;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.json.JSONObject;

import com.angeldsis.LOU.Account;
import com.angeldsis.LOU.LouSession;
import com.angeldsis.LOU.LouState;

public class LouMain {
	boolean cli;
	RPCWrap rpc;
	LouSession session;
	LouState state;
	public static void main(String[] args) throws Exception {
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
		Config.init();
		Display display = new Display();
		cli = cli2;
		session = new LouSession();
		if (true) {
			DoLogin login = new DoLogin(display,session);
		}
		System.out.println("done?");
		return;
		/*
		System.out.println("found "+session.servers.size());
		Iterator<Account> i = session.servers.iterator();
		int j = 0;
		while (i.hasNext()) {
			Account a = i.next();
			System.out.println("account #"+j+" "+a.world);
			j++;
		}
		System.out.println("please pick one");
		Scanner in = new Scanner(System.in);
		int number = in.nextInt();
		Account a = session.servers.get(number);
		System.out.println("you picked "+a.world);
		state = new LouState();
		rpc = new RPCWrap(a,state);
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
		while (true) {
			String message = in.nextLine();
			System.out.println(message);
			rpc.QueueChat(message+"\n");
		}*/
	}
}
