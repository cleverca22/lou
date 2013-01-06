package com.angeldsis.lounative;

import com.angeldsis.louapi.Log;
import com.angeldsis.louapi.Log.LogServer;

public class Logger implements LogServer {
	static Logger self = init();
	static Logger init() {
		if (self != null) return self;
		Logger l = new Logger();
		Log.init(l);
		return l;
	}
	@Override
	public void v(String TAG, String msg) {
		System.out.println("V / "+ TAG + ": "+msg);
	}
	@Override
	public void e(String TAG, String string) {
		System.out.println("E / "+ TAG + ": "+string);
	}
	@Override
	public void w(String TAG, String string) {
		System.out.println("W / "+ TAG + ": "+string);
	}
}
