package com.angeldsis.loudb;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SaveServerInfo extends Transaction {
	public String serverName;
	public long Ref;
	public int hostid,pathid;
	@Override
	void internalRun() throws SQLException {
		PreparedStatement p = link.prepare("INSERT IGNORE INTO servers (name,Ref,hostid,pathid) VALUES (?,?,?,?)");
		p.setString(1, serverName);
		p.setLong(2, Ref);
		p.setInt(3, hostid);
		p.setInt(4, pathid);
		p.executeUpdate();
		p = link.prepare("UPDATE servers SET Ref=? WHERE pathid = ?");
		p.setLong(1, Ref);
		p.setInt(2, pathid);
		p.executeUpdate();
	}
}
