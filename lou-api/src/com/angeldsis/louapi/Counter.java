package com.angeldsis.louapi;

public class Counter {
	double base;
	double delta;
	long lastTime;
	int step;
	LouState state;
	public Counter(LouState state,double base2, double delta) {
		this.base = base2;
		this.delta = delta;
		this.state = state;
		lastTime = System.currentTimeMillis();
	}
	public Counter(LouState state) {
		this.state = state;
		base = 0;
		delta = 0;
		lastTime = 0;
	}
	public long getCurrent() {
		long dT = state.getServerStep();
		if (dT == 0) return 0;
		long dU = dT - step;
		return (long) (dU * delta + base);
	}
	public void update(double base2, double delta2, int step) {
		//String oldval = getCurrent();
		base = base2;
		delta = delta2;
		this.step = step;
		//String newval = getCurrent();
		//Log.v("Counter",oldval);
		//Log.v("Counter",newval);
	}
}
