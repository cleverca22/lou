package com.angeldsis.lounative;

import java.lang.Thread.UncaughtExceptionHandler;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.angeldsis.louapi.Log;
import com.angeldsis.louapi.LouSession;
import com.angeldsis.louapi.LouSession.result;
import com.angeldsis.louutil.HttpUtilImpl;

public class LouMain {
	private static final String TAG = "LouMain";
	static LouMain instance;
	LouSession session;
	Display display;
	int auto_world;
	public static void main(String[] args) throws Exception {
		Logger.init();
		UncaughtExceptionHandler currentHandler = Thread.getDefaultUncaughtExceptionHandler();
		DefaultExceptionHandler deh;
		if (!(currentHandler instanceof DefaultExceptionHandler)) {
			deh = new DefaultExceptionHandler(currentHandler);
			Thread.currentThread().setUncaughtExceptionHandler(deh);
			Thread.setDefaultUncaughtExceptionHandler(deh);
		} else deh = (DefaultExceptionHandler) currentHandler;

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
		try {
			LouMain.instance.init(world);
		} catch (Exception e) {
			deh.uncaughtException(Thread.currentThread(), e);
		}
	}
	private void init(int world) throws Exception {
		Config.init();
		display = new Display();
		session = new LouSession(HttpUtilImpl.getInstance());
		auto_world = world;
		Config config = Config.getConfig();
		if (config.getRememberMe() != null) {
			HttpUtilImpl.getInstance().restore_cookie(config.getRememberMe());
			Log.v(TAG,"checking cookie");
			result r = session.check_cookie(config.getUsername());
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
		for (CoreSession sess : CoreSession.sessions) {
			sess.rpc.stopLooping();
		}
		display.dispose();
	}
}
