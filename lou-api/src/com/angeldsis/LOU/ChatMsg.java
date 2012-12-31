package com.angeldsis.LOU;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatMsg {
	public String s,c,m;
	public boolean hascrown;
	public ChatMsg(JSONObject C) throws JSONException {
		s = C.getString("s").substring(1);
		c = C.getString("c");
		m = C.getString("m");
		if (C.getString("s").substring(0, 1).equals("C")) hascrown = true;
		else hascrown = false;
	}
	public String toString() {
		if (c.equals("@A")) {
			return "[Alliance] ["+s+"] "+m;
		} else if (c.equals("privatein")) {
			return "[PM] ["+s+"] "+m;
		} else if (c.equals("privateout")) {
			return "[PM to "+s+"] "+m;
		}
		return s+" "+c+" "+m;
	}
}
