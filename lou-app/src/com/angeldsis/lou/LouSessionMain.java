package com.angeldsis.lou;

import java.util.ArrayList;

import com.angeldsis.LOU.ChatMsg;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LouSessionMain extends SessionUser implements SessionKeeper.Callbacks {
	static final String TAG = "LouSessionMain";

	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		Log.v(TAG,"onCreate");
		setContentView(R.layout.session_core);
		//vis_data_loaded = false;
		Intent intent = new Intent(this,SessionKeeper.class);
		startService(intent);
	}
	protected void onStart() {
		super.onStart();
		Log.v(TAG,"onStart");
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
	public void onChat(ArrayList<ChatMsg> c) {
		int total = session.state.chat_history.size();
		Button chat = (Button) findViewById(R.id.chat);
		chat.setText("chat ("+total+")");
	}
}
