package com.angeldsis.lounative;

import org.eclipse.swt.widgets.Display;
import org.json.JSONArray;
import org.json.JSONException;

import com.angeldsis.LOU.Account;
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
	public void onChat(final JSONArray d) throws JSONException {
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				int i;
				for (i = 0; i < d.length(); i++) {
					try {
						chat.handle_msg(d.getJSONObject(i));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
}
