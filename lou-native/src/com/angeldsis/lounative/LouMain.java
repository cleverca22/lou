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

public class LouMain {
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
			HttpCookie httpcookie = new HttpCookie("REMEMBER_ME_COOKIE",config.getRememberMe());
			httpcookie.setDomain("www.lordofultima.com");
			httpcookie.setPath("/");
			httpcookie.setVersion(0);
			try {
				((CookieManager)CookieHandler.getDefault()).getCookieStore()
						.add(new URI("http://www.lordofultima.com/"),
								httpcookie);
				System.out.println("cookie restored?");
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				System.out.println("hard-coded uri not valid"+ e);
			}
			session.check_cookie();
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
