package com.angeldsis.louapi.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.angeldsis.louapi.Log;

public class Coord {
	static Pattern p = Pattern.compile("^(\\d+):(\\d+)$");
	public static String format(int x,int y) {
		return String.format("%d:%d", x,y);
	}
	public static int toCityId(int x, int y) {
		return x | (y << 0x10);
	}
	public static int fromString(String in) {
		Matcher m = p.matcher(in);
		if (!m.find()) return 0;
		String a = m.group(1);
		String b = m.group(2);
		int x = Integer.parseInt(a);
		int y = Integer.parseInt(b);
		return toCityId(x,y);
	}
	public static String fromCityId(int in) {
		int x = in & 0xffff;
		int y = in >> 0x10;
		return format(x,y);
	}
	public static int getX(long in) {
		return (int) (in & 0xffff);
	}
	public static int getY(long in) {
		return (int) (in >> 0x10);
	}
}
