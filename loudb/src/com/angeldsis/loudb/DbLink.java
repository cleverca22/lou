package com.angeldsis.loudb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

public class DbLink {
	static int lastid = 0;
	static private String user = "root", password = "fa0Ahvia",
			url = "jdbc:mysql://loudb.ch35ondylszf.us-east-1.rds.amazonaws.com/loudb";
	Connection con = null;
	int id;
	DbLink() throws SQLException {
		id = lastid++;
		con = DriverManager.getConnection(url, user, password);
	}
	HashMap<String,PreparedStatement> statement_cache = new HashMap<String,PreparedStatement>();
	static public void init() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	public PreparedStatement prepare(String sql) throws SQLException {
		return prepare(sql,0);
	}
	public PreparedStatement prepare(String sql, int flags) throws SQLException {
		PreparedStatement p;
		if (statement_cache.containsKey(sql)) {
			p = statement_cache.get(sql);
			p.clearParameters();
			return p;
		}
		p = con.prepareStatement(sql,flags);
		statement_cache.put(sql, p);
		return p;
	}
}
