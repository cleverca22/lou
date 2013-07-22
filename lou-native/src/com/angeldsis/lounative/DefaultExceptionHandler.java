package com.angeldsis.lounative;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;

import com.angeldsis.louapi.DnsError;
import com.angeldsis.louapi.HttpUtil.HttpReply;
import com.angeldsis.louapi.Log;
import com.angeldsis.louapi.TimeoutError;
import com.angeldsis.louutil.HttpUtilImpl;

public class DefaultExceptionHandler implements UncaughtExceptionHandler {
	UncaughtExceptionHandler oldHook;
	
	public DefaultExceptionHandler(UncaughtExceptionHandler currentHandler) {
		oldHook = currentHandler;
	}
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		try {
			InputStream in = getClass().getResourceAsStream("/lou-version.txt");
			BufferedReader input = new BufferedReader(new InputStreamReader(in));
			String version = input.readLine();
			printWriter.print(version);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		e.printStackTrace(printWriter);
		HttpReply reply;
		try {
			reply = HttpUtilImpl.getInstance().postUrl("http://angeldsis.com/dsisscripts/load/loudesktop", result.toString().getBytes());
		} catch (TimeoutError e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			oldHook.uncaughtException(t, e);
			return;
		} catch (DnsError e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			oldHook.uncaughtException(t, e);
			return;
		}
		if (reply.e == null) {
			Log.v("FIXME","show a friendly error");
			// upload worked, show a friendly error
			System.exit(-1);
		} else {
			// upload failed, let java handle it
			oldHook.uncaughtException(t, e);
		}
	}
}
