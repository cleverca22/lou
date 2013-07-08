package com.angeldsis.loudb;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.angeldsis.louapi.Report;

public class SaveReport extends Transaction {
	String raw;
	Report r;
	Client c;
	public SaveReport(String string, Report r, Client client) {
		raw = string;
		this.r = r;
		c = client;
	}
	@Override
	void internalRun() throws SQLException {
		PreparedStatement s = link.con.prepareStatement("INSERT IGNORE INTO report (sid,ts,json) VALUES (?,?,?)");
		s.setString(1, r.share);
		s.setLong(2, r.reportHeader.timestamp);
		s.setString(3, raw);
		s.execute();
		c.log("Saved report "+r.share+" "+r.reportHeader.toString());
	}
}
