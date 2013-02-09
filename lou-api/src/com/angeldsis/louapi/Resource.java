package com.angeldsis.louapi;

import com.google.gson.annotations.SerializedName;

public class Resource {
	static final String[] names = {"wood","stone","iron","food"};
	@SerializedName("d") double delta; // gain per step
	@SerializedName("b") double base; // last value
	@SerializedName("s") long step; // value of step at that time
	@SerializedName("m") int max;
	@SerializedName("n") private String name;
	Resource(LouState state, int type) {
		step = state.getServerStep();
		name = names[type];
	}
	public void set(double d, double b, int m, int s, LouState state) {
		int oldval = getCurrent(state);
		//Log.v("Resource","d:"+delta+" b:"+base+" m:"+max+" s:"+step);
		delta = d;
		base = b;
		max = m;
		step = s;
		//Log.v("Resource","d:"+delta+" b:"+base+" m:"+max+" s:"+step);
		int newval = getCurrent(state);
		if (oldval != newval) {
			if (oldval < newval) Log.v("Resource",name+" went up "+(newval - oldval));
			else Log.v("Resource",name+" went down "+(oldval - newval));
		}
	}
	public int getCurrent(LouState state) {
		int stepsPassed = (int) (state.getServerStep() - step);
		double newVal = stepsPassed * delta + base;
		if (newVal > max) return max;
		return (int) newVal;
	}
	public String getRate() {
		// FIXME
		double rate = delta * 3600;
		return ""+(int)rate;
	}
	public int getMax() {
		return max;
	}
	public void fix(int i) {
		name = names[i];
	}
}
