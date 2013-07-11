package com.angeldsis.loudb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.net.ssl.SSLHandshakeException;

import org.json2.JSONArray;
import org.json2.JSONException;
import org.json2.JSONObject;
import org.json2.JSONTokener;

import com.angeldsis.louapi.IncomingAttack;
import com.angeldsis.louapi.Log;
import com.angeldsis.louapi.Report;
import com.angeldsis.louapi.data.AlliancePollRow;
import com.angeldsis.louapi.data.PublicCityInfo;
import com.angeldsis.louapi.data.UnitOrder;
import com.angeldsis.louapi.data.World;
import com.angeldsis.louapi.world.AllianceMapping;
import com.angeldsis.louapi.world.CityMapping;
import com.angeldsis.louapi.world.Dungeon;
import com.angeldsis.louapi.world.PlayerMapping;
import com.angeldsis.louapi.world.WorldParser;
import com.angeldsis.louapi.world.WorldParser.Cell;
import com.angeldsis.louapi.world.WorldParser.MapItem;
import com.angeldsis.louapi.world.WorldParser.WorldCallbacks;
import com.google.gson.Gson;

public class Client implements Runnable, WorldCallbacks {
	private static final String TAG = "Client";
	
	// hack used for debug info
	static ThreadLocal<World> currentWorld;
	InputStream is;
	OutputStream os;
	String id;
	static int lastId;
	HashMap<Integer,World> w;
	Socket s;
	WorldParser wp; // FIXME, share a world parser within a world?, or maybe even dont parse, use a dedicated bot acct
	private int currentCity;
	private String source;
	ArrayList<CityToSave> cities = new ArrayList<CityToSave>();
	SaveWorldCityData citysaver = new SaveWorldCityData(cities);
	Server parent;
	public Client(Socket s, Server server) throws IOException {
		currentWorld = new ThreadLocal<World>(); // FIXME?
		id = ""+lastId++;
		is = s.getInputStream();
		os = s.getOutputStream();
		this.s = s;
		s.setKeepAlive(true);
		source = s.getInetAddress().toString();
		//Log.v(TAG,"accepted new client, socket:"+s);
		byte[] msg = {0,0,0,0,3};
		os.write(msg, 0, 5);
		w = new HashMap<Integer,World>();
		parent = server;
		ThreadPool.getInstance().post(this);
	}
	@Deprecated void log(String msg) {
		Log.v(TAG,"id:"+id+" "+msg);
	}
	@Override
	public void run() {
		byte[] size = new byte[4];
		int read;
		try {
			int pos = 0;
			while ((read = is.read(size, pos, 4 - pos)) > 0) {
				pos += read;
				if (pos == 4) {
					ByteBuffer b = ByteBuffer.wrap(size);
					int realsize = b.getInt();
					readPayload(realsize);
					pos = 0;
				}
				if (is.available() > 0) log("available: "+is.available());
			}
			s.close();
		//} catch (SSLHandshakeException e) { 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			if (e instanceof SSLHandshakeException) {
			} else {
				e.printStackTrace();
				log("source:"+source);
			}
		}
		log(source+" disconnected?");
		parent.removeClient(this);
		s = null; // reduce memory leak problems
	}
	private void readPayload(int realsize) throws IOException {
		int pos2 = 0,read;
		byte[] buffer = new byte[realsize];
		while ((read = is.read(buffer,pos2,realsize-pos2)) > 0) {
			//log("pos2:"+pos2+" read:"+read);
			pos2 += read;
			if (pos2 == realsize) break;
		}
		String msg = new String(buffer);
		//System.out.println(msg);
		try {
			handleMsg(msg);
		} catch (Exception e) {
			log(msg);
			log("size: "+msg.length());
			log("real size:"+realsize);
			log("pos2: "+pos2);
			e.printStackTrace();
			System.exit(-1);
		}
	}
	private void handleMsg(String msg) throws JSONException {
		Object p = new JSONTokener(msg).nextValue();
		if (p instanceof JSONObject) {
			JSONObject o = (JSONObject) p;
			JSONObject error = o.optJSONObject("error");
			if (error != null) {
				JSONObject versions = o.optJSONObject("versions");
				ErrorMsg msg2 = new ErrorMsg(error,versions);
				ThreadPool.getInstance().post(msg2);
				return;
			}
			//System.out.println(o.toString(1));
			String func = o.getString("func");
			JSONObject req = o.getJSONObject("req");
			int hostid = o.optInt("hostid");
			int pathid = o.optInt("pathid");
			Object reply = o.get("reply");
			handlePacket(func,req,reply,hostid,pathid);
		} else {
			System.out.println(p.toString());
		}
	}
	private void handlePacket(String func, JSONObject req, Object reply, int hostid, int pathid) throws JSONException {
		//Log.v(TAG,"w:"+w);
		World thisWorld = w.get(pathid);
		if (thisWorld == null) {
			Log.v(TAG,"had to create world "+Worlds.pathToWorld[pathid]+" "+pathid);
			thisWorld = World.get(Worlds.pathToWorld[pathid]);
			w.put(pathid, thisWorld);
		}
		currentWorld.set(thisWorld);
		if (func.equals("GetPublicAllianceMemberList")) {
			int allianceid = req.getInt("id");
			JSONArray a = (JSONArray) reply;
			int i;
			AllianceMemberRow[] members = new AllianceMemberRow[a.length()];
			for (i=0; i < a.length(); i++) {
				JSONObject r = a.getJSONObject(i);
				AllianceMemberRow row = new AllianceMemberRow();
				row.rank = r.getInt("r");
				row.cities = r.getInt("c");
				row.score = r.getInt("p");
				row.name = r.getString("n");
				row.id = r.getInt("i");
				members[i] = row;
			}
			SaveAllianceMembers trans = new SaveAllianceMembers(allianceid,members);
			ThreadPool.getInstance().post(trans);
		} else if (func.equals("Poll")) {
			parsePoll(req,(JSONArray)reply);
		} else if (func.equals("SetFoodWarning")) {
		} else if (func.equals("GetPublicCityInfo")) {
			PublicCityInfo info = new PublicCityInfo(thisWorld,(JSONObject)reply);
			ThreadPool.getInstance().post(new SavePublicCityInfo(info,this));
		} else if (func.equals("GetReport")) {
			parseReport((JSONObject)reply);
		} else if (func.equals("GetSharedReport")) {
			parseReport((JSONObject) reply);
		} else if (func.equals("GetServerInfo")) {
			log(reply.toString());
			JSONObject r = (JSONObject) reply;
			SaveServerInfo s = new SaveServerInfo();
			s.serverName = r.getString("n").trim();
			JSONObject timedata = (JSONObject) new JSONTokener(r.getString("td")).nextValue();
			s.Ref = timedata.getLong("Ref");
			s.hostid = hostid;
			s.pathid = pathid;
			log("ref:"+s.Ref);
			ThreadPool.getInstance().post(s);
		} else if (func.equals("GetPlayerInfo")) {
			JSONObject r = (JSONObject) reply;
			thisWorld.Id = r.getInt("Id");
			thisWorld.Name = r.getString("Name");
			Log.v(TAG,"found player name");
		} else {
			log(String.format("%d:%d %s(%s)",hostid,pathid,func,req.toString()));
			//System.out.println(reply.toString());
		}
	}
	private void parsePoll(JSONObject req, JSONArray reply) throws JSONException {
		String[] reqs = req.optString("requests").split("\f");
		HashMap<String,String> reqs2 = new HashMap<String,String>();
		int i,j;
		for (String s : reqs) {
			int pos = s.indexOf(':');
			reqs2.put(s.substring(0,pos), s.substring(pos+1));
		}
		for (i=0; i<reply.length(); i++) {
			JSONObject p = reply.getJSONObject(i);
			String C = p.getString("C");
			if (C.equals("SYS")) {
				log(p.toString(1));
			} else if (C.equals("TIME")) {
			} else if (C.equals("CHAT")) {
			} else if (C.equals("MAT")) {
				JSONObject D = p.getJSONObject("D");
				//log(D.toString(1));
			} else if (C.equals("INV")) {
				//log(p.toString(1));
			} else if (C.equals("CITY")) {
				int cityid = Integer.parseInt(reqs2.get("CITY"));
				currentCity = cityid;
				JSONObject D = p.getJSONObject("D");
				JSONArray uo = D.optJSONArray("uo");
				if (uo != null) {
					String hack = uo.toString();
					log(hack);
					Gson gson = new Gson();
					UnitOrder[] obj = gson.fromJson(hack,UnitOrder[].class);
					for (j=0; j<obj.length; j++) {
						log(obj[j].toString());
					}
					SaveUnitOrders s = new SaveUnitOrders(cityid,obj);
					ThreadPool.getInstance().post(s);
				}
			} else if (C.equals("QUEST")) {
			} else if (C.equals("WORLD")) {
				if (wp == null) wp = new WorldParser(null);
				wp.parse(p, this);
				continue;
				/*JSONObject D = p.getJSONObject("D");
				try {
					Map<Integer,WorldCellRequest> requested = new HashMap<Integer,WorldCellRequest>();
					for (j=0; j<reqs.length; j++) {
						String[] parts = reqs[j].split(":");
						if (parts[0].equals("WORLD")) {
							//log(reqs[j]);
							BaseLou y = new BaseLou(reqs[j].substring(6));
							while (y.offset < y.data.length()) {
								int first = y.read2Bytes();
								int CV = first >> 10; // return value from this.CV(firstb)
								int firstb = first & 0x3ff; // bit-packed g/h
								int g = firstb >> 5;
								int h = firstb & 0x1f;
								
								// version# for this cell on the map?
								int BV = y.readMultiBytes(); // return value from this.BV(firstb);
								//log(String.format("CV%d g=%2d h%d version=%6d", CV,g,h,BV));
								requested.put((g << 5) | h,new WorldCellRequest(g,h,BV));
							}
							
							break;
						}
					}
					parseWorldPacket(requested,D);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
			} else if (C.equals("PLAYER")) {
				JSONObject D = p.getJSONObject("D");
				//log(D.toString(1));
				//Log.v(TAG,"player packet ^^^");
			} else if (C.equals("ALLIANCE")) {
				JSONObject D = p.getJSONObject("D");
				int allianceid = p.optInt("id");
				if (allianceid != 0) {
					//log(D.getString("a"));
					JSONArray m = D.optJSONArray("m");
					AlliancePollRow[] rows = new AlliancePollRow[m.length()];
					for (j=0; j<m.length(); j++) {
						JSONObject member = m.getJSONObject(j);
						AlliancePollRow r = new AlliancePollRow(member);
						rows[j] = r;
					}
					// FIXME, save allianceid too
					SaveAlliancePoll s = new SaveAlliancePoll(rows);
					ThreadPool.getInstance().post(s);
				}
			} else if (C.equals("ALL_AT")) {
				log(p.toString(1));
				JSONObject D = p.getJSONObject("D");
				JSONArray a = D.optJSONArray("a");
				if (a == null) continue;
				IncomingAttack[] all_at = new IncomingAttack[a.length()];
				for (j=0; j<a.length(); j++) {
					JSONObject att = a.getJSONObject(j);
					int id = att.getInt("i");
					IncomingAttack attack = new IncomingAttack(null,id);
					attack.updateAllianceType(att,currentWorld.get());
					all_at[j] = attack;
				}
				
				HashMap<Integer,AttackGroup> groups = new HashMap<Integer,AttackGroup>();
				for (j=0; j<all_at.length; j++) {
					IncomingAttack in = all_at[j];
					AttackGroup out = groups.get(in.tc);
					if (out == null) {
						out = new AttackGroup();
						groups.put(in.tc, out);
						out.target_city = in.targetCityName;
					}
					out.attacker_size += in.total_strength_attacker;
					out.count++;
				}
				log("cityname total ts(attack count)");
				for (AttackGroup g : groups.values()) {
					if (g.attacker_size < 2000) continue;
					log(g.target_city+": "+g.attacker_size+"("+g.count+")");
				}
			} else log(C);
		}
	}
	/*private void parseWorldPacket(Cell cell,Map<Integer, WorldCellRequest> requested, JSONObject d) throws Exception {
		//log("WORLD");
		//hidewrap = false;
		JSONArray s = d.getJSONArray("s");
		int j;
		for (j=0; j < s.length(); j++) {
			JSONObject x = s.getJSONObject(j);
			int v = x.getInt("v");
			int i = x.getInt("i");
			WorldCellRequest req = requested.get(i);
			int diff = v - req.version;
			int coarserow = i >> 5;
			int coarsecol = i & 0x1f;
			//log(String.format("row/col %2d/%2d",coarserow,coarsecol));
			if (diff > 10) {
				//log(x.toString(1));
			}
			// maybe bit-packed terrain??
			//Object t = x.opt("t");
			//if (t != null) log("t:"+t.toString());
			Object u = x.opt("u");
			if (u != null) log("u:"+u.toString());
			Object r = x.opt("r");
			//if (r != null) log("r:"+r.toString());
			Object p = x.opt("p");
			if (p != null) log("p:"+p.toString());
			Object a = x.opt("a");
			//if (a != null) log("a:"+a.toString());
			JSONArray c = x.optJSONArray("c");
			JSONArray d2 = x.optJSONArray("d");
			//String json = x.toString();
			//int index = json.indexOf("Lord");
			//if (index == -1) continue;
			//log(String.format("index:%d v:%5d %4x row:%d column:%d",index,v,v,row,maybecolumn));
			if (c != null) parseWorldLevel2c(cell,coarserow,coarsecol,c);
			if (d2 != null) parseWorldLevel2d(cell,coarserow,coarsecol,d2); 
			//log("line 206: "+x.toString());
		}
	}
	private void parseWorldLevel2c(Cell cell,int coarserow, int coarsecolumn, JSONArray c) throws Exception {
		AllianceMapping[] atest = new AllianceMapping[32];
		PlayerMapping[] ptest = new PlayerMapping[256];
		ArrayList<CityMapping> ctest = new ArrayList<CityMapping>();
		// c packets seem to be change, d packets are initial data maybe?
		// it seems to send seperate packets for alliance, player, and city, the order seems to be alliance, city, player
		int i,Points;
		for (i=0; i<c.length(); i++) {
			String x = c.getString(i);
			//if (x.indexOf("Lord") == -1) continue;
			BaseLou y = new BaseLou(x);
			int type1 = y.readByte();
			//log("type1: "+type1+" x: "+x);
			switch (type1) {
			case 2:
				parseWorldLevel2c2(cell,coarserow,coarsecolumn,y,ctest);
				break;
			case 4:
				PlayerMapping p = new PlayerMapping(y);
				ptest[p.shortid] = p;
				break;
			case 6:
				AllianceMapping a = new AllianceMapping(y);
				atest[a.shortid] = a;
				break;
			default:
				//log("type1:"+type1);
			}
		}
		for (i=0; i<128; i++) {
			PlayerMapping p = ptest[i];
			if (p == null) continue;
			/*if ((p.id == 4121) || (p.id == 2839)) {
				log("case 4: shortid:"+p.shortid+" "+p.id+" "+p.Points+" "+p.Alliance+" "+p.name);
				AllianceMapping a = atest[p.Alliance];
				if (a == null) {
					log("alliance is null!");
				}
				else log(String.format("alliance p:%d points:%d id:%d name:%s", a.shortid,a.points,a.id,a.name));
			}*
		}
		ArrayList<WorldCityData> tosave = new ArrayList<WorldCityData>();
		for (CityMapping city : ctest) {
			String citymsg = String.format("%d city!: %15s, score: %4d owner:%2d",city.location.format(),city.name,city.Points,city.shortplayer);
			if (city.name.equals("030flippergu") || 
					city.name.equals("C21_3")) {
				//log(citymsg);
			}
			PlayerMapping p = ptest[city.shortplayer];
			AllianceMapping a = null;
			//if (p != null) log("owner? "+p.name);
			//else log("owner null");
			String playermsg;
			String amsg = " player is null";
			if (p == null) playermsg = " player is null";
			else {
				playermsg = String.format(" PLAYER name:%11s",p.name);
				a = atest[p.shortAlliance];
				if (a == null) amsg = String.format(" ALLIANCE null %d",p.shortAlliance);
				else amsg = String.format(" ALLIANCE name:%s",a.name);
			}
			if (p == null) log(citymsg+playermsg+amsg);
			else if (p.id == 4121) log(citymsg+playermsg+amsg);
			if ((p != null) && (a != null)) {
				tosave.add(new WorldCityData(city,a,p));
			}
			SaveWorldCityData saver = new SaveWorldCityData(tosave);
			ThreadPool.getInstance().post(saver);
		}
	}
	private void parseWorldLevel2c2(Cell cell,int coarserow, int coarsecol, BaseLou y, ArrayList<CityMapping> ctest) throws Exception {
		int c2 = y.read2Bytes();
		int d = (c2 & 0x1f);
		int e = ((c2 >> 5) & 0x1f);
		int f = (c2 >> 10);
		// refer to FIND ME2 in output-1.js
		switch (f) {
		case 1: // city
			int col = (coarsecol*32) + d;
			int row = (coarserow*32) + e;
			//log(String.format("col/row %2d/%2d",col,row));
			CityMapping city = new CityMapping((e << 0x10) | d,y,col,row, cell);
			//log(String.format("%d %d",d,e));
			//if (Castle) log(String.format("Castle!: %10s, score: %d owner:%d",remainder,Points,Player));
			//if (!Castle) log(String.format("city!: %10s, score: %d owner:%d",remainder,Points,Player));
			ctest.add(city);
			break;
		case 2: // dungeon
		case 3: // boss
		case 4: // moongate
		default:
			//log(String.format("%d %d %d",d,e,f));
			break;
		case 5: // shrine
			log(y.readRest());
			break;
		case 6: // lawless
		case 7: // free slot
		}
	}
	private void parseWorldLevel2d(Cell cell,int row, int maybecolumn, JSONArray c) throws Exception {
		int i;
		for (i=0; i<c.length(); i++) {
			String detail = c.getString(i);
			if (detail.indexOf("City") == -1) continue;
			BaseLou y = new BaseLou(detail);
			ArrayList<CityMapping> ctest = new ArrayList<CityMapping>();
			parseWorldLevel2c2(cell,0,0,y,ctest);
			for (CityMapping c2 : ctest) {
				//log(String.format("name:%s owner:%d",c2.name,c2.shortplayer));
			}
			//int c2 = y.read2Bytes();
			//int d = (c2 & 0x1f);
			//int e = ((c2 >> 5) & 0x1f);
			//int f = (c2 >> 10);
			//log("type1:"+f+" "+y.readRest());
		}
	}*/
	private class AttackGroup {
		public String target_city;
		public int attacker_size,count;
	}
	private void parseReport(JSONObject reply) {
		Report r = new Report(reply);
		if (r.reportHeader.generalType == Report.types.general.alliance) return;
		SaveReport trans = new SaveReport(reply.toString(),r,this);
		ThreadPool.getInstance().post(trans);
	}
	boolean changed = false;
	@Override
	public void cellUpdated(Cell c, ArrayList<Object> changes) {
		synchronized (cities) {
			for (Object o : changes) {
				if (o instanceof Dungeon) {
					changed = true;
				} else if (o instanceof CityMapping) {
					CityMapping city = (CityMapping) o;
					PlayerMapping player = city.playerLink;
					if (player == null) continue; // FIXME
					AllianceMapping alliance = player.allianceLink;
					if (player.id == 2839) {
						Log.v(TAG,String.format("citydebug1 %s %s",city.location.format(),city.name));
					}
					CityToSave s = new CityToSave();
					s.city = city;
					s.player = player;
					s.alliance = alliance;
					cities.add(s);
				}
			}
		}
		if (cities.size() > 0) citysaver.wake();
		if (!changed) return;
		changed = false;
		ArrayList<Dungeon> candidates = new ArrayList<Dungeon>();
		for (MapItem i : c.objects) {
			if (i == null) continue;
			int x = currentCity >> 0x10;
			int y = currentCity & 0xffff;
			if (i instanceof Dungeon) {
				Dungeon d = (Dungeon) i;
				if (d.level < 8) continue;
				if (d.type != 4) continue;
				int disty = y-d.location.y;
				int distx = x-d.location.x;
				d.dist = Math.sqrt((disty*disty) + (distx*distx));
				if (d.dist > 20) continue;
				candidates.add(d);
			}
		}
		Dungeon[] list = new Dungeon[candidates.size()];
		candidates.toArray(list);
		Arrays.sort(list);
		//log("start");
		int count = 0;
		for (Dungeon d : list) {
			if (count++ > 5) return;
			log(String.format("dist:%f %s %s, level%d, progress%d",d.dist,d.location.format(),d.getType(),d.level,d.progress));
		}
	}
}
