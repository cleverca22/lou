package com.angeldsis.loudb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.angeldsis.louapi.data.PublicCityInfo;

public class SavePublicCityInfo extends Transaction {
	PublicCityInfo i;
	Client c;
	public SavePublicCityInfo(PublicCityInfo info, Client client) {
		i = info;
		c = client;
	}
	@Override
	void internalRun() throws SQLException {
		PreparedStatement p = link.con.prepareStatement("INSERT INTO alliance (allianceid,name) VALUES (?,?) ON DUPLICATE KEY UPDATE name=?");
		p.setInt(1, i.alliance.id);
		p.setString(2, i.alliance.name);
		p.setString(3,i.alliance.name);
		p.execute();
		p = link.con.prepareStatement("INSERT INTO player (playerid,name,allianceid) VALUES (?,?,?) ON DUPLICATE KEY UPDATE name=?, allianceid=?");
		p.setInt(1, i.player.getId());
		p.setString(2, i.player.getName());
		p.setInt(3, i.player.alliance.id);
		p.setString(4, i.player.getName());
		p.setInt(5, i.player.alliance.id);
		p.execute();
		p = link.prepare("SELECT cellid FROM cell WHERE x=? AND y=?");
		p.setInt(1, i.x);
		p.setInt(2, i.y);
		ResultSet r = p.executeQuery();
		int cellid = -1;
		if (r.next()) { // it exists, use cellid
			cellid = r.getInt(1);
		} else { // doesnt exist, need to insert it
			p = link.prepare("INSERT INTO cell (x,y) VALUES (?,?)",Statement.RETURN_GENERATED_KEYS);
			p.setInt(1, i.x);
			p.setInt(2, i.y);
			p.executeUpdate();
			r = p.getGeneratedKeys();
			r.next();
			cellid = r.getInt(1);
		}
		c.log("cellid:"+cellid+" x:"+i.x+" y:"+i.y);
		p = link.prepare("INSERT INTO city (cellid,playerid,name) VALUES (?,?,?) ON DUPLICATE KEY UPDATE playerid=?,name=?");
		p.setInt(1, cellid);
		p.setInt(2, i.player.getId());
		p.setString(3, i.name);
		p.setInt(4, i.player.getId());
		p.setString(5,i.name);
		p.executeUpdate();
	}
}
