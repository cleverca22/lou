package com.angeldsis.louapi;

import com.google.gson.annotations.SerializedName;

public class Resource {
	static final String[] names = {"wood","stone","iron","food"};
	@SerializedName("d") public double delta; // gain per step
	@SerializedName("b") double base; // last value
	@SerializedName("s") long step; // value of step at that time
	@SerializedName("m") int max;
	@SerializedName("n") private String name;
	Resource(LouState state, int type) {
		step = state.getServerStep();
		name = names[type];
	}
	public void set(double d, double b, int m, int s, LouState state) {
		//int oldval = getCurrent(state);
		//Log.v("Resource","d:"+delta+" b:"+base+" m:"+max+" s:"+step);
		delta = d;
		base = b;
		max = m;
		step = s;
		//Log.v("Resource","d:"+delta+" b:"+base+" m:"+max+" s:"+step);
		//int newval = getCurrent(state);
		//if (oldval != newval) {
		//	if (oldval < newval) Log.v("Resource",name+" went up "+(newval - oldval));
		//	else Log.v("Resource",name+" went down "+(oldval - newval));
		//}
	}
	public int getMax() {
		return max;
	}
	public void fix(int i) {
		name = names[i];
	}
}
