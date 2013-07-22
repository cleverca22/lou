package com.angeldsis.lounative;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

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
	private Button btnIdleTroops;
	private Button btnMail;
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
		cityButton.setEnabled(false);
		cityButton.setText("CITY");
		
		btnIdleTroops = new Button(this, SWT.NONE);
		btnIdleTroops.setText("Idle Troops");
		btnIdleTroops.addSelectionListener(new Clicker(){
			@Override public void clicked() {
				coreSession.openIdleTroops();
			}});
		Button btnOpensharedreport = new Button(this, SWT.NONE);
		btnOpensharedreport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				testShareReport();
			}
		});
		btnOpensharedreport.setText("OpenSharedReport");
		
		btnMail = new Button(this, SWT.NONE);
		btnMail.setText("Mail");
		btnMail.addSelectionListener(new Clicker() {
			@Override
			public void clicked() {
				coreSession.openMail();
			}
		});
		cityButton.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				Log.v(TAG,"opening city");
				coreSession.openCity();
			}
			@Override public void widgetDefaultSelected(SelectionEvent e) {
			}});
		open();
	}
	protected void checkSubclass() {}
	public void onReportCountUpdate(int viewed, int unviewed) {
		String msg = String.format("Reports (%d)",unviewed);
		btnReports.setText(msg);
		this.layout();
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
