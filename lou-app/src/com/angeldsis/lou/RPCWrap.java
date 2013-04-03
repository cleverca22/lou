package com.angeldsis.lou;

import java.util.ArrayList;

import android.os.Handler;
import android.util.Log;

import com.angeldsis.louapi.Account;
import com.angeldsis.louapi.ChatMsg;
import com.angeldsis.louapi.IncomingAttack;
import com.angeldsis.louapi.LouState;
import com.angeldsis.louapi.LouVisData;
import com.angeldsis.louapi.RPC;
import com.angeldsis.louapi.Timeout;
import com.angeldsis.louapi.world.WorldParser.Cell;

public class RPCWrap extends RPC {
	Handler handler = new Handler();
	SessionKeeper.Session callbacks;
	public RPCWrap(Account acct, LouState state,SessionKeeper.Session activity) {
		super(acct, state);
		Log.v("RPCWrap","handler is "+handler);
		this.callbacks = activity;
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
		Log.v("RPCWrap","posting "+r+" to "+handler);
		handler.post(r);
	}
	@Override
	public void onVisObjAdded(LouVisData[] v) {
		callbacks.onVisObjAdded(v);
	}
	@Override
	public void onNewAttack(IncomingAttack a) {
		callbacks.onNewAttack(a);
	}
	@Override
	public void onReportCountUpdate() {
		callbacks.onReportCountUpdate();
	}
	@Override
	public void onSubListChanged() {
		callbacks.onSubListChanged();
	}
	@Override
	public void startSubstituteSession(String sessionid) {
		callbacks.startSubstituteSession(sessionid);
	}
	@Override
	protected Timeout getMaxPoll() {
		return callbacks.getMaxPoll();
	}
	@Override
	public void cellUpdated(final Cell c, ArrayList<Object> changes) { // FIXME
		runOnUiThread(new Runnable() {
			public void run() {
				callbacks.cellUpdated(c);
			}
		});
	}
	@Override public void setTimer(long maxdelay) {
		callbacks.setTimer(maxdelay);
	}
	@Override public void setThreadActive(boolean b) {
		callbacks.setThreadActive(b);
	}
	@Override public void onBuildQueueUpdate() {
		callbacks.onBuildQueueUpdate();
	}
	@Override public boolean uiActive() {
		return callbacks.uiActive();
	}
	public void logRequest(int req,int reply,String func) {
		super.logRequest(req, reply, func);
		callbacks.logRequest(req,reply,func);
	}
	@Override public void onDefenseOverviewUpdate() {
		callbacks.onDefenseOverviewUpdate();
	}
}
