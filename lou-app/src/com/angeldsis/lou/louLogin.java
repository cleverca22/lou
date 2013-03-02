package com.angeldsis.lou;

import java.net.MalformedURLException;
import com.angeldsis.lou.SessionKeeper.MyBinder;
import com.angeldsis.louapi.LouSession;
import com.angeldsis.louapi.LouSession.result;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

public class louLogin extends Activity {
	static String TAG = "louLogin";
	SessionKeeper mService;
	boolean mBound;
	private AsyncTask<loginInfo,Integer,result> pwChecker;
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			MyBinder binder = (MyBinder)service;
			mService = binder.getService();
			mBound = true;
		}
		public void onServiceDisconnected(ComponentName arg0) {
			Log.v(TAG,"onServiceDisconnected");
			mBound = false;
		}
	};
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG,"onCreate");
		setContentView(R.layout.login_page);
		String email = getSharedPreferences("main",MODE_PRIVATE).getString("email", null);
		String password = getSharedPreferences("main",MODE_PRIVATE).getString("password", null);
		if (email != null && password != null) {
			((EditText)findViewById(R.id.username)).setText(email);
			((EditText)findViewById(R.id.password)).setText(password);
			((CheckBox)findViewById(R.id.save_pw)).setChecked(true);
		}
		Log.v(TAG,"getting service");
		Intent intent2 = new Intent(this,SessionKeeper.class);
		bindService(intent2,mConnection,BIND_AUTO_CREATE);
	}
	@Override protected void onDestroy() {
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
		super.onDestroy();
	}
	public void doLogin(View view) throws MalformedURLException {
		String username = ((EditText)findViewById(R.id.username)).getText().toString();
		String password = ((EditText)findViewById(R.id.password)).getText().toString();
		synchronized (mService) {
			if (SessionKeeper.session2 == null) {
				SessionKeeper.session2 = new LouSession();
			}
		}
		loginInfo info = new loginInfo();
		info.username = username.toString();
		info.password = password.toString();
		CheckBox save = (CheckBox) findViewById(R.id.save_pw);
		boolean savePw = save.isChecked();
		if (savePw) {
			SharedPreferences.Editor trans = this.getSharedPreferences("main", MODE_PRIVATE).edit();
			trans.putString("email", info.username);
			trans.putString("password", info.password);
			trans.commit();
		}
		Log.v(TAG,"starting login");
		pwChecker  = new AsyncTask<loginInfo,Integer,result>(){
			@Override protected result doInBackground(loginInfo... params) {
				loginInfo info = params[0];
				LouSession session = SessionKeeper.session2;
				// FIXME, give the user feedback
				return session.startLogin(info.username, info.password);
			}
			protected void onPostExecute(result reply) {
				LouSession session = SessionKeeper.session2;
				if (reply.error) {
					// FIXME
					Log.e(TAG,reply.errmsg);
					reply.e.printStackTrace();
				}
				if (reply.worked) {
					SharedPreferences.Editor trans = getSharedPreferences("main", MODE_PRIVATE).edit();
					trans.putString("cookie", session.REMEMBER_ME);
					trans.commit();
					System.out.println("worked");
					finish();
					Intent backtomain = new Intent(louLogin.this,LouMain.class);
					startActivity(backtomain);
				}
				else {
					// FIXME
					Log.e(TAG,"something went wrong");
					return;
				}
			}
		};
		pwChecker.execute(info);
	}
	private static class loginInfo {
		String username,password;
	}
}
