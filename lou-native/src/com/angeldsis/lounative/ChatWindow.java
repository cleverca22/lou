package com.angeldsis.lounative;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import com.angeldsis.louapi.ChatMsg;

public class ChatWindow extends Shell {
	static final String TAG = "ChatWindow";
	private Text text;
	private Text msg_input;
	RPCWrap rpc;
	public ChatWindow(Display display,RPCWrap rpc) {
		this.rpc = rpc;
		setLayout(new GridLayout(1, false));
		setText("Chat");
		
		text = new Text(this, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		msg_input = new Text(this, SWT.BORDER);
		msg_input.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		msg_input.addListener(SWT.KeyUp, new Listener() {
			public void handleEvent(Event e) {
				if (e.keyCode == 13) {
					sendMessage(null);
				}
			}
		});
		open();
	}
	protected void checkSubclass() {}
	public void handle_msg(ChatMsg c) {
		text.setText(text.getText() + "\r\n" + c.toString());
		forceActive();
	}
	public void sendMessage(MouseEvent e) {
		rpc.QueueChat(msg_input.getText() + "\n");
		msg_input.setText("");
	}
}
