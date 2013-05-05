package com.angeldsis.lou;

import java.util.ArrayList;

import com.angeldsis.lou.SessionKeeper.Callbacks;
import com.angeldsis.lou.SessionKeeper.MyBinder;
import com.angeldsis.lou.allianceforum.AllianceForumList;
import com.angeldsis.lou.city.SendTrade;
import com.angeldsis.lou.fragments.FoodWarnings;
import com.angeldsis.lou.home.DisconnectedDialog;
import com.angeldsis.lou.world.DungeonList;
import com.angeldsis.louapi.ChatMsg;
import com.angeldsis.louapi.IncomingAttack;
import com.angeldsis.louapi.LouVisData;
import com.angeldsis.louapi.RPC.GetLockboxURLDone;
import com.angeldsis.louapi.world.WorldParser.Cell;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class SessionUser extends FragmentActivity implements Callbacks {
	private static final String TAG = "SessionUser";
	SessionKeeper mService;
	boolean mBound;
	protected AccountWrap acct;
	protected SessionKeeper.Session session;
	boolean allow_login;
	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		allow_login = false;
		Intent msg = getIntent();
		Bundle args = msg.getExtras();
		acct = new AccountWrap(args);
		setTheme(SessionUser.getCurrentTheme(this));
	}
	public static int getCurrentTheme(Context c) {
		SharedPreferences p = c.getSharedPreferences("com.angeldsis.lou_preferences",MODE_PRIVATE);
		String theme = p.getString("theme","holo");
		if (theme.equals("holo")) return android.R.style.Theme_Holo;
		else if (theme.equals("holo_light")) return android.R.style.Theme_Holo_Light;
		else if (theme.equals("lou")) return R.style.theme1;
		else return android.R.style.Theme;
	}
	public void userActive() {
		session.state.userActivity = true;
	}
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	protected void initApi14() {
		Log.v(TAG,"doing init for api 14+");
		getActionBar().setDisplayHomeAsUpEnabled(true);
		//getActionBar().setHomeButtonEnabled(true);
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
		cityChanged();
		gotCityData();
	}
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
	/*public void startActivity (Intent intent, Bundle options) {
		Log.v(TAG,"options "+options);
		Log.v(TAG,"options2 "+intent.getExtras());
		Log.v(TAG,"acct "+acct);
		if (options != null) acct = new AccountWrap(options);
	}*/
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
	/** ignore the event for most, if any subclass needs it, override
	 */
	public void onChat(ArrayList<ChatMsg> d) {}
	public void onEjected() {
		// keep in sync with FragmentUser.onEjected
		Log.v(TAG,"you have been logged out");
		// FIXME give a better error
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
		if (prev != null) ft.remove(prev);
		ft.addToBackStack(null);
		DialogFragment f = DisconnectedDialog.newInstance();
		f.show(ft, "dialog");
	}
	public void onPlayerData() {}
	public void cityChanged() {}
	public void cityListChanged() {}
	public void visDataReset() {
		Log.v(TAG,"vis count "+session.rpc.state.currentCity.visData.size()+" "+session.rpc.state.currentCity.hashCode());
	}
	public void visDataUpdated() {
		Log.v(TAG,"vis count "+session.rpc.state.currentCity.visData.size());
	}
	/** triggers with data for the current city has changed **/
	public void gotCityData() {}
	public void tick() {}
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		switch (item.getItemId()) {
		case R.id.open_chat:
			Intent intent = new Intent(this,ChatWindow.class);
			intent.putExtras(acct.toBundle());
			startActivity(intent);
			return true;
		case R.id.city:
			Log.v(TAG,"opening city view");
			long heapSize = Runtime.getRuntime().maxMemory() / 1024 / 1024;
			if (heapSize > 15) {
				i = new Intent(this,CityView.class);
				i.putExtras(acct.toBundle());
				startActivity(i);
			} else {
				AlertDialog.Builder b = new AlertDialog.Builder(this);
				b.setMessage(R.string.low_ram);
				b.setPositiveButton(R.string.ok, null);
				AlertDialog d = b.create();
				d.show();
			}
			return true;
		case R.id.subs:
			i = new Intent(this,Options.class);
			i.putExtras(acct.toBundle());
			startActivity(i);
			return true;
		case R.id.options:
			i = new Intent(this,Settings.class);
			Log.v(TAG,"opening settings "+i);
			startActivity(i);
			return true;
		case R.id.logout:
			session.logout();
			finish();
			return true;
		case R.id.getfunds:
			session.rpc.GetLockboxURL(new GetLockboxURLDone() {
				@Override public void done(String reply) {
					Log.v(TAG,"got url:"+reply);
					Uri location = Uri.parse(reply);
					Intent buyFunds = new Intent(Intent.ACTION_VIEW,location);
					startActivity(buyFunds);
				}});
			return true;
		case R.id.allianceForum:
			i = new Intent(this,AllianceForumList.class);
			i.putExtras(acct.toBundle());
			startActivity(i);
			return true;
		case R.id.sendTrade:
			i = new Intent(this,SendTrade.class);
			i.putExtras(acct.toBundle());
			startActivity(i);
			return true;
		case R.id.bqo:
			i = new Intent(this,BuildQueueOverview.class);
			i.putExtras(acct.toBundle());
			startActivity(i);
			return true;
		case R.id.dungeonlist:
			i = new Intent(this,DungeonList.class);
			i.putExtras(acct.toBundle());
			startActivity(i);
			return true;
		case R.id.idleunits:
			i = new Intent(this,IdleUnits.class);
			i.putExtras(acct.toBundle());
			startActivity(i);
			return true;
		case R.id.update:
			Uri location = Uri.parse("http://andoria.angeldsis.com/apks/LouMain.apk");
			i = new Intent(Intent.ACTION_VIEW,location);
			startActivity(i);
			return true;
		case R.id.el_city_list:
			i = new Intent(this,EnlightenedCityList.class);
			i.putExtras(acct.toBundle());
			startActivity(i);
			return true;
		case R.id.foodWarning:
			i = new Intent(this,SingleFragment.class);
			i.putExtras(acct.toBundle());
			i.putExtra("fragment", FoodWarnings.class);
			startActivity(i);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	protected void onDestroy() {
		super.onDestroy();
		Log.v(TAG,"onDestroy");
	}
	@Override public void loginDone() {}
	@Override public void onVisObjAdded(LouVisData[] v) {}
	@Override public boolean onNewAttack(IncomingAttack a) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override public void onReportCountUpdate() {}
	@Override public void onSubListChanged() {}
	@Override public void onBuildQueueUpdate() {}
	@Override public void cellUpdated(Cell c) {}
	@Override public void onDefenseOverviewUpdate() {}
	@Override public void onEnlightenedCityChanged() {}
	@Override public void onFoodWarning() {}
}
