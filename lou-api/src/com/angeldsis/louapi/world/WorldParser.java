package com.angeldsis.louapi.world;

import java.util.ArrayList;

import org.json2.JSONArray;
import org.json2.JSONException;
import org.json2.JSONObject;

import com.angeldsis.louapi.Log;
import com.angeldsis.louapi.LouState;
import com.angeldsis.louapi.RPC;
import com.angeldsis.louapi.data.BaseLou;
import com.angeldsis.louapi.data.Coord;

public class WorldParser {
	private static final String TAG = "WorldParser";
	public int mincol,maxcol, minrow,maxrow;
	public Cell[] cells = new Cell[1024];
	private boolean enabled;
	private RPC rpc;
	
	public WorldParser(RPC rpc) {
		mincol = 4;
		maxcol = 4;
		minrow = 7;
		maxrow = 7;
		this.rpc = rpc;
	}
	public String getRequestDetails() {
		try {
		int row,col;
			BaseLou y = new BaseLou();
			for (row=mincol; row<=maxcol; row++) {
				for (col=minrow; col<=maxrow; col++) {
					int id = (col << 5) | row;
					if (cells[id] == null) cells[id] = new Cell(id);
					y.write2Bytes(id);
					y.writeManyBytes(cells[id].version);
				}
			}
			return y.getOutput();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public void parse(JSONObject packet,WorldCallbacks cb) throws JSONException {
		JSONObject D = packet.getJSONObject("D");
		JSONArray s = D.getJSONArray("s");
		int i;
		try {
			for (i=0; i<s.length(); i++) {
				ArrayList<Object> changes = new ArrayList<Object>();
				JSONObject x = s.getJSONObject(i);
				int id = x.getInt("i");
				int row = id >> 5;
				int col = id & 0x1f;
				int version = x.getInt("v");
				
				if (cells[id] == null) cells[id] = new Cell(id);
				cells[id].version = version;
				
				JSONArray d = x.optJSONArray("d");
				JSONArray p = x.optJSONArray("p");
				JSONArray a = x.optJSONArray("a");
				JSONArray c = x.optJSONArray("c");
				JSONArray t = x.optJSONArray("t");
				if ((p != null) && (p.length() > 0)) {
					for (int y=0; y<p.length();y++) {
						BaseLou stream = new BaseLou(p.getString(y));
						PlayerMapping player = new PlayerMapping(stream);
						cells[id].players[player.shortid] = player;
					}
				}
				// d is the initial change data?
				if ((d != null) && (d.length() > 0)) parseWorldChanges(cells[id],d,changes);
				//if ((a != null) && (a.length() > 0)) Log.v(TAG,"a="+a.toString());
				if ((c != null) && (c.length() > 0)) parseWorldChanges2(cells[id],c,changes);
				if ((t != null) && (t.length() > 0)) Log.v(TAG,"t="+t.toString());
				for (int y=0; y<changes.size(); y++) {
					Object o = changes.get(y);
					if (o instanceof CityMapping) {
						CityMapping city = (CityMapping) o;
						city.playerLink = cells[id].players[city.shortplayer];
						//System.out.println(String.format("%s player %d %s %s",city.location.format(),city.shortplayer,city.playerLink != null ? city.playerLink.name:"",city.playerLink));
					} else if (o instanceof PlayerMapping) {
						PlayerMapping player = (PlayerMapping) o;
						player.allianceLink = cells[id].alliances[player.shortAlliance];
					}
				}
				cb.cellUpdated(cells[id],changes);
			}
		} catch (Exception e) {
			if (e instanceof JSONException) throw (JSONException) e;
			e.printStackTrace();
		}
	}
	private void parseWorldChanges2(Cell cell,JSONArray d, ArrayList<Object> changes) throws Exception {
		int i;
		for (i=0; i<d.length(); i++) {
			String x = d.optString(i);
			BaseLou y = new BaseLou(x);
			int type1 = y.readByte();
			switch (type1) {
			case 2:
				parseWorldChange(cell,y,changes);
				break;
			case 3: // deletes object?
				int something = y.read2Bytes();
				int finerow = (something >> 5) & 0x1f;
				int finecol = something & 0x1f;
				int col = cell.getFineCol() + finecol;
				int row = cell.getFineRow() + finerow;
				int fineid = (finerow << 5) | finecol;
				cell.objects[fineid] = null;
				Log.v(TAG,String.format("%3d:%3d change type %d a/b %d/%d",col,row,type1,finerow,finecol));
				break;
			case 4:
				PlayerMapping p = new PlayerMapping(y);
				cell.players[p.shortid] = p;
				break;
			case 6:
				AllianceMapping a = new AllianceMapping(y);
				cell.alliances[a.shortid] = a;
				break;
			default:
				Log.v(TAG,String.format("%3d:%3d change type %d %s",cell.getFineCol(),cell.getFineRow(),type1,x));
			}
		}
	}
	private void parseWorldChange(Cell cell,BaseLou y, ArrayList<Object> changes) throws Exception {
		int packed = y.read2Bytes();
		int finecol = (packed & 0x1f);
		int finerow = ((packed >> 5) & 0x1f);
		int f = (packed >> 10);
		int fineid = (finerow << 5) | finecol;
		int col,row;
		
		switch (f) {
		case 1: // city
			// FIXME, clean up this code, and set playerid right
			col = cell.getFineCol() + finecol;
			row = cell.getFineRow() + finerow;
			CityMapping city = (CityMapping) cell.objects[fineid];
			if (city == null) {
				city = new CityMapping((finerow << 0x10) | finecol,y,col,row,cell);
				city.location = new Coord(cell.getFineCol() + finecol,cell.getFineRow() + finerow);
				cell.objects[fineid] = city;
			}
			changes.add(city);
			//log(String.format("%d %d",d,e));
			//if (city.Castle) System.out.println(String.format("Castle!: score: %d owner:%d",city.Points,city.shortplayer));
			//if (!Castle) log(String.format("city!: %10s, score: %d owner:%d",remainder,Points,Player));
			//ctest.add(city);
			//Log.v(TAG,String.format("city %3d:%3d points:%5d name:%s",city.col,city.row,city.Points,city.name));
			break;
		case 2: // dungeon
			Dungeon d = (Dungeon) cell.objects[fineid];
			if (d == null) {
				d = new Dungeon();
				d.stateObj = rpc.state;
				cell.objects[fineid] = d;
			}
			d.location = new Coord(cell.getFineCol() + finecol,cell.getFineRow() + finerow);

			packed = y.read4Bytes();
			d.state = ((packed & 1) != 0);
			d.type = ((packed >> 1) & 15);
			d.level = ((packed >> 5) & 15);
			d.progress = ((packed >> 9) & 0x7f);
			d.slot = ((packed >> 0x10) & 0x1f);
			d.startStep = y.readMultiBytes();
			if ((d.level > 7)) {
				//Log.v(TAG,String.format("%3d:%3d %s, level%d, progress%d",d.col,d.row,d.getType(),d.level,d.progress));
			}
			changes.add(d);
			break;
		case 3: // boss
			packed = y.read3Bytes();
			Boss boss = (Boss) cell.objects[fineid];
			if (boss == null) {
				boss = new Boss();
				cell.objects[fineid] = boss;
			}
			boss.location = new Coord(cell.getFineCol() + finecol,cell.getFineRow() + finerow);
			boss.state = ((packed&1) != 0);
			boss.bossType = ((packed>>1)&15);
			boss.bossLevel = ((packed>>5)&15);
			boss.slot = ((packed>>9)&0x1f);
			boss.startStep = y.readMultiBytes();
			break;
		case 4: // moongate
			Moongate mg = (Moongate) cell.objects[fineid];
			if (mg == null) {
				mg = new Moongate();
				cell.objects[fineid] = mg;
			}
			mg.location = new Coord(cell.getFineCol() + finecol,cell.getFineRow() + finerow);
			mg.state = y.readByte();
			mg.activationStep = y.readMultiBytes();
			break;
		case 5: // shrine
			Shrine s = (Shrine) cell.objects[fineid];
			if (s == null) {
				s = new Shrine();
				cell.objects[fineid] = s;
			}
			s.location = new Coord(cell.getFineCol() + finecol,cell.getFineRow() + finerow);
			s.type = y.readByte();
			//Log.v(TAG,String.format("SHRINE %s type:%d",s.location.format(),s.type));
			break;
		case 6: // lawless
			LawlessCity lc = (LawlessCity) cell.objects[fineid];
			if (lc == null) {
				lc = new LawlessCity();
				cell.objects[fineid] = lc;
			}
			lc.location = new Coord(cell.getFineCol() + finecol,cell.getFineRow() + finerow);
			
			//col = cell.getFineCol() + finecol;
			//row = cell.getFineRow() + finerow;
			lc.flags = y.readByte();
			lc.points = y.readMultiBytes();
			//Log.v(TAG,String.format("lawless %d:%d flags:%d points:%d",col,row,lc.flags,lc.points));
		case 7: // free slot
			//Log.v(TAG,String.format("cell:%d packed:%s d:%2d e:%2d f:%d x:%s",cell.id,packed,d2,e,f,x));
			break;
		default:
			//Log.v(TAG,String.format("cell:%d packed:%s d:%2d e:%2d f:%d x:%s",cell.id,packed,d2,e,f,y.readRest()));
			//log(String.format("%d %d %d",d,e,f));
			break;
		}
	}
	private void parseWorldChanges(Cell cell, JSONArray d, ArrayList<Object> changes) throws Exception {
		int i;
		for (i=0; i < d.length(); i++) {
			String x = d.getString(i);
			BaseLou y = new BaseLou(x);
			parseWorldChange(cell, y, changes);
		}
	}
	public static class Cell {
		public AllianceMapping[] alliances;
		public PlayerMapping[] players;
		public MapItem[] objects;
		public int version,id;
		public Cell(int id2) {
			id = id2;
			players = new PlayerMapping[1024]; // FIXME
			alliances = new AllianceMapping[1024]; // FIXME
			objects = new MapItem[1024]; // FIXME
		}
		int getFineRow() {
			// FIXME
			return (id >> 5) * 32;
		}
		int getFineCol() {
			// FIXME
			return (id & 0x1f) * 32;
		}
	}
	public static class MapItem {
		public Coord location;
		public LouState stateObj;
	}
	public interface WorldCallbacks {
		void cellUpdated(Cell c, ArrayList<Object> changes);
	}
	public void enable() {
		enabled = true;
		if (rpc != null) rpc.pollSoon();
	}
	public void disable() {
		enabled = false;
	}
	public boolean isEnabled() {
		return enabled;
	}
}
