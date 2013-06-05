package com.angeldsis.lounative;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import com.angeldsis.louapi.ChatMsg;
import com.angeldsis.louapi.Log;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class ChatWindow extends Shell implements SelectionListener {
	static final String TAG = "ChatWindow";
	private Text msg_input;
	RPCWrap rpc;
	private TabFolder tabFolder;
	private class TabData {
		public Text history;
		public TabItem tab;
		private int count = 0;
		String title;
		public String prefix;
		public TabData(String titleIn, String tag, String prefixIn) {
			tab = new TabItem(tabFolder, SWT.NONE);
			tab.setText(titleIn);
			Group group = new Group(tabFolder, SWT.NONE);
			group.setText(titleIn);
			tab.setControl(group);
			group.setLayout(new GridLayout(1, true));
			history = new Text(group, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
			history.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			
			title = titleIn;
			prefix = prefixIn;
			
			tab.setData(this);
		}
		public void handle_msg(ChatMsg c) {
			String msg = history.getText() + "\r\n" + c.toString();
			history.setText(msg);
			history.setSelection(msg.length());
			
			TabItem[] active = tabFolder.getSelection();
			if (active[0] == tab) selected();
			else {
				count++;
				tab.setText(title+"("+count+")");
			}
		}
		public void selected() {
			count = 0;
			tab.setText(title);
		}
	}
	private TabData continent,alliance,officer,PM;
	public ChatWindow(Display display,RPCWrap rpc) {
		this.rpc = rpc;
		setLayout(new GridLayout(1, true));
		setText("Chat");
		
		tabFolder = new TabFolder(this, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tabFolder.addSelectionListener(this);
		
		continent = new TabData("Continent","@C","");
		alliance = new TabData("Alliance","@A","/a ");
		officer = new TabData("Officer","@O","/o ");
		PM = new TabData("PM(doesnt work)","PM","");
		
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
		Log.v(TAG,c.toString());
		if (c.tag.equals("@A")) alliance.handle_msg(c);
		else if (c.tag.equals("@O")) officer.handle_msg(c);
		else { // @C and all others
			continent.handle_msg(c);
		}
		forceActive();
	}
	public void sendMessage(MouseEvent e) {
		TabItem[] items = tabFolder.getSelection();
		TabData data = (TabData) items[0].getData();
		rpc.QueueChat(data.prefix + msg_input.getText() + "\n");
		msg_input.setText("");
	}
	@Override public void widgetSelected(SelectionEvent e) {
		TabData data = (TabData) e.item.getData();
		data.selected();
	}
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub
		
	}
}
