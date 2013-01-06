package com.angeldsis.lounative;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import com.angeldsis.louapi.HttpRequest;

public class HttpRequestWrap implements HttpRequest {
	public void PostURL(String urlIN, String data, Callback cb) {
		HttpReply reply = new HttpReply();
		try {
			URL url = new URL(urlIN);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(30000);
			conn.setConnectTimeout(15000);
			conn.setRequestMethod("POST");
			HttpURLConnection.setFollowRedirects(false);
			conn.setFixedLengthStreamingMode(data.getBytes().length);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);
			PrintWriter out = new PrintWriter(conn.getOutputStream());
			out.print(data);
			out.close();
			conn.connect();
			reply.code = conn.getResponseCode();
			//System.out.println("response code "+reply.code);
			char[] buffer = new char[1024];
			int size;
			StringBuilder buf = new StringBuilder();
			InputStreamReader reply1 = new InputStreamReader(conn.getInputStream()); // FIXME, change char encoding?
			try {
				while ((size = reply1.read(buffer, 0, 1024)) != -1) {
					buf.append(buffer,0,size);
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				cb.done(null);
				return;
			}
			reply.body = buf.toString();
			cb.done(reply);
			return;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			cb.error(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cb.done(null);
		return;
	}
}
