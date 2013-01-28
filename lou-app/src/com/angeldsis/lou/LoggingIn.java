package com.angeldsis.lou;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class LoggingIn extends SessionUser {
	private static final String TAG = "LoggingIn";
	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		Log.v(TAG,"onCreate");
		setContentView(R.layout.loading); // FIXME
		allow_login = true;
	}
	@Override
	public void loginDone() {
		Log.v(TAG,"loginDone");
		Intent i = new Intent(this,LouSessionMain.class);
		i.putExtras(acct.toBundle());
		startActivity(i);
		finish();
	}
	@Override
	public void session_ready() {
		Log.v(TAG,"session_ready "+session.loggingIn);
		if (!session.loggingIn) loginDone();
	}
}
