package com.angeldsis.louapi;

public class Alliance {
	private String name;
	private int id;
	public Alliance(int id, String name) {
		this.id = id;
		this.name = name;
	}
	public static Alliance get(int id, String name) {
		// FIXME, reuse objects
		return new Alliance(id,name);
	}
}