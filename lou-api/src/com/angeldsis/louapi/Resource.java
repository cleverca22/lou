package com.angeldsis.louapi;

import java.io.IOException;
import java.io.Serializable;

public class Resource implements Serializable {
	static final String[] names = {"wood","stone","iron","food"};
	private static final long serialVersionUID = 1L;
	double delta; // gain per step
	double base; // last value
	long step; // value of step at that time
	int max;
	LouState state;
	private String name;
	Resource(LouState state, int type) {
		this.state = state;
		step = state.getServerStep();
		name = names[type];
	}
	public void set(double d, double b, int m, int s) {
		int oldval = getCurrent();
		//Log.v("Resource","d:"+delta+" b:"+base+" m:"+max+" s:"+step);
		delta = d;
		base = b;
		max = m;
		step = s;
		//Log.v("Resource","d:"+delta+" b:"+base+" m:"+max+" s:"+step);
		int newval = getCurrent();
		if (oldval != newval) {
			if (oldval < newval) Log.v("Resource",name+" went up "+(newval - oldval));
			else Log.v("Resource",name+" went down "+(oldval - newval));
		}
	}
	public int getCurrent() {
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
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.writeDouble(base);
		out.writeLong(step);
		out.writeDouble(delta);
		out.writeInt(max);
	}
	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		base = in.readDouble();
		step = in.readLong();
		delta = in.readDouble();
		max = in.readInt();
	}
	public void fix(LouState louState, int i) {
		state = louState;
		name = names[i];
	}
}
