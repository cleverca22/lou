package com.angeldsis.lounative;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.json.JSONException;
import org.json.JSONObject;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;

public class ChatWindow extends Shell implements MouseListener {
	private Text text;
	private Text text_1;
	RPCWrap rpc;
	public ChatWindow(Display display,RPCWrap rpc) {
		this.rpc = rpc;
		setLayout(new GridLayout(2, false));
		
		text = new Text(this, SWT.BORDER | SWT.MULTI);
		GridData gd_text = new GridData(SWT.LEFT, SWT.FILL, true, true, 2, 1);
		gd_text.widthHint = 431;
		text.setLayoutData(gd_text);
		
		text_1 = new Text(this, SWT.BORDER);
		text_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button btnNewButton = new Button(this, SWT.NONE);
		btnNewButton.setText("Send");
		btnNewButton.addMouseListener(this);
		open();
	}
	protected void checkSubclass() {}
	public void handle_msg(JSONObject d) throws JSONException {
		System.out.println(d.toString(1));
		String s = d.getString("s");
		String c = d.getString("c");
		String m = d.getString("m");
		String formated = s+" "+c+" "+m;
		text.setText(text.getText() + "\r\n" + formated);
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
		rpc.QueueChat(text_1.getText() + "\n");
		text_1.setText("");
	}
}
