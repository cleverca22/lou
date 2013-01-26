package com.angeldsis.lou.chat;

import java.util.ArrayList;

import com.angeldsis.louapi.ChatMsg;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ChatHistory extends SQLiteOpenHelper {
	public static final String DBName = "chat_history_w%d_p%d.db";
	public static int VERSION = 2;
	private static final String tblFormat = "CREATE TABLE ChatLogs (time,channel,sender,crown,message,tag)";
	ArrayList<String> openTags = new ArrayList<String>();
	
	public ChatHistory(Context context,int world,int player) {
		super(context, String.format(DBName,world,player), null, VERSION);
	}
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(tblFormat);
	}
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if ((oldVersion == 1) && (newVersion == 2)) {
			db.execSQL("ALTER TABLE ChatLogs ADD tag");
		}
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
		String[] args = {tag,""+position};
		Cursor c = getReadableDatabase().rawQuery(
				"SELECT time,channel,sender,crown,message,tag FROM ChatLogs WHERE tag = ? LIMIT ?,1",args);
		c.moveToFirst();
		ChatMsg m = new ChatMsg();
		m.ts = c.getLong(0);
		m.channel = c.getString(1);
		m.sender = c.getString(2);
		m.hascrown = c.getInt(3) == 1 ? true : false;
		Log.v("ChatHistory","crown is "+c.getInt(3));
		m.message = c.getString(4);
		m.tag = c.getString(5);
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
