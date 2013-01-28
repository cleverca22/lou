package com.angeldsis.lou.chat;

import java.util.ArrayList;

import com.angeldsis.louapi.ChatMsg;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.SparseArray;

public class ChatHistory extends SQLiteOpenHelper {
	public static final String DBName = "chat_history_w%d_p%d.db";
	public static int VERSION = 3;
	private static final String tblFormat = "CREATE TABLE ChatLogs (time,channel,sender,crown,message,tag)";
	ArrayList<String> openTags = new ArrayList<String>();
	SparseArray<ChatMsg> cache;
	
	public ChatHistory(Context context,int world,int player) {
		super(context, String.format(DBName,world,player), null, VERSION);
		cache = new SparseArray<ChatMsg>();
	}
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(tblFormat);
	}
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if ((oldVersion == 1) && (newVersion <= 3)) {
			db.execSQL("ALTER TABLE ChatLogs ADD tag");
		}
		db.execSQL("CREATE INDEX IF NOT EXISTS tagindex ON ChatLogs(tag)");
		// onCreate(db);
	}
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//onUpgrade(db, oldVersion, newVersion);
	}
	public int getCount(String tag) {
		String[] args = { tag };
		Cursor c = this.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM ChatLogs WHERE tag = ?", args);
		c.moveToFirst();
		return c.getInt(0);
	}
	public void onChat(ArrayList<ChatMsg> d) {
		SQLiteDatabase db = getWritableDatabase();
		for (ChatMsg c : d) {
			ContentValues data = new ContentValues();
			data.put("time", System.currentTimeMillis());
			data.put("channel", c.channel);
			data.put("sender",c.sender);
			data.put("crown", c.hascrown);
			data.put("message",c.message);
			data.put("tag",c.tag);
			db.insert("ChatLogs", null, data);
			
			if (!openTags.contains(c.tag)) {
				openTags.add(c.tag);
			}
		}
	}
	public ChatMsg getItem(String tag,int position) {
		ChatMsg m = cache.get(position);
		if (m != null) return m;
		
		String[] args = {tag,""+position};
		Cursor c = getReadableDatabase().rawQuery(
				"SELECT time,channel,sender,crown,message,tag FROM ChatLogs WHERE tag = ? LIMIT ?,1",args);
		if (!c.moveToNext()) return null;
		m = new ChatMsg();
		m.ts = c.getLong(0);
		m.channel = c.getString(1);
		m.sender = c.getString(2);
		m.hascrown = c.getInt(3) == 1 ? true : false;
		m.message = c.getString(4);
		m.tag = c.getString(5);
		cache.put(position, m);
		return m;
	}
	public void teardown() {
		SQLiteDatabase db = getWritableDatabase();
		for (String tag : openTags) {
			Log.v("ChatHistory","closing tag "+tag);
			ContentValues data = new ContentValues();
			data.put("tag",tag);
			data.put("time", System.currentTimeMillis());
			data.put("sender", "System");
			data.put("message", "you have been disconnected");
			db.insert("ChatLogs",null,data);
		}
		db.close();
	}
}
