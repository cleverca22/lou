package com.angeldsis.louapi.data;

import org.json2.JSONObject;

import com.angeldsis.louapi.Alliance;
import com.angeldsis.louapi.Player;

public class PublicCityInfo {
	public Alliance alliance;
	public Player player;
	public int x,y;
	public String name;
	public PublicCityInfo(World w,JSONObject reply) {
		System.out.println("world:"+w);
		System.out.println("reply:"+reply);
		alliance = w.getAlliance(reply.optInt("a"), reply.optString("an"));
		name = reply.optString("n");
		player = w.getPlayer(reply.optInt("p"),reply.optString("pn"));
		player.alliance = alliance;
		int cityscore = reply.optInt("po");
		x = reply.optInt("x");
		y = reply.optInt("y");
	}
}
