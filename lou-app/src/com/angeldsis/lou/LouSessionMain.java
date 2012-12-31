package com.angeldsis.lou;

import java.util.ArrayList;

import com.angeldsis.LOU.ChatMsg;
import com.angeldsis.lou.fragments.ResourceBar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LouSessionMain extends SessionUser implements SessionKeeper.Callbacks {
	static final String TAG = "LouSessionMain";
	ResourceBar resource_bar;

	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		Log.v(TAG,"onCreate");
		setContentView(R.layout.session_core);
		resource_bar = new ResourceBar();
		getSupportFragmentManager().beginTransaction().add(R.id.resource_bar, resource_bar).commit();
	}
	protected void onStart() {
		super.onStart();
		Log.v(TAG,"onStart");
	}
	protected void session_ready() {
		Log.v(TAG,"session_ready");
		if (session.alive == false) {
			onEjected();
		} else {
			int total = session.state.chat_history.size();
			Button chat = (Button) findViewById(R.id.chat);
			chat.setText("chat ("+total+")");
			updateTickers();
		}
	}
	public void cityChanged() {
		TextView city = (TextView) findViewById(R.id.current_city);
		city.setText(session.state.currentCity.name);
	}
	public void onEjected() {
		Log.v(TAG,"you have been logged out");
		// FIXME give a better error
		finish();
	}
	protected void onStop() {
		super.onStop();
		Log.v(TAG,"onStop");
	}
	public void visDataReset() {
		Log.v(TAG,"vis count "+session.rpc.state.visData.size());
		//if (!vis_data_loaded) gotVisDataInit();
	}
	public void gotCityData() {
		//mTest.gotCityData();
	}
	public void tick() {
		// called from the network thread, needs to re-dir to main one
		Runnable resync = new Runnable() {
			public void run() {
				LouSessionMain.this.mainTick();
			}
		};
		this.runOnUiThread(resync);
	}
	/** tick ran in main thread by poller
	**/
	void mainTick() {
		updateTickers();
	}
	public void cityView(View v) {
		Log.v(TAG,"opening city view");
		Intent i = new Intent(this,CityView.class);
		i.putExtras(acct.toBundle());
		startActivity(i);
	}
	@Override
	public void onPlayerData() {
		updateTickers();
	}
	public void updateTickers() {
		TextView gold = (TextView) findViewById(R.id.gold);
		gold.setText(""+session.state.gold.getCurrent());
		TextView mana = (TextView) findViewById(R.id.mana);
		mana.setText(""+session.state.mana.getCurrent());
		TextView incoming = (TextView) findViewById(R.id.incoming_attacks);
		incoming.setText("" + session.state.incoming_attacks.size());
		resource_bar.update(session.state);
	}
	public void showIncoming(View v) {
		Intent i = new Intent(this,IncomingAttacks.class);
		i.putExtras(acct.toBundle());
		startActivity(i);
	}
	public void openChat(View v) {
		Intent i = new Intent(this,ChatWindow.class);
		i.putExtras(acct.toBundle());
		startActivity(i);
	}
	public void doLogout(View v) {
		session.logout();
		finish();
	}
	public void onChat(ArrayList<ChatMsg> c) {
		int total = session.state.chat_history.size();
		Button chat = (Button) findViewById(R.id.chat);
		chat.setText("chat ("+total+")");
	}
}
