package com.angeldsis.lounative;

import java.util.Calendar;
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
	synchronized public void v(String TAG, String msg) {
		printNow();
		System.out.println("V / "+ TAG + ": "+msg);
	}
	@Override
	synchronized public void e(String TAG, String string) {
		printNow();
		System.out.println("E / "+ TAG + ": "+string);
	}
	@Override
	synchronized public void w(String TAG, String string) {
		printNow();
		System.out.println("W / "+ TAG + ": "+string);
	}
	@Override
	synchronized public void wtf(String tag, String string, Exception e) {
		printNow();
		System.out.println("WTF / "+tag+": "+string);
		e.printStackTrace();
	}
	@Override
	synchronized public void e(String TAG, String string, Exception e) {
		printNow();
		e(TAG,string);
		e.printStackTrace();
	}
	public void printNow() {
		Calendar c = Calendar.getInstance();
		System.out.print(String.format("%02d-%02d %02d:%02d:%02d.%03d ",c.get(Calendar.MONTH)+1,
				c.get(Calendar.DAY_OF_MONTH),
				c.get(Calendar.HOUR_OF_DAY),
				c.get(Calendar.MINUTE),
				c.get(Calendar.SECOND),
				c.get(Calendar.MILLISECOND)));
		System.out.print(String.format("%5d ",Thread.currentThread().getId()));
	}
}
