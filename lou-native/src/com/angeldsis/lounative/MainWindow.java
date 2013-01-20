package com.angeldsis.lounative;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class MainWindow extends Shell {
	Button cityButton;
	Button btnReports;
	CoreSession session;
	public MainWindow(Display display, final CoreSession coreSession) {
		session = coreSession;
		setText("main window");
		setLayout(new GridLayout(1, false));
		
		btnReports = new Button(this, SWT.NONE);
		btnReports.setText("Reports");
		
		cityButton = new Button(this, SWT.NONE);
		cityButton.setText("CITY");
		
		Button btnOpensharedreport = new Button(this, SWT.NONE);
		btnOpensharedreport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				testShareReport();
			}
		});
		btnOpensharedreport.setText("OpenSharedReport");
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
	void testShareReport() {
		String sharestring = "ANDTCGLTCZC8LWAK";
		session.rpc.GetSharedReport(sharestring);
	}
}
