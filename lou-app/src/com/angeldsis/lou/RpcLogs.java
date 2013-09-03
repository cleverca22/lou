package com.angeldsis.lou;

import java.text.DateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RpcLogs extends SQLiteOpenHelper {
	private static final String DBName = "rpclogs.db";
	private static final int VERSION = 3;
	public RpcLogs(Context c) {
		super(c, DBName, null, VERSION);
	}
	@Override public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE pollLogs(func,reply_size)");
		db.execSQL("CREATE TABLE logs (func,req,reply,ts,networktime,parsetime,daymonth)");
	}
	@Override public void onUpgrade(SQLiteDatabase db, int old, int newver) {
		if ((old == 1) && (newver <= 3)) {
			db.execSQL("ALTER TABLE logs ADD ts");
			db.execSQL("ALTER TABLE logs ADD networktime");
			db.execSQL("ALTER TABLE logs ADD parsetime");
			db.execSQL("ALTER TABLE logs ADD daymonth");
		}
		if (newver <= 3) {
			db.execSQL("CREATE TABLE pollLogs(func,reply_size)");
		}
	}
	public void logRequest(int req, int reply, String func, int nettime, int parse1) {
		// FIXME, optimize this to reduce GC pressure
		SQLiteDatabase db = getWritableDatabase();
		ContentValues data = new ContentValues();
		data.put("ts",System.currentTimeMillis()/1000);
		data.put("func",func);
		data.put("req", req);
		data.put("reply",reply);
		data.put("networktime", nettime);
		data.put("parsetime", parse1);
		// FIXME, medium == May 12, 2013, use a more compact form, or derive it from ts when processing
		data.put("daymonth",DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date()));
		db.insert("logs", null, data);
		db.close();
	}
	public void logPollRequest(String c, int reply_size) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues data = new ContentValues();
		data.put("func", c);
		data.put("reply_size", reply_size);
		db.insert("pollLogs", null, data); // FIXME, write the insert myself?
		db.close();
	}
}
