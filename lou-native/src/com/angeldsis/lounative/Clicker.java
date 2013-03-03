package com.angeldsis.lounative;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

public abstract class Clicker implements SelectionListener {
	@Override public void widgetSelected(SelectionEvent e) {
		clicked();
	}
	@Override public void widgetDefaultSelected(SelectionEvent e) {
		clicked();
	}
	public abstract void clicked();
}
