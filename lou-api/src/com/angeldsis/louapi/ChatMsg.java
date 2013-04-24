package com.angeldsis.louapi;

import org.json2.JSONException;
import org.json2.JSONObject;

public class ChatMsg {
	public String sender,channel,message,tag;
	public boolean hascrown;
	public long ts;
	public ChatMsg(JSONObject C) throws JSONException {
		sender = C.getString("s").substring(1);
		channel = C.getString("c");
		message = C.getString("m");
		if (C.getString("s").substring(0, 1).equals("C")) hascrown = true;
		else hascrown = false;
		if (channel.equals("@A")) tag = "@A";
		else if (channel.equals("@O")) tag = "@O";
		else if (channel.equals("privatein") || channel.equals("privateout")) tag = "pm_"+sender;
		else tag = "@C";
	}
	public ChatMsg() {
	}
	public String toString() {
		if (channel == null) {
			return "NULL ["+sender+"] "+message;
		} else if (channel.equals("@A")) {
			return "[Alliance] ["+sender+"] "+message;
		} else if (channel.equals("@O")) {
			return "[Officer] ["+sender+"] "+message;
		} else if (channel.equals("privatein")) {
			return "[PM] ["+sender+"] "+message;
		} else if (channel.equals("privateout")) {
			return "[PM to "+sender+"] "+message;
		}
		return sender+" "+channel+" "+message;
	}
}
