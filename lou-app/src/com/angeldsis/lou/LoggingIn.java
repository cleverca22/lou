package com.angeldsis.lou;

import com.angeldsis.lou.fragments.RestartConfirm;
import com.angeldsis.louapi.LouState.States;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class LoggingIn extends SessionUser {
	private static final String TAG = "LoggingIn";
	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		Log.v(TAG,"onCreate");
		setContentView(R.layout.loading); // FIXME
		allow_login = true;
	}
	@Override public void loginDone() {
		Log.v(TAG,"loginDone");
		if (session.state.accountState == States.VALID) {
			Intent i = new Intent(this,SingleFragment.class);
			Log.v(TAG,"opening main "+acct.toBundle());
			i.putExtras(acct.toBundle());
			i.putExtra("fragment", LouSessionMain.class);
			startActivity(i);
			Log.v(TAG,"running intent "+i);
			finish();
		}
	}
	@Override public void onEjected(String code) {
		if (code.equals("GAMEOVER")) {
			Intent i = new Intent(this,SingleFragment.class);
			i.putExtras(acct.toBundle());
			i.putExtra("fragment", RestartConfirm.class);
		} else {
			TextView msg = new TextView(this);
			msg.setText("Error logging in: "+code);
			setContentView(msg);
		}
	}
	@Override public void session_ready() {
		Log.v(TAG,"session_ready "+session.loggingIn);
		if (!session.loggingIn) loginDone();
	}
}
