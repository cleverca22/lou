package com.angeldsis.louapi;

public interface HttpUtil {
	void restore_cookie(String cookie);
	String getCookieData();
	void dumpCookies();
	void logout();
}
