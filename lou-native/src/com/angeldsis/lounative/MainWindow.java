package com.angeldsis.lounative;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;

public class MainWindow extends Shell {
	Button cityButton;
	Button btnReports;
	public MainWindow(Display display, final CoreSession coreSession) {
		setText("main window");
		setLayout(new GridLayout(1, false));
		
		btnReports = new Button(this, SWT.NONE);
		btnReports.setText("Reports");
		
		cityButton = new Button(this, SWT.NONE);
		cityButton.setText("CITY");
		cityButton.addListener(SWT.Activate , new Listener() {
			@Override
			public void handleEvent(Event event) {
				coreSession.openCity();
			}
		});
		open();
	}
	protected void checkSubclass() {}
	public void onReportCountUpdate(int viewed, int unviewed) {
		String msg = String.format("Reports (%d)",unviewed);
		btnReports.setText(msg);
		System.out.println(msg);
	}
}
