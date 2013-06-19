package com.angeldsis.louapi;

public class ManaCounter extends Counter {
	int max;
	public ManaCounter(LouState state) {
		super(state);
		max = 0;
		step = 0;
	}
	public ManaCounter(LouState state, double base2, double delta,int max, int step) {
		super(state, base2, delta);
		this.max = max;
		this.step = step;
	}
	public int getCurrent() {
		if (delta == 0) return 0;
		long dV = state.getServerStep();
		if (dV == 0) return 0;
		long dW = dV - step;
		double now = dW * delta + base;
		if (now > max) return max;
		return (int) now;
	}
	public void update(double base, double delta, int max2, int step) {
		max = max2;
		super.update(base,delta,step);
	}
}
