package com.angeldsis.lou;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONObject;

import com.angeldsis.louapi.ChatMsg;
import com.angeldsis.louapi.LouState;
import com.angeldsis.louapi.RPC;

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
	NotificationManager mNotificationManager;
	private final IBinder binder = new MyBinder();
	
	// constansts for notification id's
	// worldid (86) will be added to these to keep them unique
	static final int STILL_OPEN = 0x1000;
	static final int UNREAD_MESSAGE = 0x2000;
	
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
	NotificationCompat.Builder mBuilder,chatBuilder;
	public class Session {
		private static final String TAG = "Session";
		RPC rpc;
		LouState state;
		AccountWrap acct;
		Callbacks cb;
		boolean alive = false;
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
			mNotificationManager.notify(STILL_OPEN, mBuilder.build());
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
			alive = true;
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
		public void onChat(ArrayList<ChatMsg> d) {
			if (cb != null) cb.onChat(d);
			else {
				Log.v(TAG,"uncaught message");
				chatBuilder.setContentText(d.get(d.size()-1).toString());
				mNotificationManager.notify(UNREAD_MESSAGE, chatBuilder.build());
			}
		}
		public void setCallback(Callbacks cb1) {
			cb = cb1;
		}
		public void unsetCallback(Callbacks cb1) {
			if (cb == cb1) cb = null;
		}
		public void onPlayerData() {
			if (cb != null) cb.onPlayerData();
		}
		public void onEjected() {
			alive = false;
			if (cb != null) cb.onEjected();
		}
		public void cityChanged() {
			if (cb != null) cb.cityChanged();
		}
		public void logout() {
			rpc.stopPolling();
			mNotificationManager.cancel(STILL_OPEN);
			alive = false;
			sessions.remove(this);
		}
	}
	public interface Callbacks {
		void visDataReset();
		void cityChanged();
		void onEjected();
		void onPlayerData();
		void onChat(ArrayList<ChatMsg> d);
		void gotCityData();
		void tick();
	}
	public Session getSession(AccountWrap acct) {
		if (mBuilder == null) {
			mBuilder = new NotificationCompat.Builder(SessionKeeper.this).setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle("Lord of Ultima")
					.setContentText("LOU is still running")
					.setOngoing(true);
			chatBuilder = new NotificationCompat.Builder(SessionKeeper.this).setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle("Unread Message in LOU")
					.setContentText("FIXME");
			mNotificationManager = (NotificationManager) getSystemService(SessionKeeper.NOTIFICATION_SERVICE);
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
