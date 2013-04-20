package com.angeldsis.lounative;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.angeldsis.louapi.Log;
import com.angeldsis.louapi.LouSession;
import com.angeldsis.louapi.LouSession.result;

public class LouMain {
	private static final String TAG = "LouMain";
	static LouMain instance;
	LouSession session;
	Display display;
	int auto_world;
	public static void main(String[] args) throws Exception {
		int i;
		int world = 0;
		for (i = 0; i < args.length; i++) {
			if (args[i].equals("--world")) {
				i++;
				world = Integer.parseInt(args[i]);
			} else {
				System.out.println("unknown argument:"+args[i]);
			}
		}
		LouMain.instance = new LouMain();
		LouMain.instance.init(world);
	}
	private void init(int world) throws Exception {
		Config.init();
		Logger.init();
		display = new Display();
		session = new LouSession();
		auto_world = world;
		Config config = Config.getConfig();
		if (config.getRememberMe() != null) {
			session.restore_cookie(config.getRememberMe());
			Log.v(TAG,"checking cookie");
			result r = session.check_cookie();
			if (r.worked == false) {
				boolean worked = DoLogin.login(display, session);
				if (!worked) {
					Log.v(TAG,"login failed");
					return;
				}
			}
		} else {
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
