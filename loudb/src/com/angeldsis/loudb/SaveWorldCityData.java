package com.angeldsis.loudb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import com.angeldsis.louapi.Log;
import com.angeldsis.louapi.data.Coord;

public class SaveWorldCityData extends Transaction {
	private static final String TAG = "SaveWorldCityData";
	ArrayList<CityToSave> d;
	boolean awake = false;
	public SaveWorldCityData(ArrayList<CityToSave> cities) {
		d = cities;
	}
	@Override void internalRun() throws Exception {
		int cellid = -1;
		try {
		//PreparedStatement findCellId = link.prepare("SELECT cellid FROM cell WHERE x = ? AND y = ? FOR UPDATE");
		//PreparedStatement makeCellId = link.prepare("INSERT INTO cell (x,y,temp,cityid) VALUES (?,?,'world data',?)",Statement.RETURN_GENERATED_KEYS);
		//PreparedStatement fixCellId = link.prepare("UPDATE cell SET cityid = ? WHERE cellid = ?");
		PreparedStatement findCityId = link.prepare("SELECT 1 FROM city WHERE cityid = ? FOR UPDATE");
		PreparedStatement makeCityId = link.prepare("INSERT INTO city (cityid,playerid,source) VALUES (?,-1,'world')",Statement.RETURN_GENERATED_KEYS);
		PreparedStatement updateCity = link.prepare("UPDATE city SET name = ?,playerid=?,castle=?,score=? WHERE cityid = ?");
		while (d.size() > 0) {
			CityToSave x;
			synchronized (d) {
				x = d.remove(0);
			}
			Coord loc = x.city.location;
			if (d.size() % 500 == 0) {
				Statement s = link.con.createStatement();
				s.execute("COMMIT");
				Log.v("SaveWorldCityData","saving city at "+loc.format()+" "+d.size());
				s.execute("BEGIN");
			}
			//findCellId.setInt(1, loc.x);
			//findCellId.setInt(2, loc.y);
			//ResultSet r = findCellId.executeQuery();
			//cellid = -1;
			//if (r.next()) {
				//cellid = r.getInt(1);
				//fixCellId.setInt(1, loc.toCityId());
				//fixCellId.setInt(2, cellid);
				//fixCellId.executeUpdate();
			//} else {
				//makeCellId.setInt(1, loc.x);
				//makeCellId.setInt(2, loc.y);
				//makeCellId.setInt(3, loc.toCityId());
				//makeCellId.executeUpdate();
				//r = makeCellId.getGeneratedKeys();
				//r.next();
				//cellid = r.getInt(1);
			//}
			
			findCityId.setInt(1, loc.toCityId());
			ResultSet r = findCityId.executeQuery();
			if (r.next()) {
			} else {
				makeCityId.setInt(1, loc.toCityId());
				makeCityId.executeUpdate();
				r = makeCityId.getGeneratedKeys();
				r.next();
			}
			updateCity.setString(1, x.city.name);
			updateCity.setInt(2, x.player.id);
			updateCity.setBoolean(3, x.city.Castle);
			updateCity.setInt(4, x.city.Points);
			updateCity.setInt(5, loc.toCityId());
			updateCity.executeUpdate();
			//System.out.println(cellid+" "+cityid+" "+x.col +":"+x.row);
		}
		} catch (Exception e) {
			awake = false;
			Log.v(TAG,"cellid was "+cellid);
			throw e;
		}
		awake = false;
		Log.v("SaveWorldCityData","sleep time");
	}
	public synchronized void wake() {
		if (awake) return;
		awake = true;
		Log.v("SaveWorldCityData","waking up");
		ThreadPool.getInstance().post(this);
	}
}
