package com.angeldsis.lounative;

import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

import com.angeldsis.louapi.Log;
import com.angeldsis.louapi.LouSession;
import com.angeldsis.louapi.ServerInfo;

public class WorldSelect extends Shell {
	Display display;
	static TrayItem item;
	private WorldSelect(Display display, LouSession session) {
		super(display);
		this.display = display;
		setLayout(new GridLayout(2, false));
		Iterator<ServerInfo> i = session.state.servers.iterator();
		while (i.hasNext()) {
			ServerInfo a = i.next();
			if (a.worldid == LouMain.instance.auto_world) {
				new CoreSession(a,display);
				return;
			}
			new Label(this,0).setText(a.servername);
			Button b = new Button(this,0);
			b.setText("login");
			// FIXME, check if the world already has a CoreSession
			b.addSelectionListener(new LoginAction(a));
		}
		setText("World Select");
		setupTray(display,session);
		open();
	}
	protected void checkSubclass() {
	}
	static void start(Display display, LouSession session) {
		new WorldSelect(display, session);
	}
	class LoginAction extends Clicker {
		private ServerInfo a;
		LoginAction(ServerInfo a2) {
			this.a = a2;
			Log.v("WorldSelect","account:"+a2.worldid);
		}
		@Override public void clicked() {
			new CoreSession(a,display);
		}
	}
	void setupTray(final Display display, final LouSession session) {
		if (WorldSelect.item != null) return;
		final Image image = new Image (display, 16, 16);
		final Image image2 = new Image (display, 16, 16);
		GC gc = new GC(image2);
		gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
		gc.fillRectangle(image2.getBounds());
		gc.dispose();
		Tray tray = display.getSystemTray();
		TrayItem item = new TrayItem(tray,SWT.NONE);
		WorldSelect.item = item;
		item.setToolTipText("Lord of Ultima");
		item.setImage(image2);
		item.setHighlightImage(image);
		final Shell fake = new Shell(display,0);
		final Menu menu = new Menu(fake,SWT.POP_UP);
		MenuItem mi = new MenuItem(menu,SWT.PUSH);
		mi.setText("open world select");
		mi.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event event) {
				WorldSelect.start(display,session);
			}
		});
		menu.setDefaultItem(mi);
		mi = new MenuItem(menu,SWT.PUSH);
		mi.setText("quit");
		mi.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				// FIXME, doesnt actualy quit, just deletes the fake Shell, quit happens when all Shells are deleted
				System.out.println("quit");
				fake.dispose();
				WorldSelect.item.dispose();
				image2.dispose();
				image.dispose();
				WorldSelect.item = null;
				
				// does this work?
				Shell[] shells = display.getShells();
				int i;
				for (i=0; i<shells.length; i++) {
					shells[i].dispose();
				}
			}
		});
		item.addListener (SWT.MenuDetect, new Listener () {
			public void handleEvent (Event event) {
				menu.setVisible(true);
			}
		});
	}
}
