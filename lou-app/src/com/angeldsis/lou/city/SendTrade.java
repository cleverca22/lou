package com.angeldsis.lou.city;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.angeldsis.lou.R;
import com.angeldsis.lou.SessionUser;
import com.angeldsis.lou.Utils;
import com.angeldsis.lou.city.SelectCity.CitySelected;
import com.angeldsis.louapi.LouState;
import com.angeldsis.louapi.LouState.City;
import com.angeldsis.louapi.RPC.GotOrderTargetInfo;
import com.angeldsis.louapi.RPC.TradeDirectDone;
import com.angeldsis.louapi.data.Coord;
import com.angeldsis.louapi.data.OrderTargetInfo;

public class SendTrade extends SessionUser implements CitySelected, GotOrderTargetInfo, TradeDirectDone {
	private static final String TAG = "SendTrade";
	CheckBox byLand;
	TextView player,city,time;
	int[] targets = {0,0,0,0};
	private int targetCity = -1;
	boolean byland = true;
	String targetPlayer;
	boolean loaded = false;
	private boolean palaceDonation;
	CheckBox palace;
	Button send_res;
	SelectCity changeCity;
	public void onCreate(Bundle b) {
		super.onCreate(b);
		if (Build.VERSION.SDK_INT > 13) initApi14();
		setContentView(R.layout.send_trade);
		byLand = (CheckBox) findViewById(R.id.byLand);
		byLand.setChecked(true);
		palace = (CheckBox) findViewById(R.id.palace);
		player = (TextView) findViewById(R.id.player);
		city = (TextView) findViewById(R.id.city);
		time = (TextView) findViewById(R.id.time);
		send_res = (Button) findViewById(R.id.send_res);
		changeCity = (SelectCity) findViewById(R.id.changeCity);
		changeCity.setMode(SelectCity.ChangeCurrentCity);
		
		Intent i = getIntent();
		Bundle args = i.getExtras();
		if (args.containsKey("targetCity")) {
			targetCity = args.getInt("targetCity");
			palaceDonation = true;
			palace.setChecked(true);
		}
	}
	@Override public void session_ready() {
		if (!loaded) {
			initBar(0,R.id.setWood,R.id.showWood);
			initBar(1,R.id.setStone,R.id.showStone);
			initBar(2,R.id.setIron,R.id.showIron);
			initBar(3,R.id.setFood,R.id.showFood);
			SelectCity s = (SelectCity) findViewById(R.id.selectCity);
			s.setHook(this);
			s.setMode(SelectCity.ModeNormal);
			s.session_ready(session.rpc.state,this);
			if (targetCity != -1) s.setPalace(targetCity);
			loaded = true;
			updateMaxRes();
			changeCity.session_ready(session.state, this);
		}
		updateCarts();
	}
	private void initBar(final int pos, int id, int id2) {
		final SeekBar b = (SeekBar) findViewById(id);
		final EditText e = (EditText) findViewById(id2);
		b.setMax(session.rpc.state.currentCity.getResourceCount(session.rpc.state, pos));
		b.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					int val = progress;
					e.setText(""+val);
					targets[pos] = val;
					SendTrade.this.tweakMax(pos);
				}
			}
			@Override public void onStartTrackingTouch(SeekBar seekBar) {
			}
			@Override public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
		e.setText("0");
		e.addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				int val = Integer.parseInt(s.toString());
				b.setProgress(val);
				targets[pos] = val;
			}
		});
		updateBar(pos,id,id2);
	}
	private void tweakMax(int pos) {
		if (pos != 0) updateBar(0,R.id.setWood,R.id.showWood);
		if (pos != 1) updateBar(1,R.id.setStone,R.id.showStone);
		if (pos != 2) updateBar(2,R.id.setIron,R.id.showIron);
		if (pos != 3) updateBar(3,R.id.setFood,R.id.showFood);
	}
	private void updateBar(int pos,int id1,int id2) {
		final SeekBar b = (SeekBar) findViewById(id1);
		final EditText e = (EditText) findViewById(id2);
		int resmax = session.rpc.state.currentCity.getResourceCount(session.rpc.state, pos);
		int capacityUsed = targets[0] + targets[1] + targets[2] + targets[3];
		int val,maxcapacity;
		if (byland) {
			maxcapacity = (session.rpc.state.currentCity.freecarts * 1000) - capacityUsed;
		} else {
			maxcapacity = (session.rpc.state.currentCity.freeships * 10000) - capacityUsed;
		}
		if (resmax > maxcapacity) val = maxcapacity;
		else val = resmax;
		b.setMax(val);
		e.setText(""+b.getProgress());
	}
	@Override public void selected(int x, int y) {
		Log.v(TAG,String.format("selected %d:%d",x,y));
		targetCity = Coord.toCityId(x, y);
		((EditText)findViewById(R.id.x)).setText(""+x);
		((EditText)findViewById(R.id.y)).setText(""+y);
		//session.rpc.GetPublicCityInfo(targetCity, this);
		session.rpc.GetOrderTargetInfo(session.rpc.state.currentCity,x,y,this);
	}
	@Override public void done(OrderTargetInfo p) {
		if (p.alliance != null) {
			player.setText(p.player.getName()+" ("+p.alliance.name+")");
		} else {
			player.setText(p.player.getName());
		}
		targetPlayer = p.player.getName();
		city.setText(p.cityname);
		
		int speedone;
		if (byland) speedone = session.rpc.state.tradeSpeedland;
		else speedone = session.rpc.state.tradeSpeedShip;
		float researchbonus = 0,shrinebonus = 0;
		float speedtwo = speedone / ( 1 + researchbonus + shrinebonus);
		double distance;
		if (byland) distance = p.targetDistance;
		else distance = p.targetDistanceWater;
		double time = distance * speedtwo;
		Log.v(TAG,"time is "+time+String.format(" speed1 %d", speedone));
		Log.v(TAG,"distance is "+distance);
		this.time.setText(String.format("%dh",(int)time/3600));
		
		if (distance == -1) {
			send_res.setEnabled(false);
		} else {
			send_res.setEnabled(true);
		}
	}
	public void sendTrade(View v) {
		Log.v(TAG,"sending resources...");
		int[] resources = {0,0,0,0};
		resources[0] = ((SeekBar)findViewById(R.id.setWood)).getProgress();
		resources[1] = ((SeekBar)findViewById(R.id.setStone)).getProgress();
		resources[2] = ((SeekBar)findViewById(R.id.setIron)).getProgress();
		resources[3] = ((SeekBar)findViewById(R.id.setFood)).getProgress();
		session.rpc.TradeDirect(session.rpc.state.currentCity,resources,byland,targetPlayer,Coord.fromCityId(targetCity),palaceDonation,this);
	}
	@Override
	public void done(int reply) {
		// FIXME
		// reply should be 0 for success
		Log.v(TAG,"reply is "+reply);
		TextView out = (TextView) findViewById(R.id.result);
		if (reply == 0) out.setText("worked");
		else out.setText("error "+reply);
		session.rpc.pollSoon();
	}
	private void updateCarts() {
		City city = session.rpc.state.currentCity;
		((TextView)findViewById(R.id.cartCounts)).setText(String.format("%d/%d",city.freecarts,city.maxcarts));
		((TextView)findViewById(R.id.shipCounts)).setText(String.format("%d/%d",city.freeships,city.maxships));
		((TextView)findViewById(R.id.cartCapacity)).setText(String.format("%d",city.freecarts * 1000));
		((TextView)findViewById(R.id.shipCapacity)).setText(String.format("%d",city.freeships * 10000));
	}
	public void onCityChanged() {
		updateMaxRes();
		updateBar(0,R.id.setWood,R.id.showWood);
		updateBar(1,R.id.setStone,R.id.showStone);
		updateBar(2,R.id.setIron,R.id.showIron);
		updateBar(3,R.id.setFood,R.id.showFood);
		// FIXME, re-run selected() for new distance, and parts of initBar to update max
		TextView out = (TextView) findViewById(R.id.result);
		out.setText("city changed");
	}
	public void gotCityData() {
		// FIXME, run parts of initBar to update max
		updateCarts();
		updateMaxRes();
		updateBar(0,R.id.setWood,R.id.showWood);
		updateBar(1,R.id.setStone,R.id.showStone);
		updateBar(2,R.id.setIron,R.id.showIron);
		updateBar(3,R.id.setFood,R.id.showFood);
	}
	private void updateMaxRes() {
		LouState state = session.rpc.state;
		City c = state.currentCity;
		setField(R.id.maxWood,c.getResourceCount(state, 0));
		setField(R.id.maxStone,c.getResourceCount(state, 1));
		setField(R.id.maxIron,c.getResourceCount(state, 2));
		setField(R.id.maxFood,c.getResourceCount(state, 3));
	}
	private void setField(int id, int value) {
		((TextView)findViewById(id)).setText("/"+Utils.NumberFormat(value));
	}
	public void landChanged(View v) {
		byland = byLand.isChecked();
		updateBar(0,R.id.setWood,R.id.showWood);
		updateBar(1,R.id.setStone,R.id.showStone);
		updateBar(2,R.id.setIron,R.id.showIron);
		updateBar(3,R.id.setFood,R.id.showFood);
		Coord c = Coord.fromCityId(targetCity);
		this.selected(c.x,c.y); // FIXME, reuse data from GetOrderTargetInfo
	}
	public void palaceChanged(View v) {
		palaceDonation = palace.isChecked();
	}
}
