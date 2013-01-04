package com.angeldsis.louapi;

public class Player {
	private String name;
	private int id;

	public Player(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public static Player get(int id, String name) {
		// FIXME, re-use the old object
		return new Player(id,name);
	}
}
