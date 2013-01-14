package com.angeldsis.lou;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Iterator;

import org.json2.JSONObject;

import com.angeldsis.louapi.ChatMsg;
import com.angeldsis.louapi.LouSession;
import com.angeldsis.louapi.LouSession.result;
import com.angeldsis.louapi.LouState;
import com.angeldsis.louapi.LouState.City;
import com.angeldsis.louapi.RPC;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class SessionKeeper extends Service {
	static final String TAG = "SessionKeeper";
	ArrayList<Session> sessions;
	NotificationManager mNotificationManager;
	private final IBinder binder = new MyBinder();
	public static LouSession session2;
	
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
		return binder;
	}
	@Override
	public void onCreate() {
		Logger.init(); // allows api to print to log
		Log.v(TAG,"onCreate");
		if (sessions == null) sessions = new ArrayList<Session>();
	}
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (sessions == null) sessions = new ArrayList<Session>();
		AccountWrap a = new AccountWrap(intent.getExtras());
		Log.v(TAG,"onStartCommand "+a.world);
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

			Intent intent = new Intent(SessionKeeper.this,SessionKeeper.class);
			intent.putExtras(acct.toBundle());
			startService(intent);

			Bundle options = acct.toBundle();
			Intent resultIntent = new Intent(SessionKeeper.this,LouSessionMain.class);
			resultIntent.putExtras(options);
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(SessionKeeper.this);
			stackBuilder.addParentStack(LouSessionMain.class);
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent = stackBuilder
					.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT, options);
			mBuilder.setContentIntent(resultPendingIntent);
			mNotificationManager.notify(STILL_OPEN | acct.worldid, mBuilder.build());
			
			state = new LouState();
			Log.v(TAG,""+state.chat_history);
			restoreState(); // FIXME, maybe do this better?
			Log.v(TAG,""+state.chat_history);

			rpc = new RPCWrap(acct,state,this);
			state.setRPC(rpc);
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
			});
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
				
				Bundle options = acct.toBundle();
				Intent resultIntent = new Intent(SessionKeeper.this,ChatWindow.class);
				// FIXME, include chat details, to open tab
				resultIntent.putExtras(options);
				TaskStackBuilder stackBuilder = TaskStackBuilder.create(SessionKeeper.this);
				stackBuilder.addParentStack(ChatWindow.class);
				stackBuilder.addNextIntent(resultIntent);
				PendingIntent resultPendingIntent = stackBuilder
						.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT, options);
				chatBuilder.setContentIntent(resultPendingIntent);

				chatBuilder.setContentText(d.get(d.size()-1).toString());
				mNotificationManager.notify(UNREAD_MESSAGE | acct.worldid, chatBuilder.build());
			}
		}
		public void setCallback(Callbacks cb1) {
			cb = cb1;
		}
		public void unsetCallback(Callbacks cb1) {
			if (cb == cb1) {
				state.disableVis();
				cb = null;
			}
			saveState();
		}
		private void saveState() {
			FileOutputStream state;
			try {
				state = SessionKeeper.this.openFileOutput("state_save", MODE_PRIVATE);
				ObjectOutputStream oos = new ObjectOutputStream(state);
				oos.writeObject(this.state);
				oos.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		public void restoreState() {
			FileInputStream state;
			try {
				state = SessionKeeper.this.openFileInput("state_save");
				ObjectInputStream ois = new ObjectInputStream(state);
				this.state = (LouState) ois.readObject();
				
				Iterator<City> i = this.state.cities.iterator();
				while (i.hasNext()) i.next().fix(this.state);
			} catch (FileNotFoundException e) {
			} catch (StreamCorruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		public void onPlayerData() {
			if (cb != null) cb.onPlayerData();
		}
		public void onEjected() {
			alive = false;
			if (cb != null) cb.onEjected();
			mNotificationManager.cancel(STILL_OPEN | acct.worldid);
			sessions.remove(this);
		}
		public void cityChanged() {
			if (cb != null) cb.cityChanged();
		}
		public void logout() {
			rpc.stopPolling();
			mNotificationManager.cancel(STILL_OPEN | acct.worldid);
			alive = false;
			sessions.remove(this);
		}
		public void cityListChanged() {
			if (cb != null) cb.cityListChanged();
		}
		public void vidDataUpdated() {
			if (cb != null) cb.visDataUpdated();
		}
	}
	public interface Callbacks {
		void visDataReset();
		void visDataUpdated();
		void cityListChanged();
		void cityChanged();
		void onEjected();
		void onPlayerData();
		void onChat(ArrayList<ChatMsg> d);
		void gotCityData();
		void tick();
	}
	public Session getSession(AccountWrap acct) {
		Log.v(TAG,"getSession");
		if (mBuilder == null) {
			mBuilder = new NotificationCompat.Builder(SessionKeeper.this).setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle("Lord of Ultima")
					.setContentText("LOU is still running")
					.setOngoing(true);
			chatBuilder = new NotificationCompat.Builder(SessionKeeper.this).setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle("Unread Message in LOU")
					.setContentText("FIXME")
					.setAutoCancel(true);
			mNotificationManager = (NotificationManager) getSystemService(SessionKeeper.NOTIFICATION_SERVICE);
			Logger.init();
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
	public static void checkCookie(final CookieCallback cb) {
		Log.v(TAG,"checkCookie");
		if (session2 == null) session2 = new LouSession();
		AsyncTask<Object,Integer,result> task = new AsyncTask<Object,Integer,result>() {
			@Override
			protected result doInBackground(Object... arg0) {
				result r = session2.check_cookie();
				return r;
			}
			protected void onPostExecute(result result) {
				cb.done(result);
			}
		};
		task.execute();
	}
	public interface CookieCallback {
		void done(result r);
	}
	public static void restore_cookie(String cookie) {
		Log.v(TAG,"restore_cookie("+cookie+")");
		if (session2 == null) session2 = new LouSession();
		session2.restore_cookie(cookie);
	}
	@Override
	public void onTrimMemory(int level) {
		switch (level) {
		case 0:
			Log.v(TAG,"onTrimMemory("+level+")");
			break;
		case TRIM_MEMORY_UI_HIDDEN:
			Log.v(TAG,"onTrimMemory(ui hidden or worse "+level+")");
		}
	}
}
