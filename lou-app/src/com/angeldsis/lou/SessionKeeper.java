package com.angeldsis.lou;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.angeldsis.LOU.Account;
import com.angeldsis.LOU.LouState;
import com.angeldsis.LOU.RPC;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class SessionKeeper extends Service {
	ArrayList<Session> sessions;
	private final IBinder binder = new MyBinder();
	public class MyBinder extends Binder {
		public SessionKeeper getService() {
			return SessionKeeper.this;
		}
	}
	public IBinder onBind(Intent arg0) {
		if (sessions == null) sessions = new ArrayList<Session>();
		return binder;
	}
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (sessions == null) sessions = new ArrayList<Session>();
		return START_NOT_STICKY;
	}
	NotificationCompat.Builder mBuilder;
	public class Session {
		private static final String TAG = "Session";
		RPC rpc;
		LouState state;
		AccountWrap acct;
		Callbacks cb;
		Session(AccountWrap acct2) {
			acct = acct2;
			Bundle options = acct.toBundle();
			Intent resultIntent = new Intent(SessionKeeper.this,LouSessionMain.class);
			resultIntent.putExtras(options);
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(SessionKeeper.this);
			stackBuilder.addParentStack(LouSessionMain.class);
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent = stackBuilder
					.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT, options);
			mBuilder.setContentIntent(resultPendingIntent);
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(SessionKeeper.NOTIFICATION_SERVICE);
			mNotificationManager.notify(0, mBuilder.build());
			state = new LouState();
			rpc = new RPCWrap(acct,state,this);
			rpc.OpenSession(true,rpc.new RPCDone() {
				public void requestDone(JSONObject reply) {
					Log.v(TAG,"session opened");
					rpc.GetServerInfo(rpc.new RPCDone() {
						public void requestDone(JSONObject reply) {
							rpc.GetPlayerInfo(rpc.new RPCDone() {
								@Override
								public void requestDone(JSONObject reply) {
									// state variable now has some data populated
									rpc.startPolling();
								}
							});
						}
					});
				}
			}, 0);
		}
		public void visDataReset() {
			if (cb != null) cb.visDataReset();
		}
		public void tick() {
			if (cb != null) cb.tick();
		}
		public void gotCityData() {
			if (cb != null) cb.gotCityData();
		}
		public void onChat(JSONArray d) {
			if (cb != null) cb.onChat(d);
		}
		public void setCallback(Callbacks cb1) {
			cb = cb1;
		}
		public void unsetCallback(Callbacks cb1) {
			if (cb == cb1) cb = null;
		}
	}
	public interface Callbacks {
		void visDataReset();
		void onChat(JSONArray d);
		void gotCityData();
		void tick();
	}
	public Session getSession(AccountWrap acct) {
		if (mBuilder == null) {
			mBuilder = new NotificationCompat.Builder(SessionKeeper.this).setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle("Lord of Ultima")
					.setContentText("LOU is still running")
					.setOngoing(true);
		}
		Iterator<Session> i = sessions.iterator();
		while (i.hasNext()) {
			Session s = i.next();
			if (s.acct.world == acct.world) return s;
		}
		Session s2 = new Session(acct);
		sessions.add(s2);
		return s2;
	}
}
