package com.angeldsis.louapi.data;

import org.json2.JSONObject;

public class AlliancePollRow {
	public String name;
	public int id,points;
	public AlliancePollRow(JSONObject m) {
		/* 0: {"f":false,"os":0,"ra":999,"t":7,"c":9,"r":2658,"no":0,"p":51551,"n":"DoctorBug","o":0,"l":"01/23/2013 19:09:15","i":19210}
0: {"f":false,"os":0,"ra":1647,"t":6,"c":6,"r":2657,"no":0,"p":20612,"n":"Altaair","o":3,"l":"01/23/2013 19:44:15","i":19566}
0: {"f":true,"os":2,"ra":1840,"t":6,"c":4,"r":2657,"no":0,"p":15358,"n":"Skadi1","o":0,"l":"01/16/2013 16:51:50","i":19567}
0: {"f":false,"os":0,"ra":1661,"t":6,"c":6,"r":2658,"no":0,"p":20289,"n":"Project909","o":0,"l":"01/23/2013 02:10:58","i":19729}
0: {"f":false,"os":1,"ra":3904,"t":5,"c":1,"r":2658,"no":0,"p":1037,"n":"Richardchees","o":0,"l":"01/20/2013 01:49:01","i":21621}
*/
		name = m.optString("n");
		id = m.optInt("i");
		points = m.optInt("p");
	}
}
