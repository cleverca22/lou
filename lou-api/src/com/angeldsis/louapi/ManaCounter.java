package com.angeldsis.louapi;

public class ManaCounter extends Counter {
	int max;
	static ManaCounter ZERO = new ManaCounter(0,0,0);
	public ManaCounter(double base2, double delta,int max) {
		super(base2, delta);
		this.max = max;
	}
}
