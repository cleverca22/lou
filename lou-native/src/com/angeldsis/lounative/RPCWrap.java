package com.angeldsis.lounative;

import java.util.ArrayList;

import com.angeldsis.louapi.Account;
import com.angeldsis.louapi.ChatMsg;
import com.angeldsis.louapi.IncomingAttack;
import com.angeldsis.louapi.Log;
import com.angeldsis.louapi.LouState;
import com.angeldsis.louapi.LouVisData;
import com.angeldsis.louapi.RPC;
import com.angeldsis.louapi.world.Dungeon;
import com.angeldsis.louapi.world.WorldParser.Cell;

public class RPCWrap extends RPC {
	private static String TAG = "RPCWrap";
	private ChatWindow chat;
	private CoreSession core;
	public RPCWrap(Account acct, LouState state) {
		super(acct, state);
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
	public void cityChanged() {
		Log.v(TAG,"current city changed");
		core.cityChanged();
	}
	@Override
	public void onEjected() {
		// TODO Auto-generated method stub
		Log.e(TAG,"ejected");
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
	@Override public void startSubstituteSession(String sessionid) {
		// TODO Auto-generated method stub
		Log.e(TAG, "startSubstituteSession");
		// this needs to create a new CoreSession object
	}
	@Override public void cellUpdated(Cell c) {
		// TODO Auto-generated method stub
	}
	@Override public void dungeonUpdated(Dungeon d) {
		// TODO Auto-generated method stub
	}
	@Override public void onReportCountUpdate() {
		Log.w(TAG,"onReportCountUpdate");
		core.mw.onReportCountUpdate(state.viewed_reports,state.unviewed_reports);
	}
	@Override protected int getMaxPoll() {
		// TODO Auto-generated method stub
		return 10000;
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
}
