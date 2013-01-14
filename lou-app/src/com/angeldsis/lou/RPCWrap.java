package com.angeldsis.lou;

import java.util.ArrayList;

import android.os.Handler;

import com.angeldsis.louapi.Account;
import com.angeldsis.louapi.ChatMsg;
import com.angeldsis.louapi.HttpRequest;
import com.angeldsis.louapi.LouState;
import com.angeldsis.louapi.RPC;

public class RPCWrap extends RPC {
	Handler handler;
	SessionKeeper.Session callbacks;
	public RPCWrap(Account acct, LouState state,SessionKeeper.Session activity) {
		super(acct, state);
		this.callbacks = activity;
		handler = new Handler();
	}
	@Override
	public
	HttpRequest newHttpRequest() {
		return new doRPCasync();
	}
	@Override
	public void visDataReset() {
		callbacks.visDataReset();
	}
	@Override
	public void visDataUpdated() {
		callbacks.vidDataUpdated();
	}
	@Override
	public void tick() {
		callbacks.tick();
	}
	@Override
	public void gotCityData() {
		callbacks.gotCityData();
	}
	@Override
	public void onChat(ArrayList<ChatMsg> d) {
		callbacks.onChat(d);
	}
	@Override
	public void onPlayerData() {
		callbacks.onPlayerData();
	}
	@Override
	public void onEjected() {
		callbacks.onEjected();
	}
	@Override
	public void cityChanged() {
		callbacks.cityChanged();
	}
	@Override
	public void cityListChanged() {
		callbacks.cityListChanged();
	}
	@Override
	public void runOnUiThread(Runnable r) {
		handler.post(r);
	}
}
