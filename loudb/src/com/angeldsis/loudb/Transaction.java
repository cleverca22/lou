package com.angeldsis.loudb;

import java.sql.SQLException;
import java.sql.Statement;

public abstract class Transaction implements Runnable {
	protected DbLink link = null;
	public void run() {
		Statement s;
		try {
			link = ConnectionPool.getLink();
			s = link.con.createStatement();
			link.con.setAutoCommit(true);
			s.execute("BEGIN");
			internalRun();
			s.execute("COMMIT");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				link.con.createStatement().executeUpdate("ROLLBACK");
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		if (link != null) ConnectionPool.restore(link);
		link = null;
	}
	abstract void internalRun() throws Exception;
}
