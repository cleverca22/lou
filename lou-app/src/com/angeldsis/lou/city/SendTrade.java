package com.angeldsis.lou.city;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.angeldsis.lou.FragmentBase;
import com.angeldsis.lou.R;
import com.angeldsis.lou.Utils;
import com.angeldsis.lou.city.SelectCity.CitySelected;
import com.angeldsis.louapi.LouState;
import com.angeldsis.louapi.LouState.City;
import com.angeldsis.louapi.RPC.GotOrderTargetInfo;
import com.angeldsis.louapi.RPC.TradeDirectDone;
import com.angeldsis.louapi.data.Coord;
import com.angeldsis.louapi.data.OrderTargetInfo;

public class SendTrade extends FragmentBase implements CitySelected, GotOrderTargetInfo, TradeDirectDone, OnClickListener, OnCheckedChangeListener {
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
	SelectCity changeCity, selectCity;
	private static class Bar {
		SeekBar seeker;
		EditText editor;
		TextView max;
		public Bar(ViewGroup root, int i, int id1, int id2, int id3) {
			seeker = (SeekBar) root.findViewById(id1);
			editor = (EditText) root.findViewById(id2);
			max = (TextView) root.findViewById(id3);
		}
	}
	Bar bars[];
	EditText x,y;
	private TextView result,cartCount,shipCount,cartCap,shipCap;
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle b) {
		super.onCreate(b);
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.send_trade,parent,false);
		byLand = (CheckBox) root.findViewById(R.id.byLand);
		byLand.setChecked(true);
		palace = (CheckBox) root.findViewById(R.id.palace);
		player = (TextView) root.findViewById(R.id.player);
		city = (TextView) root.findViewById(R.id.city);
		time = (TextView) root.findViewById(R.id.time);
		send_res = (Button) root.findViewById(R.id.send_res);
		send_res.setOnClickListener(this);
		palace.setOnCheckedChangeListener(this);
		bars = new Bar[4];
		bars[0] = new Bar(root,0,R.id.setWood,R.id.showWood,R.id.maxWood);
		bars[1] = new Bar(root,1,R.id.setStone,R.id.showStone,R.id.maxStone);
		bars[2] = new Bar(root,2,R.id.setIron,R.id.showIron,R.id.maxIron);
		bars[3] = new Bar(root,3,R.id.setFood,R.id.showFood,R.id.maxFood);
		x = (EditText)root.findViewById(R.id.x);
		y = (EditText)root.findViewById(R.id.y);
		result = (TextView) root.findViewById(R.id.result);
		cartCount = (TextView)root.findViewById(R.id.cartCounts);
		shipCount = (TextView)root.findViewById(R.id.shipCounts);
		cartCap = (TextView)root.findViewById(R.id.cartCapacity);
		shipCap = (TextView)root.findViewById(R.id.shipCapacity);

		Bundle args = getArguments();
		if ((args != null) && args.containsKey("targetCity")) {
			targetCity = args.getInt("targetCity");
			palaceDonation = true;
			palace.setChecked(true);
		}
		
		if (b == null) {
			changeCity = new SelectCity();
			changeCity.setMode(SelectCity.ChangeCurrentCity);
			selectCity = new SelectCity();
			selectCity.setMode(SelectCity.ModeNormal);
			selectCity.setHook(this);
			if (targetCity != -1) selectCity.setPalace(targetCity);
			getChildFragmentManager().beginTransaction()
				.replace(R.id.changeCity, changeCity)
				.replace(R.id.selectCity, selectCity)
				.commit();
		}
		
		return root;
	}
	@Override public void session_ready() {
		if (!loaded) {
			initBar(0);
			initBar(1);
			initBar(2);
			initBar(3);
			loaded = true;
			updateMaxRes();
		}
		updateCarts();
	}
	private void initBar(final int pos) {
		final SeekBar b = bars[pos].seeker;
		final EditText e = bars[pos].editor;
		b.setMax(parent.session.state.currentCity.getResourceCount(parent.session.state, pos));
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
				int val;
				try {
					val = Integer.parseInt(s.toString());
				} catch (NumberFormatException e) {
					val = 0;
				}
				b.setProgress(val);
				targets[pos] = val;
			}
		});
		updateBar(pos);
	}
	private void tweakMax(int pos) {
		if (pos != 0) updateBar(0);
		if (pos != 1) updateBar(1);
		if (pos != 2) updateBar(2);
		if (pos != 3) updateBar(3);
	}
	private void updateBar(int pos) {
		final SeekBar b = bars[pos].seeker;
		final EditText e = bars[pos].editor;
		int resmax = parent.session.state.currentCity.getResourceCount(parent.session.state, pos);
		int capacityUsed = targets[0] + targets[1] + targets[2] + targets[3];
		int val,maxcapacity;
		if (byland) {
			maxcapacity = (parent.session.state.currentCity.freecarts * 1000) - capacityUsed;
		} else {
			maxcapacity = (parent.session.state.currentCity.freeships * 10000) - capacityUsed;
		}
		if (resmax > maxcapacity) val = maxcapacity;
		else val = resmax;
		b.setMax(val);
		e.setText(""+b.getProgress());
	}
	@Override public void selected(int x, int y) {
		Log.v(TAG,String.format("selected %d:%d",x,y));
		targetCity = Coord.toCityId(x, y);
		this.x.setText(""+x);
		this.y.setText(""+y);
		//session.rpc.GetPublicCityInfo(targetCity, this);
		parent.session.rpc.GetOrderTargetInfo(parent.session.state.currentCity,x,y,this);
	}
	@Override public void done(OrderTargetInfo p) {
		if (parent == null) return;
		if (p.alliance != null) {
			player.setText(p.player.getName()+" ("+p.alliance.name+")");
		} else {
			player.setText(p.player.getName());
		}
		targetPlayer = p.player.getName();
		city.setText(p.cityname);
		
		int speedone;
		if (byland) speedone = parent.session.state.tradeSpeedland;
		else speedone = parent.session.state.tradeSpeedShip;
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
		resources[0] = bars[0].seeker.getProgress();
		resources[1] = bars[1].seeker.getProgress();
		resources[2] = bars[2].seeker.getProgress();
		resources[3] = bars[3].seeker.getProgress();
		parent.session.rpc.TradeDirect(parent.session.state.currentCity,resources,byland,targetPlayer,Coord.fromCityId(targetCity),palaceDonation,this);
	}
	@Override public void done(int reply) {
		if (result == null) return;
		// FIXME
		// reply should be 0 for success
		Log.v(TAG,"reply is "+reply);
		if (reply == 0) result.setText("worked");
		else result.setText("error "+reply);
		parent.session.rpc.pollSoon();
	}
	private void updateCarts() {
		City city = parent.session.state.currentCity;
		cartCount.setText(String.format("%d/%d",city.freecarts,city.maxcarts));
		shipCount.setText(String.format("%d/%d",city.freeships,city.maxships));
		cartCap.setText(String.format("%d",city.freecarts * 1000));
		shipCap.setText(String.format("%d",city.freeships * 10000));
	}
	public void onCityChanged() {
		updateMaxRes();
		updateBar(0);
		updateBar(1);
		updateBar(2);
		updateBar(3);
		// FIXME, re-run selected() for new distance, and parts of initBar to update max
		result.setText("city changed");
	}
	public void gotCityData() {
		// FIXME, run parts of initBar to update max
		updateCarts();
		updateMaxRes();
		updateBar(0);
		updateBar(1);
		updateBar(2);
		updateBar(3);
	}
	private void updateMaxRes() {
		LouState state = parent.session.state;
		City c = state.currentCity;
		// FIXME
		setField(0,c.getResourceCount(state, 0));
		setField(1,c.getResourceCount(state, 1));
		setField(2,c.getResourceCount(state, 2));
		setField(3,c.getResourceCount(state, 3));
	}
	private void setField(int pos, int value) {
		bars[pos].max.setText("/"+Utils.NumberFormat(value));
	}
	public void landChanged(View v) {
		byland = byLand.isChecked();
		updateBar(0);
		updateBar(1);
		updateBar(2);
		updateBar(3);
		Coord c = Coord.fromCityId(targetCity);
		this.selected(c.x,c.y); // FIXME, reuse data from GetOrderTargetInfo
	}
	@Override
	public void onClick(View arg0) {
		sendTrade(arg0);
	}
	@Override
	public void onCheckedChanged(CompoundButton view, boolean isChecked) {
		if (view.getId() == R.id.palace) {
			palaceDonation = palace.isChecked();
		}
	}
}
