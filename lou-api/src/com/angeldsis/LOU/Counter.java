package com.angeldsis.LOU;

public class Counter {
	static Counter ZERO = new Counter(0,0);
	double base;
	double delta;
	long lastTime;
	public Counter(double base2, double delta) {
		this.base = base2;
		this.delta = delta;
		lastTime = System.currentTimeMillis();
	}
	public String getCurrent() {
		long timepassed = (System.currentTimeMillis() - lastTime)/1000;
		double gained = timepassed * delta;
		return ""+(int)(base+gained);
	}
}
