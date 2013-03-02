package com.angeldsis.lounative;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

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

import com.angeldsis.louapi.Log;
import com.angeldsis.louapi.RPC.ReportCallback;
import com.angeldsis.louapi.Report;
import com.angeldsis.louapi.ReportDumper;

public class MainWindow extends Shell implements ReportCallback {
	static final private String TAG = "MainWindow";
	Button cityButton;
	Button btnReports;
	CoreSession session;
	private Button btnSaveAllReports;
	public MainWindow(Display display, final CoreSession coreSession) {
		session = coreSession;
		setText("main window");
		setLayout(new GridLayout(2, false));
		
		btnReports = new Button(this, SWT.NONE);
		btnReports.setText("Reports");
		Log.v(TAG,"made reports button");
		
		btnSaveAllReports = new Button(this, SWT.NONE);
		btnSaveAllReports.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ReportDumper d = new ReportDumper(session.rpc);
				try {
					d.dumpReports(new FileOutputStream(new File("out.txt")));
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnSaveAllReports.setText("Save All Reports");
		
		cityButton = new Button(this, SWT.NONE);
		cityButton.setText("CITY");
		new Label(this, SWT.NONE);
		
		Button btnOpensharedreport = new Button(this, SWT.NONE);
		btnOpensharedreport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				testShareReport();
			}
		});
		btnOpensharedreport.setText("OpenSharedReport");
		new Label(this, SWT.NONE);
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
		btnReports.redraw();
		Log.v(TAG,msg);
	}
	void testShareReport() {
		String sharestring = "ANDTCGLTCZC8LWAK";
		session.rpc.GetSharedReport(sharestring,this);
	}
	@Override
	public void done(Report report) {
		// TODO Auto-generated method stub
		
	}
}
