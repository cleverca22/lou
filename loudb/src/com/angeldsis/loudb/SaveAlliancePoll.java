package com.angeldsis.loudb;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.angeldsis.louapi.data.AlliancePollRow;

public class SaveAlliancePoll extends Transaction {
	AlliancePollRow[] rows;
	public SaveAlliancePoll(AlliancePollRow[] rows) {
		this.rows = rows;
	}
	@Override
	void internalRun() throws SQLException {
		// FIXME, save rank history as well
		PreparedStatement p = link.prepare("INSERT INTO player (playerid,name) VALUES (?,?) ON DUPLICATE KEY UPDATE name=?,points=?");
		int i;
		for (i=0; i<rows.length; i++) {
			p.setInt(1, rows[i].id);
			p.setString(2, rows[i].name);
			p.setString(3, rows[i].name);
			p.setInt(4, rows[i].points);
			p.executeUpdate();
		}
	}
}
