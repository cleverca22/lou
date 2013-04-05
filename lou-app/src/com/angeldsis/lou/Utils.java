package com.angeldsis.lou;

public class Utils {
	public static String NumberFormat(int input) {
		return coolFormat(input,0);
	}
	// http://stackoverflow.com/questions/4753251/how-to-go-about-formatting-1200-to-1-2k-in-java
	private static char[] c = new char[]{'k', 'm', 'b', 't'};
	private static String coolFormat(double n, int iteration) {
		double d = ((long) n / 100) / 10.0;
		boolean isRound = (d * 10) % 10 == 0;// true if the decimal part is equal to 0 (then it's trimmed anyway)
		return (d < 1000 ? // this determines the class, i.e. 'k', 'm' etc
		((d > 99.9 || isRound || (!isRound && d > 9.99) ? // this decides whether to trim the decimals
		(int) d * 10 / 10 : d + "" // (int) d * 10 / 10 drops the decimal
		) + "" + c[iteration])
				: coolFormat(d, iteration + 1));
		
	}
}
