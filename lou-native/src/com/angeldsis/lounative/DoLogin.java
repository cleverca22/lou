package com.angeldsis.lounative;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.angeldsis.louapi.LouSession;
import com.angeldsis.louapi.LouSession.result;
import org.eclipse.swt.layout.GridData;

public class DoLogin extends Shell implements MouseListener {
	Text user,pass;
	Button btnSavePw;
	LouSession session;
	Display display;
	DoLogin(Display display, LouSession session) throws Exception {
		super(display);
		this.session = session;
		this.display = display;
		setSize(473, 140);
		setLayout(new GridLayout(2, false));
		Label label = new Label(this, 0);
		label.setText("username");
		user = new Text(this, SWT.BORDER);
		user.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		Label label_1 = new Label(this,0);
		label_1.setText("password");
		pass = new Text(this,SWT.BORDER | SWT.PASSWORD);
		pass.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		btnSavePw = new Button(this, SWT.CHECK);
		btnSavePw.setText("Save PW");

		Config config = Config.getConfig();
		if (!config.getPassword().equals("")) {
			user.setText(config.getUsername());
			pass.setText(config.getPassword());
			btnSavePw.setSelection(true);
		}
		
		Button button = new Button(this,0);
		button.addMouseListener(this);
		button.setText("login!");
		this.setText("login screen");
		this.open();
	}
	private void eventloop() {
		while (!this.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
	}
	@Override
	public void mouseDoubleClick(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseDown(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseUp(MouseEvent e) {
		String username = user.getText();
		String password = pass.getText();
		Config config = Config.getConfig();
		if (btnSavePw.getSelection()) {
			config.setUsername(username);
			config.setPassword(password);
		} else {
			config.clearCredentials();
		}
		System.out.println("starting login");
		result reply = session.startLogin(username,password);
		if (reply.error) {
			System.out.println(reply.errmsg);
			reply.e.printStackTrace();
		}
		if (reply.worked) {
			config.setRememberMe(session.save_cookie());
			config.flush();
			System.out.println("worked");
			close();
			dispose();
		}
		else {
			config.flush();
			return;
		}
	}
	protected void checkSubclass() {
	}
	public static boolean login(Display display, LouSession session2) throws Exception {
		DoLogin self = new DoLogin(display,session2);
		self.eventloop();
		if (self.session.servers == null) return false;
		else return true;
	}
}
