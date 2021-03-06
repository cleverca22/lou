package com.angeldsis.louapi;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.TreeMap;

import org.json2.JSONArray;
import org.json2.JSONException;
import org.json2.JSONObject;

import com.angeldsis.louapi.data.AllianceMember;
import com.angeldsis.louapi.data.AllianceMembers;
import com.angeldsis.louapi.data.BuildQueue;
import com.angeldsis.louapi.data.Coord;
import com.angeldsis.louapi.data.SubRequest;
import com.angeldsis.louapi.data.UnitCount;
import com.angeldsis.louapi.data.World;
import com.google.gson.annotations.SerializedName;

public class LouState {
	String TAG = "LouState";
	transient int AllianceId;
	transient String AllianceName;
	transient public Player self;
	@SerializedName("cities") public TreeMap<Integer,City> cities;
	transient public City currentCity;
	transient public Counter gold;
	transient public ManaCounter mana;
	transient public ArrayList<IncomingAttack> incoming_attacks;
	private int diff, stepTime;
	long refTime;
	transient RPC rpc;
	transient public TimeZone tz;
	transient boolean fetchVis = false;
	transient public int unviewed_reports;
	transient public int viewed_reports;
	transient public ArrayList<SubRequest> subs;
	transient public boolean userActivity;
	transient boolean getFullPlayerData = true,checkOnline = false;
	transient public int tradeSpeedShip, tradeSpeedland;
	transient AllianceMembers alliancemembers;
	public int[] voidResources = new int[4];
	// FIXME, maybe move this elsewhere?
	private static final int[] types = { UnitCount.ZERK, UnitCount.PALADIN };
	public ArrayList<Coord> recentBosses;
	private JSONArray config; // FIXME, use a map?
	public CityGroup[] groups;
	public States accountState;
	public int title;
	
