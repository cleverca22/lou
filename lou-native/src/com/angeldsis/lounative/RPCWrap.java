package com.angeldsis.lounative;

import java.util.ArrayList;

import org.eclipse.swt.widgets.Display;
import org.json.JSONArray;
import org.json.JSONException;

import com.angeldsis.LOU.Account;
import com.angeldsis.LOU.ChatMsg;
import com.angeldsis.LOU.HttpRequest;
import com.angeldsis.LOU.LouState;
import com.angeldsis.LOU.RPC;

public class RPCWrap extends RPC {
	private ChatWindow chat;
	Display display;
	public RPCWrap(Account acct, LouState state, Display display) {
		super(acct, state);
		this.display = display;
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
	public void setChat(ChatWindow chatWindow) {
		this.chat = chatWindow;
	}
	@Override
	public void onChat(final ArrayList<ChatMsg> d) throws JSONException {
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				int i;
				for (i = 0; i < d.size(); i++) {
					try {
						chat.handle_msg(d.get(i));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	@Override
	public void onPlayerData() {
		// TODO Auto-generated method stub
		
	}
}
