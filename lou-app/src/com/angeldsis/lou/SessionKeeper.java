package com.angeldsis.lou;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

import org.json2.JSONObject;

import com.angeldsis.lou.chat.ChatHistory;
import com.angeldsis.louapi.ChatMsg;
import com.angeldsis.louapi.IncomingAttack;
import com.angeldsis.louapi.LouSession;
import com.angeldsis.louapi.LouSession.result;
import com.angeldsis.louapi.LouState;
import com.angeldsis.louapi.LouState.City;
import com.angeldsis.louapi.LouVisData;
import com.angeldsis.louapi.RPC;
import com.angeldsis.louapi.RPC.RPCDone;
import com.angeldsis.louapi.world.Dungeon;
import com.angeldsis.louapi.world.WorldParser.Cell;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.nullwire.trace.ExceptionHandler;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class SessionKeeper extends Service {
	static final String TAG = "SessionKeeper";
	ArrayList<Session> sessions;
	int lastSessionid = 0;
	NotificationManager mNotificationManager;
	private final IBinder binder = new MyBinder();
	public static LouSession session2;
	PowerManager.WakeLock wl,doing_network;
	RpcLogs rpclogs;
	/* basic wakelock logic
	 * when any worker thread goes idle (between calling setTimer and Thread.sleep)
	 * it will check to see if others are active, if everything is going idle, the wakelock gets released
	 * causing the device to enter low-power mode
	 * 
	 * if the device is running normally when Thread.sleep expires, it will simply run normally
	 * 
	 * if the device was in sleep mode, the ALarmManager will fire 10 seconds late, and grab the wakelock
	 * then .interrupt() every worker thread (and force the thread to active state)
	 * each thread will then check its own state, and set threadActive if its busy or not
	 * 
	 * once all threads become idle, the wakelock gets released and the device is free to shut the
	 * cpu back off
	 */
	boolean lockLocked;
	AlarmManager alarmManager;
	private static SessionKeeper self;
	PendingIntent wakeSelf = null;
	
	// constansts for notification id's
	// worldid (86) will be added to these to keep them unique
	static final int STILL_OPEN = 0x100;
	static final int UNREAD_MESSAGE = 0x200;
	static final int INCOMING_ATTACK = 0x300;

	public class MyBinder extends Binder {
		public SessionKeeper getService() {
			Log.v(TAG,"getService");
			return SessionKeeper.this;
		}
	}
	public SessionKeeper() {
		super();
		Log.v(TAG,this+" constructor");
	}
	public IBinder onBind(Intent arg0) {
		Log.v(TAG,"onBind");
		return binder;
	}
	@Override
	public void onCreate() {
		ExceptionHandler.register(this,"http://ext.earthtools.ca/backtrace.php");
		Logger.init(); // allows api to print to log
		Log.v(TAG,"onCreate");
		if (sessions == null) sessions = new ArrayList<Session>();
		PowerManager pm = (PowerManager) this.getSystemService(POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "incoming attack");
		// partial lock seems to not effect cpu freq scalling on the kindle
		doing_network = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "lou logged in");
		self = this;
		alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(this,SessionKeeper.class);
		i.putExtra("wakingSelf", true);
		wakeSelf = PendingIntent.getService(this,0,i,0);
		rpclogs = new RpcLogs(this);
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v(TAG,this+" onDestroyed");
		self = null;
	}
	static SessionKeeper getInstance() {
		return self;
	}
	void refreshConfig() {
		// FIXME, must be called when settings change
		SharedPreferences p = getSharedPreferences("com.angeldsis.lou_preferences",MODE_PRIVATE);
		boolean monitor = p.getBoolean("monitor_alliance_attacks",false);
		Log.v(TAG,"updating monitor config to "+monitor);
		for (Session s : sessions) {
			s.refreshConfig(monitor);
		}
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//AccountWrap a = new AccountWrap(intent.getExtras());
		//Log.v(TAG,"onStartCommand "+a.world);
		if (intent.getExtras().containsKey("wakingSelf")) {
			Log.v(TAG,"waking self");
			for (Session s : sessions) {
				s.wakeUp();
			}
			if (sessions.size() > 0) {
				//Log.v(TAG,"getting wakelock");
				synchronized(this) {
					if (lockLocked == false) {
						lockLocked = true;
						doing_network.acquire();
					}
				}
			}
			checkState("woke self");
		}
		return START_NOT_STICKY;
	}
	NotificationCompat.Builder mBuilder,chatBuilder,incomingAttackBuilder;
	public class Session {
		private static final String TAG = "Session";
		public RPC rpc;
		LouState state;
		AccountWrap acct;
		Callbacks cb;
		boolean alive = false,loggingIn;
		int sessionid;
		public ChatHistory chat;
		boolean threadActive;
		Session(AccountWrap acct2, int sessionid) {
			Log.v(TAG,"new Session");
			loggingIn = true;
			acct = acct2;
			// FIXME give the playerid#
			chat = new ChatHistory(SessionKeeper.this,acct2.worldid,0);
			this.sessionid = sessionid;

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
			mBuilder.setContentText("LOU is still running "+acct.world);
			// FIXME, this interface can only support 1 notif, replace it with one that opens an active session list
			SessionKeeper.this.startForeground(STILL_OPEN | sessionid,mBuilder.build());
			//doing_network.acquire();
			
			state = new LouState();
			restoreState(); // FIXME, maybe do this better?

			rpc = new RPCWrap(acct,state,this);
			state.setRPC(rpc);
			rpc.OpenSession(true,new RPCDone() {
				public void requestDone(JSONObject reply) {
					Log.v(TAG,"session opened");
					rpc.GetServerInfo(new RPCDone() {
						public void requestDone(JSONObject reply) {
							rpc.GetPlayerInfo(new RPCDone() {
								@Override
								public void requestDone(JSONObject reply) {
									// state variable now has some data populated
									rpc.startPolling();
									loggingIn = false;
									loginDone();
								}
							});
						}
					});
				}
			});
			alive = true;
		}
		public void wakeUp() {
			threadActive = true;
			rpc.interrupt();
		}
		public void refreshConfig(boolean monitor) {
			rpc.refreshConfig(monitor);
		}
		private void loginDone() {
			if (cb != null) cb.loginDone();
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
			chat.onChat(d);
			for (ChatMsg m : d) {
				Log.v(TAG,m.toString());
			}
			if (cb != null) cb.onChat(d);
			else {
				//Log.v(TAG,"uncaught message");
				
				Bundle options = acct.toBundle();
				Intent resultIntent = new Intent(SessionKeeper.this,ChatWindow.class);
				Intent homeIntent = new Intent(SessionKeeper.this,LouSessionMain.class);
				// FIXME, include chat details, to open tab
				resultIntent.putExtras(options);
				homeIntent.putExtras(options);
				TaskStackBuilder stackBuilder = TaskStackBuilder.create(SessionKeeper.this);
				stackBuilder.addParentStack(ChatWindow.class);
				stackBuilder.addNextIntent(homeIntent);
				stackBuilder.addNextIntent(resultIntent);
				PendingIntent resultPendingIntent = stackBuilder
						.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT, options);
				chatBuilder.setContentIntent(resultPendingIntent);

				chatBuilder.setContentText(d.get(d.size()-1).toString());
				mNotificationManager.notify(UNREAD_MESSAGE | sessionid, chatBuilder.build());
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
			Gson gson = new Gson();
			FileOutputStream state;
			try {
				state = SessionKeeper.this.openFileOutput("state_save.tmp", MODE_PRIVATE);
				String data1 = gson.toJson(this.state);
				byte[] data2 = data1.getBytes();
				state.write(data2);
				state.close();
				File source = SessionKeeper.this.getFileStreamPath("state_save.tmp");
				File dest = SessionKeeper.this.getFileStreamPath("state_save");
				source.renameTo(dest);
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
			File source = SessionKeeper.this.getFileStreamPath("state_save");
			try {
				state = new FileInputStream(source);
				Gson gson = new Gson();
				this.state = gson.fromJson(new InputStreamReader(state), LouState.class);
				
				Log.v(TAG,"state:"+this.state);
				Log.v(TAG,"cities:"+this.state.cities);
				Iterator<City> i = this.state.cities.iterator();
				while (i.hasNext()) i.next().fix(this.state);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				source.delete();
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				source.delete();
			}
		}
		public void onPlayerData() {
			if (cb != null) cb.onPlayerData();
		}
		public void onEjected() {
			alive = false;
			if (cb != null) cb.onEjected();
			//mNotificationManager.cancel(STILL_OPEN | sessionid);
			SessionKeeper.this.stopForeground(true);
			sessions.remove(this);
			rpc.stopLooping();
			//doing_network.release();
			teardown();
		}
		public void cityChanged() {
			if (cb != null) cb.cityChanged();
		}
		public void logout() {
			rpc.stopPolling();
			rpc.stopLooping();
			//mNotificationManager.cancel(STILL_OPEN | sessionid);
			SessionKeeper.this.stopForeground(true);
			alive = false;
			sessions.remove(this);
			//doing_network.release();
			teardown();
		}
		public void cityListChanged() {
			if (cb != null) cb.cityListChanged();
		}
		public void vidDataUpdated() {
			if (cb != null) cb.visDataUpdated();
		}
		public void onVisObjAdded(LouVisData[] v) {
			if (cb != null) cb.onVisObjAdded(v);
		}
		public void onNewAttack(IncomingAttack a) {
			boolean handled = false;
			if (cb != null) handled = cb.onNewAttack(a);
			Log.v(TAG,handled + " new incoming attack!!! "+a);
			if (!handled) {
				String msg = String.format("incoming attack from %s to %s", a.sourcePlayerName,a.targetCityName);
				if (a.targetIsMe) msg += ", you are the target";
				long end = rpc.state.stepToMilis(a.end);
				incomingAttackBuilder.setContentText(msg)
					.setDefaults(Notification.DEFAULT_SOUND)
					.setWhen(end);
				long start = rpc.state.stepToMilis(a.start);
				
				Bundle options = acct.toBundle();
				Intent resultIntent = new Intent(SessionKeeper.this,IncomingAttacks.class);
				Intent homeIntent = new Intent(SessionKeeper.this,LouSessionMain.class);
				resultIntent.putExtras(options);
				homeIntent.putExtras(options);
				TaskStackBuilder stackBuilder = TaskStackBuilder.create(SessionKeeper.this);
				stackBuilder.addParentStack(IncomingAttacks.class);
				stackBuilder.addNextIntent(homeIntent);
				stackBuilder.addNextIntent(resultIntent);
				PendingIntent resultPendingIntent = stackBuilder
						.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT, options);
				incomingAttackBuilder.setContentIntent(resultPendingIntent);

				
				Notification n = incomingAttackBuilder.build();
				int id = (INCOMING_ATTACK | sessionid) + (a.id << 15);
				Log.v(TAG,String.format("id:0x%x,sessionid:%d, id:%d",id,sessionid,a.id));
				mNotificationManager.notify(id, n);
				wl.acquire(60000);
			}
		}
		public void onReportCountUpdate() {
			if (cb != null) cb.onReportCountUpdate();
			else Log.v(TAG,String.format("report update viewed:%d unviewed:%d",state.viewed_reports,state.unviewed_reports));
		}
		public void onSubListChanged() {
			if (cb != null) cb.onSubListChanged();
		}
		public void startSubstituteSession(String sessionid) {
			AccountWrap acct2 = new AccountWrap(acct);
			acct2.sessionid = sessionid;
			acct2.id = lastSessionid++;
			Session s2 = new Session(acct2,acct2.id);
			sessions.add(s2);
		}
		private void teardown() {
			chat.teardown();
		}
		public int getMaxPoll() {
			if (cb == null) return 150000;
			else return 10000;
		}
		public void setTimer(long maxdelay) {
			SessionKeeper.this.setTimer(maxdelay);
		}
		public void setThreadActive(boolean b) {
			if (b == true) {
				synchronized(this) {
					if (lockLocked == false) {
						lockLocked = true;
						doing_network.acquire();
					}
				}
			}
			threadActive = b;
			SessionKeeper.this.checkState("setThreadActive "+b);
		}
		public void onBuildQueueUpdate() {
			if (cb != null) cb.onBuildQueueUpdate();
		}
		public boolean uiActive() {
			return cb != null;
		}
		public void logRequest(int req, int reply, String func) {
			rpclogs.logRequest(req,reply,func);
		}
		public void cellUpdated(Cell c) {
			if (cb != null) cb.cellUpdated(c);
		}
		public void dungeonUpdated(Dungeon d) {
			// TODO Auto-generated method stub
			
		}
		public void onDefenseOverviewUpdate() {
			if (cb != null) cb.onDefenseOverviewUpdate();
		}
	}
	public void setTimer(long maxdelay) {
		long target = System.currentTimeMillis() + maxdelay + 10000;
		//Log.v(TAG,"setup alarm manager");
		// FIXME, doesnt run the first timer, when 2 sessions are live
		alarmManager.set(AlarmManager.RTC_WAKEUP, target, wakeSelf);
	}
	public interface Callbacks {
		void visDataReset();
		void onDefenseOverviewUpdate();
		void cellUpdated(Cell c);
		void onBuildQueueUpdate();
		void onSubListChanged();
		void onReportCountUpdate();
		boolean onNewAttack(IncomingAttack a);
		void onVisObjAdded(LouVisData[] v);
		void loginDone();
		void visDataUpdated();
		void cityListChanged();
		void cityChanged();
		void onEjected();
		void onPlayerData();
		void onChat(ArrayList<ChatMsg> d);
		void gotCityData();
		void tick();
	}
	public Session getSession(AccountWrap acct, boolean allow_login) {
		Log.v(TAG,"getSession(world:"+acct.world+")");
		if (mBuilder == null) {
			mBuilder = new NotificationCompat.Builder(SessionKeeper.this).setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle("Lord of Ultima")
					.setOngoing(true);
			chatBuilder = new NotificationCompat.Builder(SessionKeeper.this).setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle("Unread Message in LOU")
					.setContentText("FIXME")
					.setAutoCancel(true);
			long[] pattern = { 100, 1000, 100, 1000 };
			incomingAttackBuilder = new NotificationCompat.Builder(SessionKeeper.this).setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle("incoming attack!")
					.setContentText("FIXME")
					.setAutoCancel(true)
					.setVibrate(pattern);
			mNotificationManager = (NotificationManager) getSystemService(SessionKeeper.NOTIFICATION_SERVICE);
			Logger.init();
		}
		Log.v(TAG,"looking for existing session");
		Iterator<Session> i = sessions.iterator();
		while (i.hasNext()) {
			Session s = i.next();
			if (s.acct.world.equals(acct.world)) {
				Log.v(TAG,"found it");
				return s;
			}
		}
		if (allow_login) {
			Session s2 = new Session(acct,lastSessionid++);
			sessions.add(s2);
			refreshConfig();
			return s2;
		} else return null; // not a login page, fail out
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
		Logger.init();
		Log.v(TAG,"restore_cookie("+cookie+")");
		if (session2 == null) session2 = new LouSession();
		session2.restore_cookie(cookie);
	}
	@Override public void onTrimMemory(int level) {
		switch (level) {
		case 0:
			Log.v(TAG,"onTrimMemory("+level+")");
			break;
		case TRIM_MEMORY_UI_HIDDEN:
			Log.v(TAG,"onTrimMemory(ui hidden or worse "+level+")");
			Log.v(TAG,"session count: "+sessions.size());
		}
		checkState("onTrimMemory");
	}
	public void onLowMemory () {
		Log.v(TAG,"onLowMemory");
		checkState("onLowMemory");
	}
	public void checkState(String reason) {
		// checks things like the number of open sessions and then hides/shows the notification
		// and clears/sets the foreground service flag
		
		// FIXME, check service idle time incase it was in the middle of starting up
		if (sessions.size() == 0) {
			Log.v(TAG,"stopping self");
			stopSelf();
		}
		synchronized (this) {
			if (lockLocked) {
				//Log.v(TAG,"checkState "+reason+" session count: "+sessions.size());
				boolean anyActive = false;
				for (Session s : sessions) if (s.threadActive) anyActive = true;
				//Log.v(TAG,"active:"+anyActive);
				if ((anyActive == false) && lockLocked) {
					//Log.v(TAG,"releasing wakelock");
					doing_network.release();
					lockLocked = false;
				} else {
					Log.v(TAG,"leaving it locked "+anyActive);
				}
			} else {
				Log.v(TAG,"lock not locked");
			}
		}
	}
}