	public enum States {
		NEW, VALID
	}
	
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
		recentBosses = new ArrayList<Coord>();
		gold = new Counter(this);
		mana = new ManaCounter(this);
		subs = new ArrayList<SubRequest>();
		if (voidResources == null) voidResources = new int[4];
	}
	public City findCityById(int cityid) {
		return cities.get(cityid);
	}
	public void processPlayerInfo(JSONObject obj) throws JSONException {
		// FIXME, shouldn't rebuild the entire array on each pass
		//Log.v(TAG,obj.toString(1));
		JSONArray cities = obj.getJSONArray("Cities");
		int x;
		TreeMap<Integer,City> old = this.cities;
		this.cities = new TreeMap<Integer,City>();
		for (x = 0; x < cities.length(); x++) {
			JSONObject cityin = cities.getJSONObject(x);
			City cityout = null;
			int cityid = cityin.getInt("i");
			
			// find and re-use the old city object if it already exists
			if (old != null) {
				cityout = old.get(cityid);
				if (cityout != null) old.remove(cityout);
			}
			if (cityout == null) {
				cityout = new City();
			}
			cityout.name = cityin.getString("n");
			cityout.location = Coord.fromCityId(cityid);
			this.cities.put(cityid,cityout);
		}
		if (this.cities.size() > 0) {
			currentCity = this.cities.values().iterator().next(); // FIXME
			accountState = States.VALID;
		} else {
			currentCity = null;
			accountState = States.NEW;
		}
		AllianceId = obj.getInt("AllianceId");
		if (AllianceId > 0) AllianceName = obj.getString("AllianceName");
		self = Player.get(obj.optInt("Id"),obj.getString("Name"));
		Object sl = obj.opt("sl");
		Object s = obj.opt("s"); // this account is under the control of a sub?
		// FIXME should i lock it out like the real client?
		// refer to webfrontend.gui.EndSubstitutionWidget.js for more info
		Log.v(TAG,"sl:"+sl+" s:"+s);
		
		// an array of n=string v=string objects
		config = obj.getJSONArray("c");
	}
	public class City implements Comparable<Integer> {
		private static final String TAG = "City";
		@SerializedName("res") public Resource[] resources;
		@SerializedName("name") public String name;
		//@SerializedName("id") public int cityid;
		transient public ArrayList<LouVisData> visData;
		transient public int visreset;
		transient public BuildQueue[] queue;
		transient public int build_queue_start;
		transient public int build_queue_end;
		transient public int freeships,freecarts,maxships,maxcarts;
		transient public Coord location;
		transient public boolean autoBuildDefense, autoBuildEconomy;
		transient public int autoBuildTypeFlags;
		@SerializedName("units") public UnitCount[] units;
		@SerializedName("fc") public double foodConsumption;
		@SerializedName("fcs") public double foodConsumptionSupporter;
		@SerializedName("fcq") public double foodConsumptionQueue;
		public ArrayList<Trade> trade_in;
		public ArrayList<Trade> trade_out;
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
		public void fix(LouState state) {
			int i = 0;
			for (i = 0; i < 4; i++) resources[i].fix(i);
			init();
			if (units != null) {
				if (units.length != 20) units = new UnitCount[20];
			}
		}
		public boolean hasVisData() {
			synchronized(this) {
				if (visData == null) return false;
				if (visData.size() > 0) return true;
				return false;
			}
		}
		@Override public int compareTo(Integer o) {
			if (location == null) return -1;
			if (location.toCityId() < o) return -1;
			if (location.toCityId() > o) return 1;
			return 0;
		}
		public int foodEmptyTime(LouState state) {
			double rate = getResourceRate(state,3)/3600;
			if (rate >= 0) return 0;
			rate = rate * -1;
			int now = state.getServerStep();
			int current = getResourceCount(state,3);
			int timeLeft = (int) (current / rate);
			int incoming;
			if (trade_in != null) { // move this check once the other FIXME's are fixed, they will conflict
				//boolean repeat = true; FIXME loop over to find more items that can cover it after the first
				//while (repeat) {
					int x;
					incoming = 0;
					Iterator<Trade> i = trade_in.iterator();
					while (i.hasNext()) {
						Trade t = i.next();
						if (t.state != Trade.Working) continue;
						if ((t.end > now) && (t.end < (timeLeft + now)) && (t.content != null)) {
							Log.v(TAG,name+" trade will get back in time, "+t.toString());
							//Log.v(TAG,"contents "+t.content.toString());
							for (x=0; x<t.content.size(); x++) { // FIXME, remove the loop
								TradeResource o = t.content.get(x);
								if (o.type == 3) { // food
									incoming += o.count;
								}
							}
						} else if (t.end < now) {
							Log.v(TAG,"trade already done "+name+" "+t.toString());
							Log.v(TAG,""+now);
							for (x=0; x<t.content.size(); x++) {
								TradeResource o = t.content.get(x);
								if (o.type == 3) {
									resources[3].base += o.count;
								}
							}
							i.remove();
						}
					}
					// FIXME, add incoming raids, plunders, and assaults
					timeLeft = (int) ((current+incoming) / rate);
				//}
			}
			return timeLeft;
		}
		public int getResourceCount(LouState state,int id) {
			int stepsPassed = (int) (state.getServerStep() - resources[id].step);
			double delta = resources[id].delta;
			if (id == 3) {
				delta -= foodConsumption + foodConsumptionSupporter;
			}
			double newVal = stepsPassed * delta + resources[id].base;
			if (newVal > resources[id].max) return resources[id].max;
			return (int) newVal;
		}
		/** returns the food rate per hour, negative is loss, positive is gain **/
		public double getResourceRate(LouState state,int id) {
			double delta = resources[id].delta;
			if (id == 3) {
				delta -= foodConsumption + foodConsumptionSupporter;
			}
			return delta * 3600;
		}
		public int getTotalArmy() {
			if (units == null) return -1;
			int x,total=0;
			
			for (x=0; x<types.length; x++) {
				if (units[types[x]] != null) total += units[types[x]].c;
			}
			return total;
		}
	}
	public void parsePlayerUpdate(JSONObject d) throws JSONException {
		int x;
		getFullPlayerData = false;
		//boolean female = d.getBoolean("f");
		
		// FIXME check d.c array for changes to cities array
		//JSONArray c = d.optJSONArray("c");
		//Log.v(TAG,"c:"+c);
		
		JSONArray cg = d.optJSONArray("cg");
		if (cg != null) {
			CityGroup groups[] = new CityGroup[cg.length() + 1];
			groups[0] = new CityGroup(CityGroup.Type.ALL,this.cities);
			int j;
			for (j=0; j<cg.length(); j++) {
				JSONObject item = cg.getJSONObject(j);
				Log.v(TAG,"cg:"+item);
				String name = item.getString("n");
				JSONArray cities = item.getJSONArray("c");
				groups[j+1] = new CityGroup(name,cities,this);
			}
			this.groups = groups;
		}

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
		JSONArray vr = d.optJSONArray("vr");
		if (vr != null) {
			for (x = 0; x < vr.length(); x++) {
				JSONArray r = vr.getJSONArray(x);
				int type = r.getInt(0);
				int count = r.getInt(1);
				int change = count - voidResources[type-5];
				if (change != 0) Log.v(TAG,"void resource "+type+" changed by "+change);
				voidResources[type-5] = count;
			}
		}
		if (d.has("t")) {
			title = d.getInt("t");
		}
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
		rpc.onCityChanged(); // FIXME, maybe fire this after the new data is in
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
	public void processCityPacket(JSONObject p, World world) throws JSONException {
		JSONArray q = p.optJSONArray("q");
		int x;
		if (q != null) {
			BuildQueue[] queue = new BuildQueue[q.length()];
			for (x=0; x < q.length(); x++) queue[x] = new BuildQueue(q.getJSONObject(x));
			currentCity.queue = queue;
		} else if ((q == null) && (currentCity.queue.length != 0)) currentCity.queue = new BuildQueue[0];
		Log.v(TAG,"city debug "+currentCity+" "+p.toString());
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
					Log.v(TAG,"X=="+X.toString(1));
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
		JSONArray u = p.optJSONArray("u");
		Log.v(TAG,"unit data:"+u);
		if (u != null) {
			currentCity.units = new UnitCount[80];
			for (x=0; x < u.length(); x++) {
				JSONObject t2 = u.getJSONObject(x);
				UnitCount t = new UnitCount(); // FIXME, reuse objects to help with gc presure
				t.tc = t2.getInt("tc");
				t.c = t2.getInt("c");
				t.t = t2.getInt("t");
				currentCity.units[t.t] = t;
			}
		} else currentCity.units = null;
		JSONArray ti = p.optJSONArray("ti");
		JSONArray to = p.optJSONArray("to");
		City c = currentCity;
		if (ti != null) {
			//Log.v(TAG,"ti:"+ti.length());
			c.trade_in = parseTrades(ti, world,Trade.IN);
		} else c.trade_in = null;
		if (to != null) {
			//Log.v(TAG,"to:"+to.length());
			c.trade_out = parseTrades(to, world,Trade.OUT);
		} c.trade_out = null;
		JSONArray traders = p.optJSONArray("t");
		//Log.v(TAG,"traders:"+traders);
		if ((traders != null) && (traders.length()>0)) {
			JSONObject land = traders.getJSONObject(0);
			JSONObject ship = traders.getJSONObject(1);
			currentCity.freecarts = land.getInt("c");
			currentCity.maxcarts  = land.getInt("tc");
			currentCity.freeships = ship.getInt("c");
			currentCity.maxships = ship.getInt("tc");
		}
		c.autoBuildDefense = p.getBoolean("ad");
		c.autoBuildEconomy = p.getBoolean("ae");
		c.autoBuildTypeFlags = p.getInt("at");
		Log.v(TAG,String.format("name=%s def=%b eco=%b flags=%d", c.name,c.autoBuildDefense,c.autoBuildEconomy,c.autoBuildTypeFlags));
		
		c.foodConsumption = p.getDouble("fc");
		c.foodConsumptionSupporter = p.getDouble("fcs");
		c.foodConsumptionQueue = p.getDouble("fcq");
		Log.v(TAG,String.format("%f %f %f",c.foodConsumption,c.foodConsumptionSupporter,c.foodConsumptionQueue));
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
	private ArrayList<Trade> parseTrades(JSONArray list, World world, int direction) throws JSONException {
		// FIXME, reuse Trade objects, update them instead
		ArrayList<Trade> out = new ArrayList<Trade>();
		int j;
		for (j = 0; j < list.length(); j++) {
			JSONObject t = list.optJSONObject(j);
			Trade trade = new Trade(t,world,direction);
			out.add(trade);
		}
		return out;
	}
	public class TradeResource {
		int count;
		int type;
	}
	public class Trade {
		Alliance alliance;
		Player player;
		public int type;
		static final int None = 0;
		static final int AuctionHouse = 1;
		public static final int Direct = 2;
		public static final int TradeMinisterRequested = 3;
		static final int TradeMinisterSurplus = 4;
		static final int IN = 0;
		static final int OUT = 1;
		int transport;
		static final int Land = 1;
		static final int Ship = 2;
		public int state; // some states omitted
		public static final int Working = 1;
		public static final int Return = 2;
		public static final int ReturnFromCancel = 6;
		public static final int WorkingPalaceSupport = 7;
		@SerializedName("d") public int direction;
		public int id,start,end;
		transient public String DEBUG = "deserialized";
		public String cityName;
		private ArrayList<TradeResource> content;
		public Trade(JSONObject t, World world, int direction) throws JSONException {
			player = Player.get(t.optInt("p"),t.optString("pn"));
			alliance = world.getAlliance(t.optInt("a"),t.optString("an"));
			transport = t.optInt("tt");
			type = t.optInt("t");
			state = t.optInt("s");
			this.direction = direction;
			id = t.optInt("i");
			DEBUG = t.toString();

			int city = t.optInt("c");
			cityName = t.optString("cn");

			Log.v(TAG,t.toString());
			JSONArray r = t.optJSONArray("r");
			int i;
			content = new ArrayList<TradeResource>();
			for (i=0; i<r.length(); i++) {
				JSONObject o = r.getJSONObject(i);
				TradeResource tr = new TradeResource();
				tr.count = o.getInt("c");
				tr.type = o.getInt("t") - 1;
				content.add(tr);
			}
			start = t.optInt("ss");
			end = t.optInt("es");
			Log.v(TAG,"ss:"+start+" es:"+end);
		}
		public String toString() {
			return DEBUG;
		}
	}
	public void parseAllianceUpdate(JSONObject d) throws JSONException {
		int ia = d.optInt("ia");
		int oa = d.optInt("oa");
		if (oa > 0) Log.v(TAG,String.format("outgoing:%d",oa));
		rpc.aam.countsUpdated(ia,oa);
		JSONArray members = d.optJSONArray("m");
		if (members != null) {
			if (alliancemembers == null) alliancemembers = new AllianceMembers();
			int i;
			int[] valid = new int[members.length()];
			for (i=0; i<members.length(); i++) {
				JSONObject m = members.getJSONObject(i);
				int id = m.getInt("i");
				String name = m.getString("n");
				AllianceMember m2 = alliancemembers.get(id);
				if (m2 == null) {
					m2 = new AllianceMember(id,name);
					alliancemembers.put(id, m2);
				}
				valid[i] = id;
				m2.update(m);
				
				//if (m2.base.getName().equals("xHavoc")) Log.v(TAG,"tag1 "+m.toString());
			}
			Iterator<AllianceMember> it = alliancemembers.values().iterator();
			while (it.hasNext()) {
				AllianceMember m = it.next();
				boolean keep = false;
				for (i=0; i<valid.length; i++) {
					if (valid[i] == m.base.getId()) { // FIXME
						keep = true;
						break;
					}
				}
				if (keep == false) {
					it.remove();
				}
			}
		}
	}
	public void parseSubs(JSONObject d) {
		synchronized (subs) {
			try {
				Log.v(TAG,"tag2 "+d.toString(1));
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
					s.role = SubRequest.Role.giver;
				} else {
					s.giver = Player.get(p0, n);
					s.receiver = self;
					s.role = SubRequest.Role.receiver;
				}
				s.id = s2.optInt("i");
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
	public String stepToString(int target) {
		Date d = new Date(stepToMilis(target));
		Calendar c = Calendar.getInstance(tz);
		c.setTime(d);
		int nowstep = getServerStep();
		int stepdiff = target - nowstep;
		// if its less then 24h away, just show the time
		// needs more work
		if (stepdiff < (60 * 60 * 24)) {
			return String.format("%02d:%02d:%02d",
					c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),
					c.get(Calendar.SECOND));
		}
		return String.format("%02d.%02d %02d:%02d:%02d",c.get(Calendar.MONTH)+1,
				c.get(Calendar.DAY_OF_MONTH),
				c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),
				c.get(Calendar.SECOND));
	}
	public void parseServerInfo(JSONObject reply) throws JSONException {
		tradeSpeedShip = reply.getInt("tss");
		tradeSpeedland = reply.getInt("tsl");
		String td = reply.getString("td");
		Log.v(TAG,"td: "+td);
	}
	public float getInfantrySpeed() {
		// FIXME, use the proper values based on virtues and research
		return (18 * 60) + 46; // 18mins 46sec per field, my current tech from w96
	}
	public String getConfig(String name) {
		int i;
		try {
			for (i=0; i<config.length(); i++) {
				JSONObject entry = config.getJSONObject(i);
				String key = entry.getString("n");
				if (key.equals(name)) return entry.getString("v");
			}
		} catch (JSONException e) {
			return null;
		}
		return null;
	}
}
