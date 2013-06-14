package com.angeldsis.lou.chat;

import java.util.ArrayList;
import java.util.HashMap;

import com.angeldsis.louapi.ChatMsg;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.util.LruCache;
import android.util.Log;

public class ChatHistory extends SQLiteOpenHelper {
	public static final String DBName = "chat_history_w%d_p%d.db";
	public static int VERSION = 3;
	private static final String tblFormat = "CREATE TABLE ChatLogs (time,channel,sender,crown,message,tag)";
	public ArrayList<String> openTags = new ArrayList<String>();
	HashMap<String,ChatCache> caches = new HashMap<String,ChatCache>();
	//SparseArray<ChatMsg> cache;
	
	public ChatHistory(Context context,int world,int player) {
		super(context, String.format(DBName,world,player), null, VERSION);
		//cache = new SparseArray<ChatMsg>();
	}
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(tblFormat);
		db.execSQL("CREATE INDEX IF NOT EXISTS tagindex ON ChatLogs(tag)");
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
		// FIXME, store into caches
	}
	public ChatMsg getItem(String tag,int position) {
		ChatCache cache;
		synchronized (caches) {
			cache = caches.get(tag);
			if (cache == null) {
				cache = new ChatCache(tag);
				caches.put(tag, cache);
			}
		}
		return cache.get(position);
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
	private class ChatCache extends LruCache<Integer,ChatMsg> {
		private static final String TAG = "ChatCache";
		String tag;
		public ChatCache(String tag) {
			super(2000);
			this.tag = tag;
		}
		protected ChatMsg create(Integer position) {
			// FIXME, this always grabs a block of 20, even if 90% of them are already in the cache
			// FIXME, pre-fill the cache as events roll in, dont save to disk, then read back
			int origpos = position;
			//Log.v(TAG,"having to fetch chat item "+position);
			position -= position % 50;
			int start = position;
			//Log.v(TAG,"changed it to "+position);
			ChatMsg m = null,wanted = null;
			String[] args = {tag,""+position};
			Cursor c = getReadableDatabase().rawQuery(
					"SELECT time,channel,sender,crown,message,tag FROM ChatLogs WHERE tag = ? LIMIT ?,50",args);
			while (c.moveToNext()) {
				m = new ChatMsg();
				m.ts = c.getLong(0);
				m.channel = c.getString(1);
				m.sender = c.getString(2);
				m.hascrown = c.getInt(3) == 1 ? true : false;
				m.message = c.getString(4);
				m.tag = c.getString(5);
				if (position != origpos) this.put(position, m);
				else wanted = m;
				position++;
			}
			//Log.v(TAG,"pre-fetched "+(position - start)+" extra?");
			return wanted;
		}
	}
}
