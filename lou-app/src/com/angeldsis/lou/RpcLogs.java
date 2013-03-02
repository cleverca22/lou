package com.angeldsis.lou;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RpcLogs extends SQLiteOpenHelper {
	private static final String DBName = "rpclogs.db";
	private static final int VERSION = 1;
	public RpcLogs(Context c) {
		super(c, DBName, null, VERSION);
	}
	@Override public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE logs (func,req,reply)");
	}
	
	@Override public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}
	public void logRequest(int req, int reply, String func) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues data = new ContentValues();
		data.put("func",func);
		data.put("req", req);
		data.put("reply",reply);
		db.insert("logs", null, data);
		db.close();
	}
}
