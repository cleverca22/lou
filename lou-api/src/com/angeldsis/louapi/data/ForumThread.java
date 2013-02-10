package com.angeldsis.louapi.data;

import org.json2.JSONException;
import org.json2.JSONObject;

public class ForumThread {
	public boolean hup;
	public int threadID,ai;
	public String tt,an;
	int pc;
	
	public ForumThread(JSONObject o) throws JSONException {
		hup = o.getBoolean("hup");
		ai = o.getInt("ai");
		threadID = o.getInt("ti");
		tt = o.getString("tt");
		an = o.getString("an");
		pc = o.getInt("pc");
		// {"hup":true,"ai":1199,"tt":"Marketplace Calculator","pc":1,"lp":{"pn":"Vishalicious","pli":1199,"t":1.360038310931093E12,"m":"","pi":51832,"up":false},"an":"Vishalicious","ti":10880,"fp":{"pn":"","pli":0,"t":-62135596800000,"m":"","pi":0,"up":false}}
		// {"hup":true,"ai":957,"tt":"SKS Script * READ *","pc":4,"lp":{"pn":"Kaliyo","pli":115,"t":1.3600067211778052E12,"m":"","pi":51579,"up":false},"an":"ProjextX","ti":1736,"fp":{"pn":"ProjextX","pli":957,"t":1353627452597,"m":"","pi":5121,"up":false}}
	}

}
