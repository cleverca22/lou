package com.angeldsis.lou;

import java.util.ArrayList;
import java.util.Iterator;

import com.angeldsis.lou.SessionKeeper.Callbacks;
import com.angeldsis.lou.SessionKeeper.MyBinder;
import com.angeldsis.lou.home.DisconnectedDialog;
import com.angeldsis.louapi.ChatMsg;
import com.angeldsis.louapi.IncomingAttack;
import com.angeldsis.louapi.LouVisData;
import com.angeldsis.louapi.world.WorldParser.Cell;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

public class FragmentUser extends FragmentActivity implements Callbacks {
	private static final String TAG = "FragmentUser";
	SessionKeeper mService;
	boolean mBound;
	public SessionKeeper.Session session;
	boolean allow_login;
	protected AccountWrap acct;
	private ArrayList<FragmentBase> hooks = new ArrayList<FragmentBase>();
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			MyBinder binder = (MyBinder)service;
			mService = binder.getService();
			mBound = true;
			FragmentUser.this.check_state();
		}
		public void onServiceDisconnected(ComponentName arg0) {
			Log.v(TAG,"onServiceDisconnected");
			mBound = false;
		}
	};
	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		allow_login = false;
		Intent msg = getIntent();
		Bundle args = msg.getExtras();
		acct = new AccountWrap(args);
		setTheme(SessionUser.getCurrentTheme(this));
	}
	protected void onStart() {
		super.onStart();
		Log.v(TAG,"onStart");
		Intent intent2 = new Intent(this,SessionKeeper.class);
		bindService(intent2,mConnection,BIND_AUTO_CREATE);
	}
	protected void onStop() {
		super.onStop();
		Log.v(TAG,"onStop");
		if (mBound) {
			if (session != null) {
				session.unsetCallback(this);
			}
			unbindService(mConnection);
			mBound = false;
			session = null;
		}
	}
	void check_state() {
		if (session == null) {
			session = mService.getSession(acct,allow_login);
			Log.v(TAG,this+" allow login: "+allow_login+" service "+session);
			if (session == null) {
				finish();
				return;
			}
			session.setCallback(this);
			Log.v(TAG,"calling session ready");
			session_ready();
			userActive();
		}
	}
	public void session_ready() {
		onCityChanged();
		gotCityData();
		Iterator<FragmentBase> i = hooks.iterator();
		while (i.hasNext()) i.next().session_ready();
	}
	public void userActive() {
		session.state.userActivity = true;
	}
	@Override public void visDataReset() {}
	@Override public void onFoodWarning() {
		Iterator<FragmentBase> i = hooks.iterator();
		while (i.hasNext()) i.next().onFoodWarning();
	}
	@Override public void onEnlightenedCityChanged() {}
	@Override public void onDefenseOverviewUpdate() {}
	@Override public void cellUpdated(Cell c) {}
	@Override public void onBuildQueueUpdate() {}
	@Override
	public void onSubListChanged() {
	}
	@Override
	public void onReportCountUpdate() {
	}
	@Override
	public boolean onNewAttack(IncomingAttack a) {
		return false;
	}
	@Override
	public void onVisObjAdded(LouVisData[] v) {
	}
	@Override
	public void loginDone() {
	}
	@Override
	public void visDataUpdated() {
	}
	@Override
	public void cityListChanged() {
	}
	@Override
	public void onCityChanged() {
	}
	@Override
	public void onEjected() {
		// keep in sync with SessionUser.onEjected
		Log.v(TAG,"you have been logged out");
		// FIXME give a better error
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
		if (prev != null) ft.remove(prev);
		ft.addToBackStack(null);
		DialogFragment f = DisconnectedDialog.newInstance();
		f.show(ft, "dialog");
	}
	@Override
	public void onPlayerData() {
	}
	@Override
	public void onChat(ArrayList<ChatMsg> d) {
	}
	@Override
	public void gotCityData() {
	}
	@Override
	public void tick() {
	}
	public void addHook(FragmentBase fragmentBase) {
		hooks.add(fragmentBase);
	}
	public void removeHook(FragmentBase fragmentBase) {
		hooks.remove(fragmentBase);
	}
}
