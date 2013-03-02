package com.angeldsis.louapi.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Coord {
	static Pattern p = Pattern.compile("^(\\d+):(\\d+)$");
	public int x,y;
	public Coord(int x, int y) {
		this.x = x;
		this.y = y;
	}
	public String format() {
		return String.format("%d:%d", x,y);
	}
	public String getContinent() {
		return String.format("C%d%d",y/100,x/100);
	}
	public static int toCityId(int x, int y) {
		return x | (y << 0x10);
	}
	public int toCityId() {
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
	public static Coord fromCityId(int in) {
		int x = in & 0xffff;
		int y = in >> 0x10;
		return new Coord(x,y);
	}
	public static int getX(long in) {
		return (int) (in & 0xffff);
	}
	public static int getY(long in) {
		return (int) (in >> 0x10);
	}
	public double distance(Coord in) {
		int x = this.x - in.x;
		int y = this.y - in.y;
		return Math.sqrt( (x*x) + (y*y) );
	}
}
