package com.angeldsis.louapi.data;

import java.util.HashMap;

import com.angeldsis.louapi.Alliance;
import com.angeldsis.louapi.Player;

public class World {
	HashMap<Integer,Alliance> alliances = new HashMap<Integer,Alliance>();
	HashMap<Integer,Player> players = new HashMap<Integer,Player>();
	public static World get(int worldid) {
		// FIXME, reuse them within a world
		return new World();
	}
	public Alliance getAlliance(int id, String name) {
		if (alliances.containsKey(id)) {
			return alliances.get(id);
		}
		Alliance a = new Alliance(id,name);
		alliances.put(id, a);
		return a;
	}
	public Player getPlayer(int id, String name) {
		if (players.containsKey(id)) return players.get(id);
		Player p = new Player(id,name);
		players.put(id, p);
		return p;
	}
}