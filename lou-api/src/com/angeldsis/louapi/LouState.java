package com.angeldsis.louapi;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import org.json2.JSONArray;
import org.json2.JSONException;
import org.json2.JSONObject;

import com.angeldsis.louapi.data.BuildQueue;
import com.angeldsis.louapi.data.SubRequest;

public class LouState implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final String TAG = "LouState";
	int AllianceId;
	String AllianceName;
	public Player self;
	public ArrayList<City> cities;
	public City currentCity;
	public Counter gold;
	public ManaCounter mana;
	public ArrayList<IncomingAttack> incoming_attacks;
	private int diff, stepTime;
	long refTime;
	public ArrayList<ChatMsg> chat_history;
	RPC rpc;
	public TimeZone tz;
	boolean fetchVis = false;
	public int unviewed_reports;
	public int viewed_reports;
	public ArrayList<SubRequest> subs;
	public boolean userActivity;

	public LouState() {
		init();
	}
	IncomingAttack findById(int id,ArrayList<IncomingAttack> list) {
		for (IncomingAttack a : list) {
			if (a.id == id) return a;
		}
		return null;
	}
	private void init() {
		incoming_attacks = new ArrayList<IncomingAttack>();
		chat_history = new ArrayList<ChatMsg>();
		gold = new Counter(this);
		mana = new ManaCounter(this);
		subs = new ArrayList<SubRequest>();
	}
	public void processPlayerInfo(JSONObject obj) throws JSONException {
		Log.v(TAG,obj.toString(1));
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
			}
			cityout.name = cityin.getString("n");
			cityout.cityid = cityid;
			this.cities.add(cityout);
		}
		currentCity = this.cities.get(0);
		AllianceId = obj.getInt("AllianceId");
		if (AllianceId > 0) AllianceName = obj.getString("AllianceName");
		self = Player.get(obj.optInt("Id"),obj.getString("Name"));
	}
	public class City implements Serializable {
		private static final String TAG = "City";
		private static final long serialVersionUID = 1L;
		public Resource[] resources;
		public String name;
		long cityid;
		public ArrayList<LouVisData> visData;
		public int visreset;
		public BuildQueue[] queue;
		public int build_queue_start;
		public int build_queue_end;
		City() {
			resources = new Resource[4];
			int i;
			for (i = 0; i < 4; i++) resources[i] = new Resource(LouState.this,i);
			init();
		}
		void init() {
			visData = new ArrayList<LouVisData>();
			visreset = 1;
			queue = new BuildQueue[0];
		}
		public String toString() {
			return this.hashCode()+" "+name;
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
		public long getCityid() {
			return cityid;
		}
		public boolean hasVisData() {
			synchronized(this) {
				if (visData == null) return false;
				if (visData.size() > 0) return true;
				return false;
			}
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
					//Log.v(TAG,X.toString(1));
					int id = X.getInt("i");
					IncomingAttack a = null;
					Iterator<IncomingAttack> i = this.incoming_attacks.iterator();
					while (i.hasNext()) {
						IncomingAttack a2 = i.next();
						if (a2.id == id) {
							a = a2;
							break;
						}
					}
					if (a == null) {
						a = new IncomingAttack(this,id);
						a.updatePlayerType(X);
						incoming_attacks.add(a);
						rpc.runOnUiThread(new NewAttackEvent(a));
					} else {
						a.updatePlayerType(X);
					}
				}
			} else Log.v(TAG,"no attacks 2!");
		}
		//else Log.v(TAG,"no attacks?");
	}
	public void setTime(long refTime2, int stepTime, int diff, int serverOffset) {
		refTime = refTime2;
		this.stepTime = stepTime;
		this.diff = diff;
		Log.v(TAG,"ref:"+refTime2+" stepTime:"+stepTime+" diff:"+diff+" serverOffset"+serverOffset);
		// FIXME, do this 'properly'
		int hours_diff = serverOffset / 1000 / 3600;
		tz = TimeZone.getTimeZone("GMT"+hours_diff);
	}
	// not sure entirely what these are for yet, so i'm reproducing them exactly
	public int getServerStep() {
		if (stepTime == 0) return 0;
		long d = System.currentTimeMillis() - refTime - diff;
		//Log.v(TAG,"refTime: "+refTime+" diff:"+diff+" stepTime:"+stepTime+" d:"+d);
		return (int) (d / stepTime);
	}
	public long stepToMilis(long step) {
		return (step * stepTime)+ refTime + diff;
	}
	public void changeCity(City city) {
		currentCity = city;
		rpc.interrupt();
		rpc.cityChanged(); // FIXME, maybe fire this after the new data is in
		city.visData.clear(); // the code wasn't receiving updates, re-fetch it
		city.visreset = 1;
		rpc.pollSoon();
	}
	public void enableVis() {
		currentCity.visData.clear();
		currentCity.visreset = 1;
		fetchVis = true;
		rpc.pollSoon();
		rpc.interrupt();
	}
	public void disableVis() {
		fetchVis = false;
	}
	public void setRPC(RPC rpc2) {
		rpc = rpc2;
	}
	public void processCityPacket(JSONObject p) throws JSONException {
		JSONArray q = p.optJSONArray("q");
		int x;
		if (q != null) {
			BuildQueue[] queue = new BuildQueue[q.length()];
			for (x=0; x < q.length(); x++) queue[x] = new BuildQueue(q.getJSONObject(x));
			currentCity.queue = queue;
		} else if ((q == null) && (currentCity.queue.length != 0)) currentCity.queue = new BuildQueue[0];
		Log.v(TAG,""+currentCity);
		currentCity.build_queue_start = p.optInt("bqs");
		currentCity.build_queue_end = p.optInt("bqe");
		Log.v(TAG, String.format("bqs %d, bqe %d",currentCity.build_queue_start,currentCity.build_queue_end));
		
		if (p.has("iuo")) {
			Object iuo2 = p.get("iuo");
			if (iuo2 != JSONObject.NULL) {
				JSONArray iuo = (JSONArray) iuo2;
				//Log.v(TAG, iuo.toString(1));
				for (x = 0; x < iuo.length(); x++) {
					// incoming attacks on current city
					JSONObject X = iuo.getJSONObject(x);
					int id = X.getInt("i");
					Log.v(TAG,X.toString(1));
					IncomingAttack a = null;
					Iterator<IncomingAttack> i = this.incoming_attacks.iterator();
					while (i.hasNext()) {
						IncomingAttack a2 = i.next();
						if (a2.id == id) {
							a = a2;
							break;
						}
					}
					if (a == null) {
						// new attack
						a = new IncomingAttack(this,id);
						a.updateCityType(X);
						this.incoming_attacks.add(a);
						rpc.runOnUiThread(new NewAttackEvent(a));
					} else {
						a.updateCityType(X);
					}
				}
			}
			else Log.v(TAG,"no attacks 2!");
		}
		else Log.v(TAG,"no attacks?");
		JSONObject u = p.optJSONObject("u");
		Log.v(TAG,"unit data:"+u);
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
	private class NewAttackEvent implements Runnable {
		IncomingAttack a;
		NewAttackEvent(IncomingAttack a ) {
			this.a = a;
		}
		public void run() {
			Log.v(TAG,"new attack being reported");
			rpc.onNewAttack(a);
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
	public void parseAllianceUpdate(JSONObject d) {
		int ia = d.optInt("ia");
		int oa = d.optInt("oa");
		if (oa > 0) Log.v(TAG,String.format("outgoing:%d",oa));
		rpc.aam.countsUpdated(ia,oa);
	}
	public void parseSubs(JSONObject d) {
		synchronized (subs) {
			try {
				Log.v(TAG,d.toString(1));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			JSONArray r = d.optJSONArray("r");
			int i;
			subs.clear();
			for (i=0; i<r.length(); i++) {
				JSONObject s2 = r.optJSONObject(i);
				SubRequest s = new SubRequest();
				Date t = new Date(s2.optLong("t"));
				s.state = s2.optInt("s");
				int p0 = s2.optInt("p0"); // the giver (account to be controlled
				int p1 = s2.optInt("p1"); // the receiver
				String n = s2.optString("n"); // name of giver
				if (p0 == self.getId()) {
					s.giver = self;
					s.receiver = Player.get(p1, n);
				} else {
					s.giver = Player.get(p0, n);
					s.receiver = self;
				}
				s.role = SubRequest.Role.receiver;
				s.id = s2.optInt("id");
				// when accepting a sub, (acting as receiver) call SubstitutionAcceptReq(id,p0);
				// once you have a sub, call CreateSubstitutionSession(id,pid);
				subs.add(s);
			}
		}
		rpc.runOnUiThread(new Runnable() {
			public void run() {
				rpc.onSubListChanged();
			}
		});
	}
	public String stepToString(int end) {
		Date d = new Date(stepToMilis(end));
		Calendar c = Calendar.getInstance(tz);
		c.setTime(d);
		return String.format("%02d.%02d %02d:%02d:%02d",c.get(Calendar.MONTH)+1,
				c.get(Calendar.DAY_OF_MONTH),
				c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),
				c.get(Calendar.SECOND));
	}
}
