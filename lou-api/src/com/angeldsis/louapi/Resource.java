package com.angeldsis.louapi;

public class Resource {
	double gain,lastValue;
	int max;
	long lastTime;
	public void set(double d, double b, int m) {
		gain = d;
		lastValue = b;
		max = m;
		lastTime = System.currentTimeMillis();
	}
	public String getCurrent() {
		long timepassed = (System.currentTimeMillis() - lastTime)/1000;
		double gained = timepassed * gain;
		return ""+(int)(lastValue+gained);
	}
	public String getRate() {
		double rate = gain * 3600;
		return ""+(int)rate;
	}
}
