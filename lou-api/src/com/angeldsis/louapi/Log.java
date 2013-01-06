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
	}
	public static void init(LogServer logger) {
		self = logger;
	}
	public static void w(String TAG, String string) {
		self.w(TAG,string);
	}
}
