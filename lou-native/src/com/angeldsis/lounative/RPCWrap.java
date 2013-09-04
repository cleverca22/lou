package com.angeldsis.lounative;

import java.util.ArrayList;
import java.util.Iterator;

import com.angeldsis.louapi.Account;
import com.angeldsis.louapi.ChatMsg;
import com.angeldsis.louapi.IncomingAttack;
import com.angeldsis.louapi.Log;
import com.angeldsis.louapi.LouState;
import com.angeldsis.louapi.LouState.City;
import com.angeldsis.louapi.LouVisData;
import com.angeldsis.louapi.RPC;
import com.angeldsis.louapi.Timeout;
import com.angeldsis.louapi.world.WorldParser.Cell;
import com.angeldsis.louutil.HttpUtilImpl;

public class RPCWrap extends RPC {
	private static String TAG = "RPCWrap";
	private ChatWindow chat;
	private CoreSession core;
	private MailWindow mailHook;
	private CoreSession session;
	public RPCWrap(Account acct, LouState state, CoreSession session) {
		super(acct, state, HttpUtilImpl.getInstance());
		this.session = session;
	}
	@Override
	public void visDataReset() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void tick() {
	}
	public void gotCityData() {
	}
	public void setChat(ChatWindow chatWindow) {
		this.chat = chatWindow;
	}
	@Override
	public void onChat(final ArrayList<ChatMsg> d) {
		// FIXME, show some kind of notification to re-open chat
		if (chat == null) return;
		int i;
		for (i = 0; i < d.size(); i++) {
			chat.handle_msg(d.get(i));
		}
	}
	@Override
	public void onPlayerData() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onCityChanged() {
		Log.v(TAG,"current city changed");
		core.cityChanged();
	}
	@Override
	public void onEjected(String reason) {
		// TODO Auto-generated method stub
		session.onEjected();
		Log.e(TAG,"ejected:"+reason);
	}
	@Override
	public void cityListChanged() {
		Log.w(TAG, "cityListChanged");
	}
	@Override
	public void visDataUpdated() {
		Log.w(TAG, "onVisDataUpdated");
	}
	@Override
	public void runOnUiThread(Runnable r) {
		LouMain.instance.display.asyncExec(r);
	}
	@Override
	public void onNewAttack(IncomingAttack a) {
		Log.w(TAG,"onNewAttack");
	}
	@Override
	public void onVisObjAdded(LouVisData[] v) {
		Log.w(TAG,"onVisObjAdded");
	}
	public void setCoreSession(CoreSession coreSession) {
		this.core = coreSession;
	}
	@Override
	public void onSubListChanged() {
		core.onSubListChanged();
	}
	@Override public void startSubstituteSession(String sessionid, int playerid, SubRequestDone cb) {
		// TODO Auto-generated method stub
		Log.e(TAG, "startSubstituteSession");
		// this needs to create a new CoreSession object then run the callback
	}
	@Override public void cellUpdated(Cell c, ArrayList<Object> changes) {
		// TODO Auto-generated method stub
	}
	@Override public void onReportCountUpdate() {
		Log.w(TAG,"onReportCountUpdate");
		core.mw.onReportCountUpdate(state.viewed_reports,state.unviewed_reports);
	}
	@Override protected Timeout getMaxPoll() {
		Timeout t = new Timeout();
		t.min = t.max = 10000;
		return t;
	}
	@Override
	public boolean uiActive() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void onBuildQueueUpdate() {
		// TODO Auto-generated method stub
		
	}
	@Override public void onDefenseOverviewUpdate() {
		core.onDefenseOverviewUpdate();
	}
	@Override
	public void onEnlightenedCityChanged() {
		// TODO Auto-generated method stub
		
	}
	@Override public void onFoodWarning() {
		Iterator<City> i = foodWarnings.warnings.values().iterator();
		while (i.hasNext()) {
			City c = i.next();
			int timeLeft = c.foodEmptyTime(state);
			if (timeLeft > 3600) continue;
			Log.v(TAG,String.format("food warning %s %,d",c.name,timeLeft));
		}
	}
	public void setMail(MailWindow mailWindow) {
		mailHook = mailWindow;
	}
}
