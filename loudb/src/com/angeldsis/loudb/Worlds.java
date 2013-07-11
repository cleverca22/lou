package com.angeldsis.loudb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.angeldsis.louapi.Log;

public class Worlds {
	private static final String TAG = "Worlds";
	static int[] pathToWorld;
	public static void init() {
		Log.v(TAG,"populating cache");
		pathToWorld = new int[300];
		Transaction t = new Transaction() {
			@Override void internalRun() throws SQLException {
				PreparedStatement s = link.con.prepareStatement("SELECT id,pathid FROM servers");
				ResultSet r = s.executeQuery();
				while (r.next()) {
					int id = r.getInt(1);
					int pathid = r.getInt(2);
					pathToWorld[pathid] = id;
				}
			}
		};
		t.run();
	}
}
