package com.angeldsis.lou;

import java.net.MalformedURLException;
import com.angeldsis.lou.SessionKeeper.MyBinder;
import com.angeldsis.lou.home.Webview;
import com.angeldsis.louapi.LouSession;
import com.angeldsis.louapi.LouSession.result;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class louLogin extends Fragment {
	static String TAG = "louLogin";
	//SessionKeeper mService;
	//boolean mBound;
	EditText username,password;
	private AsyncTask<loginInfo,Integer,result> pwChecker;
	private CheckBox savepw;
	/*private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			MyBinder binder = (MyBinder)service;
			mService = binder.getService();
			mBound = true;
		}
		public void onServiceDisconnected(ComponentName arg0) {
			Log.v(TAG,"onServiceDisconnected");
			mBound = false;
		}
	};*/
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.v(TAG,"onCreateView");
		ViewGroup v = (ViewGroup) inflater.inflate(R.layout.login_page, container,false);
		username = ((EditText)v.findViewById(R.id.username));
		this.password = ((EditText)v.findViewById(R.id.password));
		this.savepw = ((CheckBox)v.findViewById(R.id.save_pw));
		((Button)v.findViewById(R.id.login)).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				louLogin.this.doLogin(null);
			}
		});
		
		String email = getActivity().getSharedPreferences("main",Context.MODE_PRIVATE).getString("email", null);
		String password = getActivity().getSharedPreferences("main",Context.MODE_PRIVATE).getString("password", null);
		if (email != null && password != null) {
			username.setText(email);
			this.password.setText(password);
			this.savepw.setChecked(true);
		}
		Log.v(TAG,"getting service");
		//Intent intent2 = new Intent(getActivity(),SessionKeeper.class);
		//getActivity().bindService(intent2,mConnection,Context.BIND_AUTO_CREATE);
		return v;
	}
	/*@Override protected void onDestroy() {
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
		super.onDestroy();
	}*/
	public void doLogin(View view) {
		String username = this.username.getText().toString();
		String password = this.password.getText().toString();
		/*synchronized (mService) {
			if (SessionKeeper.session2 == null) {
				SessionKeeper.session2 = new LouSession();
			}
		}*/
		//loginInfo info = new loginInfo();
		//info.username = username.toString();
		//info.password = password.toString();
		boolean savePw = savepw.isChecked();
		if (savePw) {
			SharedPreferences.Editor trans = getActivity().getSharedPreferences("main", Context.MODE_PRIVATE).edit();
			trans.putString("email", username);
			trans.putString("password", password);
			trans.commit();
		}
		FragmentTransaction trans = getActivity().getSupportFragmentManager().beginTransaction();
		Webview v = new Webview();
		Bundle b = new Bundle();
		b.putString("username", username);
		b.putString("password", password);
		v.setArguments(b);
		trans.replace(R.id.main_frame, v);
		trans.commit();
		/*
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
					SharedPreferences.Editor trans = getActivity().getSharedPreferences("main", Context.MODE_PRIVATE).edit();
					trans.putString("cookie", session.REMEMBER_ME);
					trans.commit();
					System.out.println("worked");
					//finish();
					Intent backtomain = new Intent(getActivity(),LouMain.class);
					startActivity(backtomain);
				}
				else {
					// FIXME
					Log.e(TAG,"something went wrong");
					return;
				}
			}
		};
		pwChecker.execute(info);*/
	}
	private static class loginInfo {
		String username,password;
	}
}
