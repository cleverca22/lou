package com.angeldsis.lounative;

import java.util.HashMap;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;

import com.angeldsis.louapi.Log;
import com.angeldsis.louapi.MailBoxFolder;
import com.angeldsis.louapi.MailHeader;
import com.angeldsis.louapi.RPC;
import com.angeldsis.louapi.RPC.GotMailMessage;
import com.angeldsis.louapi.RPC.MailBoxCallback;
import com.angeldsis.louapi.RPC.MessageCountCallback;
import com.angeldsis.louapi.RPC.MessageHeaderCallback;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.layout.FillLayout;

public class MailWindow extends Shell implements MailBoxCallback, MessageCountCallback {
	private static final String TAG = "MailWindow";
	Composite messages;
	RPC rpc;
	private MailBoxFolder inbox,outbox;
	int reqcount = 0;
	MailHeader[] rows;
	private ScrolledComposite scrolledComposite;
	private MailBoxFolder currentFolder;
	private Compose compose;
	private TabFolder tabFolder;
	private Composite composite;
	HashMap<String,TabData> tabs = new HashMap<String,TabData>();
	private Composite left;
	public MailWindow(Display display, RPCWrap rpc) {
		setSize(561, 300);
		this.rpc = rpc;
		rpc.IGMGetFolders(this);
		setLayout(new GridLayout(2, true));
		setText("Mail");
		
		// left pane
		left = new Composite(this,SWT.NONE);
		left.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		left.setLayout(new GridLayout(1,false));

		composite = new Composite(left, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		
		Button btnInbox = new Button(composite, SWT.RADIO);
		btnInbox.setSelection(true);
		btnInbox.setText("Inbox");
		
		Button btnOutbox = new Button(composite, SWT.RADIO);
		btnOutbox.setText("Outbox");
		
		scrolledComposite = new ScrolledComposite(left, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setMinHeight(50);
		
		messages = new Composite(scrolledComposite,SWT.NONE);
		scrolledComposite.setContent(messages);
		messages.setLayout(new GridLayout(3,false));
		
		// right pane
		tabFolder = new TabFolder(this, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		compose = new Compose();
		open();
	}
	private class TabData {
		public TabItem tab;
		TabData(String key) {
			tabs.put(key,this);
		}
	};
	private class Compose extends TabData {
		private Text message;
		private Text to;
		private Text cc;
		private Text subject;
		Compose() {
			super("compose");
			// start
			tab = new TabItem(tabFolder,SWT.NONE);
			tab.setText("Compose");
			
			Composite composite_1 = new Composite(tabFolder, SWT.NONE);
			tab.setControl(composite_1);
			composite_1.setLayout(new GridLayout(1, false));
			
			Composite composite_2 = new Composite(composite_1, SWT.NONE);
			composite_2.setLayout(new GridLayout(2, false));
			composite_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			Composite composite_4 = new Composite(composite_2, SWT.NONE);
			composite_4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			composite_4.setBounds(0, 0, 64, 64);
			composite_4.setLayout(new GridLayout(2, false));
			
			Button btnTo = new Button(composite_4, SWT.NONE);
			btnTo.setBounds(0, 0, 99, 30);
			btnTo.setText("To");
			
			to = new Text(composite_4, SWT.BORDER);
			to.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			to.setText("text 3");
			
			Button btnCc = new Button(composite_4, SWT.NONE);
			btnCc.setBounds(0, 0, 99, 30);
			btnCc.setText("CC");
			
			cc = new Text(composite_4, SWT.BORDER);
			cc.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			Label lblSubject = new Label(composite_4, SWT.NONE);
			lblSubject.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			lblSubject.setBounds(0, 0, 76, 18);
			lblSubject.setText("Subject");
			
			subject = new Text(composite_4, SWT.BORDER);
			subject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			Composite composite_5 = new Composite(composite_2, SWT.NONE);
			composite_5.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));
			composite_5.setLayout(new GridLayout());
			
			Button btnLink = new Button(composite_5, SWT.NONE);
			btnLink.setText("Link");
			
			Button btnSend = new Button(composite_5, SWT.NONE);
			btnSend.setText("Send");
			btnSend.addSelectionListener(new Clicker(){
				@Override public void clicked() {
					sendMsg();
				}});
			
			Composite composite_3 = new Composite(composite_1, SWT.NONE);
			composite_3.setLayout(new FillLayout(SWT.HORIZONTAL));
			composite_3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
			message = new Text(composite_3,SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.MULTI);
			// end
		}
		void sendMsg() {
			rpc.IGMBulkSendMsg(to.getText(),cc.getText(),subject.getText(),message.getText());
		}
	};
	protected void checkSubclass() {
	}
	@Override public void done(MailBoxFolder[] folders) {
		int i;
		for (i=0; i<folders.length; i++) {
			MailBoxFolder f = folders[i];
			if (f.name.equals("@In")) inbox = f;
			else if (f.name.equals("@Out")) outbox = f;
		}
		rpc.IGMGetMsgCount(inbox, this);
	}
	@Override public void gotCount(int count, MailBoxFolder f) {
		currentFolder = f;
		f.count = count;
		rows = new MailHeader[count];
		getMore(0);
	}
	private void getMore(final int start) {
		final long starttime = System.currentTimeMillis();
		rpc.IGMGetMsgHeader(start, start+50, currentFolder, 0, false, false, new MessageHeaderCallback(){
			@Override public void gotHeaders(MailHeader[] out) {
				Log.v(TAG,"request took:"+(System.currentTimeMillis() - starttime));
				Log.v(TAG,"mail count:"+out.length);
				int i;
				for (i=0;i<out.length;i++) {
					rows[start+i] = out[i];
				}
				updateRows();
			}
		});
	}
	private void updateRows() {
		for (Control c : messages.getChildren()) {
			c.dispose();
		}
		int i;
		for (i=0; i<rows.length; i++) {
			Log.v(TAG,"row"+i+":"+rows[i]);
			MailHeader mh = rows[i];
			if (mh != null) {
				Button b = new Button(messages,SWT.NONE);
				b.setText(mh.subject);
				b.addSelectionListener(new OpenMessage(mh));
				Label fixme = new Label(messages,SWT.NONE);
				fixme.setText(mh.from);
				Label ts = new Label(messages,SWT.NONE);
				ts.setText(""+mh.date);
			} else {
				Button b = new Button(messages,SWT.NONE);
				b.setText("load more!");
				b.addSelectionListener(new GetMore(i));
				break;
			}
		}
		Point size = messages.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		//messages.setSize(size);
		scrolledComposite.setMinSize(size);
		messages.layout();
	}
	private class OpenMessage extends Clicker implements GotMailMessage {
		private MailHeader mh;
		public OpenMessage(MailHeader mh) {
			this.mh = mh;
		}
		@Override public void clicked() {
			String key = "mail"+mh.id;
			TabData td = tabs.get(key);
			if (td == null) {
				rpc.IGMGetMsg(mh,this);
			} else {
				tabFolder.setSelection(td.tab);
			}
		}
		@Override public void gotMailMessage(String message) {
			String key = "mail"+mh.id;
			TabData td = new Message(message,mh,key);
			tabFolder.setSelection(td.tab);
		}
	}
	private class Message extends TabData {
		private Text text;
		Message(String message,MailHeader mh, final String key) {
			super(key); 
			tab = new TabItem(tabFolder,SWT.NONE);
			tab.setText(mh.subject);
			Composite top = new Composite(tabFolder, SWT.NONE);
			tab.setControl(top);
			top.setLayout(new GridLayout(1, false));
			
			//Composite contents = new Composite(tabFolder, SWT.NONE);
			//contents.setLayout(new GridLayout(1, true));
			
			// top section
			Composite topPane = new Composite(top,SWT.NONE);
			topPane.setLayout(new GridLayout(2,false));
			Composite topLeftPane = new Composite(topPane,SWT.NONE);
			topLeftPane.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1));
			topLeftPane.setLayout(new GridLayout(2,false));
			new Label(topLeftPane,SWT.NONE).setText("Subject:");
			new Label(topLeftPane,SWT.NONE).setText(mh.subject);
			new Label(topLeftPane,SWT.NONE).setText("Date:");
			new Label(topLeftPane,SWT.NONE).setText(""+mh.date);
			new Label(topLeftPane,SWT.NONE).setText("From:");
			new Label(topLeftPane,SWT.NONE).setText(mh.from);
			new Label(topLeftPane,SWT.NONE).setText("To:");
			if (mh.to != null) {
				StringBuilder b = new StringBuilder();
				int i = 1;
				b.append(mh.to[0]);
				while (i < mh.to.length) {
					b.append(";");
					if (i > 4) {
						b.append("...");
						break;
					}
					b.append(mh.to[i++]);
				}
				new Label(topLeftPane,SWT.NONE | SWT.WRAP).setText(b.toString());
			} else {
				new Label(topLeftPane,SWT.NONE).setText("???");
			}
			new Label(topLeftPane,SWT.NONE).setText("CC");
			new Label(topLeftPane,SWT.NONE).setText("FIXME");
			Composite topRightPane = new Composite(topPane,SWT.NONE);
			topRightPane.setLayout(new GridLayout(1,false));
			new Button(topRightPane,SWT.NONE).setText("Reply");
			new Button(topRightPane,SWT.NONE).setText("Reply All");
			new Button(topRightPane,SWT.NONE).setText("Forward");
			makeButton(topRightPane,"Close",new Clicker(){
				@Override
				public void clicked() {
					tab.dispose();
					tabs.remove(key);
				}
			});
			
			// main section
			text = new Text(top, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
			// SWT.FILL, SWT.FILL, true, true, 1, 1
			GridData gd = new GridData();
			gd.grabExcessHorizontalSpace = true;
			gd.grabExcessVerticalSpace = true;
			gd.heightHint = text.getLineHeight() * 15;
			text.setLayoutData(gd);
			text.setText(message);
			//scrolledComposite_1.setMinSize(grpTest.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		}
		private void makeButton(Composite root,String title,Clicker clicker) {
			Button b = new Button(root,SWT.NONE);
			b.setText(title);
			b.addSelectionListener(clicker);
		}
	}
	private class GetMore extends Clicker {
		int pos;
		public GetMore(int i) {
			pos = i;
		}
		@Override public void clicked() {
			getMore(pos);
		}
	}
}
