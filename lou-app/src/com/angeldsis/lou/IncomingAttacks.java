package com.angeldsis.lou;

import java.util.Iterator;

import com.angeldsis.louapi.IncomingAttack;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

public class IncomingAttacks extends SessionUser {
	private static final String TAG = "IncomingAttacks";
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.incoming_attacks);
	}
	void session_ready() {
		Log.v(TAG,""+session.state.incoming_attacks.size());
		Iterator<IncomingAttack> i = session.state.incoming_attacks.iterator();
		while (i.hasNext()) {
			IncomingAttack a = i.next();
			LinearLayout l = (LinearLayout) findViewById(R.id.list);
			TextView t1 = new TextView(this);
			TextView t2 = new TextView(this);
			t1.setText(a.targetCityName);
			t2.setText(a.playerName);
			l.addView(t1);
			l.addView(t2);
		}
	}
	@Override
	public void onPlayerData() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void gotCityData() {
		// TODO Auto-generated method stub
		
	}
}
