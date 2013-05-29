package com.angeldsis.louapi;

public class Artifact {
	String name;
	int title,itemid;
	public Artifact(String string, int i, int j) {
		name = string;
		title = i;
		itemid = j;
	}
	public static Artifact a208 = new Artifact("Valorite Wheel",10,208);
	public static Artifact a209 = new Artifact("Verite Wheel",9,209);
	public static Artifact a210 = new Artifact("Platinum Wheel",8,210);
	public static Artifact a211 = new Artifact("Golden Wheel",7,211);
	public static Artifact a212 = new Artifact("Silver Wheel",6,212);
	public static Artifact a213 = new Artifact("Steel Wheel",5,213);
	public static Artifact a214 = new Artifact("Bronze Wheel",3,214);
}
