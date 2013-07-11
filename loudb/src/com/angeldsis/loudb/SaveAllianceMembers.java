package com.angeldsis.loudb;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SaveAllianceMembers extends Transaction {
	int id;
	AllianceMemberRow[] members;
	public SaveAllianceMembers(int allianceid, AllianceMemberRow[] members) {
		id=allianceid;
		this.members = members;
	}
	@Override
	void internalRun() throws SQLException {
		PreparedStatement insert = link.con.prepareStatement("INSERT IGNORE INTO player (playerid) VALUES (?)");
		PreparedStatement update = link.con.prepareStatement("UPDATE player SET name=?, allianceid=? WHERE playerid = ?");
		int i;
		for (i=0; i < members.length; i++) {
			AllianceMemberRow row = members[i];
			insert.setInt(1, row.id);
			insert.execute();
			update.setString(1, row.name);
			update.setInt(2, id);
			update.setInt(3,row.id);
			update.execute();
		}
	}
}
