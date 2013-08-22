package com.angeldsis.loudb;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.json2.JSONException;
import org.json2.JSONObject;

import com.angeldsis.louapi.Log;

public class ErrorMsg extends Transaction {
	String stack;
	String uri;
	String msg;
	String type;
	String version,versions;
	int line;
	public ErrorMsg(JSONObject e, JSONObject versions) throws JSONException {
		msg = e.getString("msg");
		uri = e.optString("uri");
		line = e.optInt("line");
		stack = e.optString("stack");
		type = e.getString("type");
		if (versions != null) {
			String filename = uri.replace("dsislou://", "");
			version = versions.optString(filename);
			
			this.versions = versions.toString();
		}
		Log.v("ErrorMsg",msg);
	}
	@Override
	void internalRun() throws SQLException {
		PreparedStatement s = link.con.prepareStatement("INSERT INTO errors (type,msg,uri,line,stack,version,versions) VALUES (?,?,?,?,?,?,?)");
		s.setString(1, type);
		s.setString(2, msg);
		s.setString(3, uri);
		s.setInt(4, line);
		s.setString(5, stack);
		s.setString(6, version);
		s.setString(7, versions);
		s.execute();
	}
}
