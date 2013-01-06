package com.angeldsis.louapi;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.angeldsis.louapi.LouState.City;

public class LouState implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final String TAG = "LouState";
	int AllianceId;
	String AllianceName,Name;
	public ArrayList<City> cities;
	public City currentCity;
	public Counter gold;
	public ManaCounter mana;
	public ArrayList<IncomingAttack> incoming_attacks;
	private int serverOffset, diff, stepTime;
	long refTime;
	public ArrayList<ChatMsg> chat_history;
	RPC rpc;
	boolean fetchVis = false;

	public LouState() {
		init();
	}
	private void init() {
		incoming_attacks = new ArrayList<IncomingAttack>();
		chat_history = new ArrayList<ChatMsg>();
		gold = new Counter(this);
		mana = new ManaCounter(this);
	}
	public void processPlayerInfo(JSONObject obj) throws JSONException {
		JSONArray cities = obj.getJSONArray("Cities");
		int x;
		ArrayList<City> old = this.cities;
		this.cities = new ArrayList<City>();
		for (x = 0; x < cities.length(); x++) {
			JSONObject cityin = cities.getJSONObject(x);
			City cityout = null;
			long cityid = cityin.getLong("i");
			
			// find and re-use the old city object if it already exists
			if (old != null) {
				Iterator<City> i = old.iterator();
				while (i.hasNext()) {
					City c = i.next();
					Log.v(TAG,c.name+""+c.cityid+"=="+cityid);
					if (c.cityid == cityid) {
						cityout = c;
						i.remove();
						break;
					}
				}
			}
			if (cityout == null) {
				cityout = new City();
				Log.v(TAG,"made new");
			}
			cityout.name = cityin.getString("n");
			cityout.cityid = cityid;
			this.cities.add(cityout);
		}
		currentCity = this.cities.get(0);
		AllianceId = obj.getInt("AllianceId");
		AllianceName = obj.getString("AllianceName");
		Name = obj.getString("Name");
	}
	public class City implements Serializable {
		private static final String TAG = "City";
		private static final long serialVersionUID = 1L;
		public Resource[] resources;
		public String name;
		long cityid;
		public ArrayList<LouVisData> visData;
		public int visreset;
		City() {
			resources = new Resource[4];
			int i;
			for (i = 0; i < 4; i++) resources[i] = new Resource(LouState.this,i);
			init();
		}
		void init() {
			visData = new ArrayList<LouVisData>();
			visreset = 1;
		}
		public String toString() {
			return name;
		}
		public void addVisObj(LouVisData parsed) {
			visData.add(parsed);
		}
		private void writeObject(java.io.ObjectOutputStream out) throws IOException {
			out.writeObject(resources);
			out.writeUTF(name);
			out.writeLong(cityid);
		}
		private void readObject(java.io.ObjectInputStream in) throws IOException,
		ClassNotFoundException {
			resources = (Resource[]) in.readObject();
			name = in.readUTF();
			cityid = in.readLong();
			init();
		}
		public void fix(LouState state) {
			int i = 0;
			for (i = 0; i < 4; i++) resources[i].fix(state,i);
		}
	}
	public void parsePlayerUpdate(JSONObject d) throws JSONException {
		// FIXME check d.c array for changes to cities array
		if (d.has("g")) {
			JSONObject g = d.optJSONObject("g");
			double base = g.optDouble("b");
			double delta = g.optDouble("d");
			int step = g.optInt("s");
			gold.update(base,delta,step);
		}
		if (d.has("m")) {
			JSONObject m = d.optJSONObject("m");
			double base = m.optDouble("b");
			double delta = m.optDouble("d");
			int step = m.optInt("s");
			int max = m.optInt("m");
			mana.update(base, delta, max, step);
		}
		if (d.has("iuo")) {
			Object iuo2 = d.get("iuo");
			if (iuo2 != JSONObject.NULL) {
				JSONArray iuo = (JSONArray) iuo2;
				//Log.v(TAG, iuo.toString(1));
				int x;
				for (x = 0; x < iuo.length(); x++) {
					// incoming attacks on current city
					JSONObject X = iuo.getJSONObject(x);
					IncomingAttack ia = new IncomingAttack(X);
					Log.v(TAG,"attack incoming to "+ia.targetCityName+" from player "+ia.playerName);
					incoming_attacks.add(ia);
				}
			}
			else Log.v(TAG,"no attacks 2!");
		}
		else Log.v(TAG,"no attacks?");
	}
	public void setTime(long refTime2, int stepTime, int diff, int serverOffset) {
		refTime = refTime2;
		this.stepTime = stepTime;
		this.diff = diff;
		this.serverOffset = serverOffset;
		Log.v(TAG,"ref:"+refTime2+" stepTime:"+stepTime+" diff:"+diff+" serverOffset"+serverOffset);
	}
	// not sure entirely what these are for yet, so i'm reproducing them exactly
	public int getServerStep() {
		if (stepTime == 0) return 0;
		long d = System.currentTimeMillis() - refTime - diff;
		//Log.v(TAG,"refTime: "+refTime+" diff:"+diff+" stepTime:"+stepTime+" d:"+d);
		return (int) (d / stepTime);
	}
	public void changeCity(City city) {
		currentCity = city;
		rpc.interrupt();
		rpc.cityChanged(); // FIXME, maybe fire this after the new data is in
		city.visData.clear(); // the code wasn't receiving updates, re-fetch it
		city.visreset = 1;
	}
	public void enableVis() {
		currentCity.visData.clear();
		currentCity.visreset = 1;
		fetchVis = true;
		rpc.interrupt();
	}
	public void disableVis() {
		fetchVis = false;
	}
	public void setRPC(RPC rpc2) {
		rpc = rpc2;
	}
	public void processCityPacket(JSONObject p) {
		JSONArray ti = p.optJSONArray("ti");
		JSONArray to = p.optJSONArray("to");
		if (ti != null) {
			//Log.v(TAG,"ti:"+ti.length());
			ArrayList<Trade> trade_in = parseTrades(ti);
		}
		if (to != null) {
			//Log.v(TAG,"to:"+to.length());
			ArrayList<Trade> trade_out = parseTrades(to);
		}
	}
	private ArrayList<Trade> parseTrades(JSONArray list) {
		ArrayList<Trade> out = new ArrayList<Trade>();
		int j;
		for (j = 0; j < list.length(); j++) {
			JSONObject t = list.optJSONObject(j);
			JSONArray contents = t.optJSONArray("r"); // contains c/t pairs
			String cityName = t.optString("cn");
			int city = t.optInt("c");
			int start = t.optInt("ss");
			int end = t.optInt("es");
			int id = t.optInt("i");
			//Log.v(TAG,"r:"+contents+" cn:"+cityName+" ss:"+start+" es:"+end);
			Trade trade = new Trade(t);
		}
		return out;
	}
	class Trade {
		Alliance alliance;
		Player player;
		int type;
		static final int None = 0;
		static final int AuctionHouse = 1;
		static final int Direct = 2;
		static final int TradeMinisterRequested = 3;
		static final int TradeMinisterSurplus = 4;
		int transport;
		static final int Land = 1;
		static final int Ship = 2;
		int state; // some states omitted
		static final int Working = 1;
		static final int Return = 2;
		static final int ReturnFromCancel = 6;
		static final int WorkingPalaceSupport = 7;
		public Trade(JSONObject t) {
			player = Player.get(t.optInt("p"),t.optString("pn"));
			alliance = Alliance.get(t.optInt("a"),t.optString("an"));
			transport = t.optInt("tt");
			type = t.optInt("t");
			state = t.optInt("s");
		}
	}
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		Log.v(TAG,"Save");
		out.writeObject(cities);
	}
	private void readObject(java.io.ObjectInputStream in) throws IOException,
	ClassNotFoundException {
		Log.v(TAG,"Restore");
		init();
		cities = (ArrayList<City>) in.readObject();
	}
}
