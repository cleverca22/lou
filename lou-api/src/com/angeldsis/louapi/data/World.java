package com.angeldsis.louapi.data;

import java.util.HashMap;

import com.angeldsis.louapi.Alliance;
import com.angeldsis.louapi.Player;

/** manages shared objects within a world, like alliance
 * and player caches
 */
public class World {
	// data that should be shared between connections
	HashMap<Integer,Alliance> alliances = new HashMap<Integer,Alliance>();
	HashMap<Integer,Player> players = new HashMap<Integer,Player>();
	// private data for just this connection, and this world
	public int Id;
	public String Name;
	public World() {
		Name = "UNK3";
	}
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
