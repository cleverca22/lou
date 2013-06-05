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
	private static final String TAG = "LouState";
	transient int AllianceId;
	transient String AllianceName;
	transient public Player self;
	@SerializedName("cities") public TreeMap<Integer,City> cities;
	transient public City currentCity;
	transient public Counter gold;
	transient public ManaCounter mana;
	transient public ArrayList<IncomingAttack> incoming_attacks;
	transient private int diff, stepTime;
	transient long refTime;
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
		gold = new Counter(this);
		mana = new ManaCounter(this);
		subs = new ArrayList<SubRequest>();
	}
	public City findCityById(int cityid) {
		return cities.get(cityid);
	}
	public void processPlayerInfo(JSONObject obj) throws JSONException {
		// FIXME, shouldnt rebuild the entire array on each pass
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
			cityout.cityid = cityid;
			this.cities.put(cityid,cityout);
		}
		currentCity = this.cities.values().iterator().next(); // FIXME
		AllianceId = obj.getInt("AllianceId");
		if (AllianceId > 0) AllianceName = obj.getString("AllianceName");
		self = Player.get(obj.optInt("Id"),obj.getString("Name"));
	}
	public class City implements Comparable<Integer> {
		private static final String TAG = "City";
		@SerializedName("res") public Resource[] resources;
		@SerializedName("name") public String name;
		@SerializedName("id") public int cityid;
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
		public double foodConsumption, foodConsumptionSupporter, foodConsumptionQueue;
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
			location = Coord.fromCityId(cityid);
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
			if (cityid < o) return -1;
			if (cityid > o) return 1;
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
					incoming = 0;
					Iterator<Trade> i = trade_in.iterator();
					while (i.hasNext()) {
						Trade t = i.next();
						if (t.state != Trade.Working) continue;
						if ((t.end > now) && (t.end < (timeLeft + now)) && (t.contents != null)) {
							//Log.v(TAG,name+" trade will get back in time, "+t.toString());
							int x;
							for (x=0; x<t.contents.length(); x++) {
								//Log.v(TAG,t.contents.toString());
								JSONObject o = t.contents.optJSONObject(x);
								int type = o.optInt("t");
								if (type == 4) { // food
									incoming += o.optInt("c");
								}
							}
						} else if (t.end < now) {
							Log.v(TAG,"trade already done "+name+" "+t.toString());
							Log.v(TAG,""+now);
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
	}
	public void parsePlayerUpdate(JSONObject d) throws JSONException {
		getFullPlayerData = false;
		JSONArray cg = d.optJSONArray("cg");
		boolean female = d.getBoolean("f");
		//Log.v(TAG,"cg:"+cg+" f:"+female);
		// FIXME check d.c array for changes to cities array
		JSONArray c = d.optJSONArray("c");
		//Log.v(TAG,"c:"+c);
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
		JSONArray u = p.optJSONArray("u");
		Log.v(TAG,"unit data:"+u);
		if (u != null) {
			currentCity.units = new UnitCount[20];
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
		Log.v(TAG,String.format("%s %b %b %d", c.name,c.autoBuildDefense,c.autoBuildEconomy,c.autoBuildTypeFlags));
		
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
	private ArrayList<Trade> parseTrades(JSONArray list, World world, int direction) {
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
		public int direction,id,start,end;
		public String DEBUG,cityName;
		@Deprecated private JSONArray contents; // FIXME, make it a proper object, contains c/t pairs
		public Trade(JSONObject t, World world, int direction) {
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
			contents = t.optJSONArray("r");
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
		JSONArray members = d.getJSONArray("m");
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
}
