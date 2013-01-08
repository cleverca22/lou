package com.angeldsis.lou;

import com.angeldsis.louapi.Log;
import com.angeldsis.louapi.Log.LogServer;

public class Logger implements LogServer {
	static Logger self;
	static void init() {
		self = new Logger();
		Log.init(self);
	}
	@Override
	public void v(String TAG, String msg) {
		android.util.Log.v(TAG,msg);
	}
	
	@Override
	public void e(String TAG, String msg) {
		android.util.Log.v(TAG,msg);
	}
	@Override
	public void w(String TAG, String string) {
		android.util.Log.w(TAG,string);
	}
}
