package com.angeldsis.louapi;

import org.json2.JSONException;
import org.json2.JSONObject;

public class MailHeader {
	public String subject;
	public MailHeader(JSONObject o) throws JSONException {
		// {"f":"SanjiNami","d":1372391806283,"t":["chi925","kashikoi"],"r":true,"mt":0,"fi":637,"ci":[],"ti":[1487,2721],"cc":[],"i":220990}
		subject = o.getString("s");
	}
}
