package com.angeldsis.lou;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;
import android.util.Log;

import com.angeldsis.louapi.HttpRequest;

public class doRPCasync implements HttpRequest {
	static final String TAG = "doRPCasync";
	public void PostURL(final String urlIN, final String data, final Callback cb) {
		AsyncTask<Integer,Integer,HttpReply> desync = new AsyncTask<Integer,Integer,HttpReply>() {
			protected HttpReply doInBackground(Integer... arg0) {
				HttpReply reply = new HttpReply();
				try {
					URL url = new URL(urlIN);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setReadTimeout(30000);
					conn.setConnectTimeout(15000);
					conn.setRequestMethod("POST");
					conn.setDoOutput(true);
					HttpURLConnection.setFollowRedirects(false);
					conn.setFixedLengthStreamingMode(data.getBytes().length);
					conn.setRequestProperty("Content-Type", "application/json");
					PrintWriter out = new PrintWriter(conn.getOutputStream());
					out.print(data);
					out.close();
					conn.connect();
					reply.code = conn.getResponseCode();
					//Log.v(TAG,"response code "+reply.code);
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
						return null;
					}
					reply.body = buf.toString();
					return reply;
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.e(TAG,"IOException",e);
				}
				// TODO Auto-generated method stub
				return null;
			}
			protected void onPostExecute(HttpReply reply) {
				cb.done(reply);
			}
		};
		desync.execute(0);
	}
}
