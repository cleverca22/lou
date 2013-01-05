package com.angeldsis.lou;

import java.util.ArrayList;

import com.angeldsis.lou.SessionKeeper.Callbacks;
import com.angeldsis.lou.SessionKeeper.MyBinder;
import com.angeldsis.louapi.ChatMsg;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class SessionUser extends FragmentActivity implements Callbacks {
	static final String TAG = "SessionUser";
	SessionKeeper mService;
	boolean mBound;
	AccountWrap acct;
	SessionKeeper.Session session;
	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		Intent msg = getIntent();
		Bundle args = msg.getExtras();
		acct = new AccountWrap(args);
	}
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	void initApi14() {
		Log.v(TAG,"doing init for api 14+");
		getActionBar().setDisplayHomeAsUpEnabled(true);
		//getActionBar().setHomeButtonEnabled(true);
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
			Log.v(TAG,"onServiceDisconnected");
			mBound = false;
		}
	};
	public void startActivity (Intent intent, Bundle options) {
		acct = new AccountWrap(options);
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
				session.state.disableVis();
				session.unsetCallback(this);
			}
			unbindService(mConnection);
			mBound = false;
			session = null;
		}
	}
	/** ignore the event for most, if any subclass needs it, override
	 */
	public void onChat(ArrayList<ChatMsg> d) {}
	public void onEjected() {}
	public void onPlayerData() {}
	public void cityChanged() {}
	public void cityListChanged() {}
	public void visDataReset() {
		Log.v(TAG,"vis count "+session.rpc.state.currentCity.visData.size()+" "+session.rpc.state.currentCity.hashCode());
	}
	public void visDataUpdated() {
		Log.v(TAG,"vis count "+session.rpc.state.currentCity.visData.size());
	}
	public void gotCityData() {}
	public void tick() {}
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.open_chat:
			Intent intent = new Intent(this,ChatWindow.class);
			intent.putExtras(acct.toBundle());
			startActivity(intent);
			return true;
		case R.id.city:
			Log.v(TAG,"opening city view");
			long heapSize = Runtime.getRuntime().maxMemory() / 1024 / 1024;
			if (heapSize > 16) {
				Intent i = new Intent(this,CityView.class);
				i.putExtras(acct.toBundle());
				startActivity(i);
			} else {
				AlertDialog.Builder b = new AlertDialog.Builder(this);
				b.setMessage("Sorry, but your device does not have enough RAM for the city view");
				b.setPositiveButton("OK", null);
				AlertDialog d = b.create();
				d.show();
			}
			return true;
		case R.id.logout:
			session.logout();
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	protected void onDestroy() {
		super.onDestroy();
		Log.v(TAG,"onDestroy");
	}
}
