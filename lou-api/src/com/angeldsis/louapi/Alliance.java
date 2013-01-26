package com.angeldsis.louapi;

public class Alliance {
	public String name;
	public int id;
	public Alliance(int id, String name) {
		this.id = id;
		this.name = name;
	}
	public static Alliance get(int id, String name) {
		Log.v("Alliance",String.format("Alliance.get(%d, %s)",id,name));
		// FIXME, reuse objects
		return new Alliance(id,name);
	}
}
