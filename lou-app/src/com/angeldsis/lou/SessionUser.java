package com.angeldsis.lou;

import java.util.ArrayList;

import com.angeldsis.LOU.ChatMsg;
import com.angeldsis.lou.SessionKeeper.Callbacks;
import com.angeldsis.lou.SessionKeeper.MyBinder;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;

public abstract class SessionUser extends FragmentActivity implements Callbacks {
	SessionKeeper mService;
	boolean mBound;
	AccountWrap acct;
	SessionKeeper.Session session;
	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		Intent msg = getIntent();
		Bundle args = msg.getExtras();
		acct = new AccountWrap(args);
		Intent intent = new Intent(this,SessionKeeper.class);
		startService(intent);
	}
	void check_state() {
		if (session == null) {
			session = mService.getSession(acct);
			session.setCallback(this);
			session_ready();
		}
	}
	void session_ready() {}
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			MyBinder binder = (MyBinder)service;
			mService = binder.getService();
			mBound = true;
			SessionUser.this.check_state();
		}
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};
	public void startActivity (Intent intent, Bundle options) {
		acct = new AccountWrap(options);
	}
	protected void onStart() {
		super.onStart();
		Intent intent2 = new Intent(this,SessionKeeper.class);
		bindService(intent2,mConnection,BIND_AUTO_CREATE);
	}
	protected void onStop() {
		super.onStop();
		if (mBound) {
			if (session != null) session.unsetCallback(this);
			unbindService(mConnection);
			mBound = false;
		}
	}
	/** ignore the event for most, if any subclass needs it, override
	 */
	public void onChat(ArrayList<ChatMsg> d) {
	}
}
