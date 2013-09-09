package com.angeldsis.louapi;

public class Log {
	private static LogServer self;
	public static void e(String TAG, String string) {
		self.e(TAG,string);
	}
	public static void v(String TAG, String string) {
		self.v(TAG, string);
	}
	public interface LogServer {
		public void v(String TAG,String msg);
		public void e(String tAG, String string);
		public void w(String tAG, String string);
		public void wtf(String tag, String string, Exception e);
		public void e(String TAG, String string, Exception e);
	}
	public static void init(LogServer logger) {
		self = logger;
	}
	public static void w(String TAG, String string) {
		self.w(TAG,string);
	}
	public static void wtf(String tag, String string, Exception e) {
		self.wtf(tag,string,e);
	}
	public static void e(String TAG, String string, Exception e) {
		self.v("Logger","exception is "+e);
		self.e(TAG, string,e);
	}
}
