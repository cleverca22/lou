package com.angeldsis.loudb;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.angeldsis.louapi.data.UnitOrder;

public class SaveUnitOrders extends Transaction {
	UnitOrder[] uo;
	int cityid,x,y;
	public SaveUnitOrders(int cityid, UnitOrder[] obj) {
		uo = obj;
		this.cityid = cityid;
		x = cityid >> 0x10;
		y = cityid & 0xffff;
	}
	@Override void internalRun() throws SQLException {
		// FIXME, remove lastconfirmed, just delete missing
		PreparedStatement p = link.prepare("INSERT IGNORE INTO unitorder (id,type) VALUES (?,?)");
		PreparedStatement p2 = link.prepare("UPDATE unitorder SET startStep = ?, endStep = ?, playername=?, x=?,y=?,state=?,lastconfirmed=NOW() WHERE id = ?");
		int i;
		for (i=0; i<uo.length; i++) {
			p.setInt(1, uo[i].id);
			p.setInt(2, uo[i].type);
			p.executeUpdate();
			p2.setInt(1, uo[i].startStep);
			p2.setInt(2, uo[i].endStep);
			p2.setString(3, uo[i].playerName);
			p2.setInt(4,y);
			p2.setInt(5,x);
			p2.setInt(6, uo[i].state);
			p2.setInt(7, uo[i].id);
			p2.executeUpdate();
		}
	}
}
