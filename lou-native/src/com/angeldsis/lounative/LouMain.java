package com.angeldsis.lounative;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import com.angeldsis.LOU.LouSession;

public class LouMain {
	boolean cli;
	LouSession session;
	public static void main(String[] args) throws Exception {
		int i;
		boolean cli = true;
		for (i = 0; i < args.length; i++) {
			if (args[i].equals("-cli")) cli = true;
			else if (args[i].equals("-nocli")) cli = false;
		}
		LouMain start = new LouMain();
		start.init(cli);
	}
	private void init(boolean cli2) throws Exception {
		Config.init();
		Display display = new Display();
		cli = cli2;
		session = new LouSession();
		if (true) {
			boolean worked = DoLogin.login(display,session);
			if (!worked) return;
		}
		System.out.println("done?");

		System.out.println("found "+session.servers.size());
		WorldSelect.start(display, session);
		while (true) {
			if (!display.readAndDispatch()) display.sleep();
			Shell[] shells = display.getShells();
			if (shells.length == 0) {
				System.out.println("quiting");
				break;
			}
		}
	}
}
