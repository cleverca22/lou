package com.angeldsis.louapi.data;

import org.json2.JSONException;
import org.json2.JSONObject;

import com.angeldsis.louapi.Player;

public class AllianceMember {
	public int online;
	public String lastLogin;
	public Player base;
	public AllianceMember(int id, String name2) {
		base = Player.get(id, name2);
	}
// {"f":false,"os":0,"ra":46,"t":10,"c":124,"r":39,"no":0,"p":763809,"n":"xHavoc","o":3,"l":"02/23/2013 21:19:42","i":4121}
	public void update(JSONObject m) throws JSONException {
		online = m.getInt("o");
		lastLogin = m.getString("l");
		base.points = m.getInt("p");
	}
}
