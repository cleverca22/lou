package com.angeldsis.louapi.world;

import org.json2.JSONArray;
import org.json2.JSONException;
import org.json2.JSONObject;

import com.angeldsis.louapi.Log;
import com.angeldsis.louapi.data.BaseLou;

public class WorldParser {
	private static final String TAG = "WorldParser";
	int mincol,maxcol, minrow,maxrow;
	Cell[] cells = new Cell[500];
	
	public WorldParser() {
		mincol = 4;
		maxcol = 4;
		minrow = 7;
		maxrow = 7;
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
				// d is the initial change data?
				if ((d != null) && (d.length() > 0)) parseWorldChanges(cells[id],d,cb);
				//if ((p != null) && (p.length() > 0)) Log.v(TAG,"p="+p.toString());
				//if ((a != null) && (a.length() > 0)) Log.v(TAG,"a="+a.toString());
				if ((c != null) && (c.length() > 0)) parseWorldChanges2(cells[id],c,cb);
				if ((t != null) && (t.length() > 0)) Log.v(TAG,"t="+t.toString());
				cb.cellUpdated(cells[id]);
			}
		} catch (Exception e) {
			if (e instanceof JSONException) throw (JSONException) e;
			e.printStackTrace();
		}
	}
	private void parseWorldChanges2(Cell cell,JSONArray d, WorldCallbacks cb) throws Exception {
		int i;
		for (i=0; i<d.length(); i++) {
			String x = d.optString(i);
			BaseLou y = new BaseLou(x);
			int type1 = y.readByte();
			switch (type1) {
			case 2:
				parseWorldChange(cell,y,false,cb);
				break;
			case 4:
				// FIXME player data
				break;
			case 6:
				// FIXME alliance data
				break;
			case 3: // deletes object?
				int something = y.read2Bytes();
				int a = (something >> 5) & 0x1f;
				int b = something & 0x1f;
				int col = cell.getFineCol() + b;
				int row = cell.getFineRow() + a;
				Log.v(TAG,String.format("%3d:%3d change type %d a/b %d/%d",col,row,type1,a,b));
				break;
			default:
				Log.v(TAG,String.format("%3d:%3d change type %d %s",cell.getFineCol(),cell.getFineRow(),type1,x));
			}
		}
	}
	private void parseWorldChange(Cell cell,BaseLou y,boolean initial, WorldCallbacks cb) throws Exception {
		int packed = y.read2Bytes();
		int finecol = (packed & 0x1f);
		int finerow = ((packed >> 5) & 0x1f);
		int f = (packed >> 10);
		int fineid = (finerow << 5) | finecol;
		
		switch (f) {
		case 1: // city
			int col = cell.getFineCol() + finecol;
			int row = cell.getFineRow() + finerow;
			CityMapping city = new CityMapping((finerow << 0x10) | finecol,y,col,row);
			//log(String.format("%d %d",d,e));
			//if (Castle) log(String.format("Castle!: %10s, score: %d owner:%d",remainder,Points,Player));
			//if (!Castle) log(String.format("city!: %10s, score: %d owner:%d",remainder,Points,Player));
			//ctest.add(city);
			//Log.v(TAG,String.format("city %3d:%3d points:%5d name:%s",city.col,city.row,city.Points,city.name));
			break;
		case 2: // dungeon
			Dungeon d = cell.dungeons[fineid];
			if (d == null) {
				d = new Dungeon();
				cell.dungeons[fineid] = d;
			}
			d.col = cell.getFineCol() + finecol;
			d.row = cell.getFineRow() + finerow;

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
			cb.dungeonUpdated(d);
			break;
		case 3: // boss
		case 4: // moongate
		default:
			//Log.v(TAG,String.format("cell:%d packed:%s d:%2d e:%2d f:%d x:%s",cell.id,packed,d2,e,f,y.readRest()));
			//log(String.format("%d %d %d",d,e,f));
			break;
		case 5: // shrine
			//Log.v(TAG,String.format("cell:%d packed:%s d:%2d e:%2d f:%d x:%s",cell.id,packed,d2,e,f,x));
			//log(y.readRest());
			break;
		case 6: // lawless
		case 7: // free slot
			//Log.v(TAG,String.format("cell:%d packed:%s d:%2d e:%2d f:%d x:%s",cell.id,packed,d2,e,f,x));
		}
	}
	private void parseWorldChanges(Cell cell, JSONArray d, WorldCallbacks cb) throws Exception {
		int i;
		for (i=0; i < d.length(); i++) {
			String x = d.getString(i);
			BaseLou y = new BaseLou(x);
			parseWorldChange(cell,y,true,cb);
		}
	}
	public static class Cell {
		public Dungeon[] dungeons;
		public int version,id;
		public Cell(int id2) {
			id = id2;
			dungeons = new Dungeon[1024]; // FIXME
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
	public interface WorldCallbacks {
		void cellUpdated(Cell c);
		void dungeonUpdated(Dungeon d);
	}
}
