package com.angeldsis.lou;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

import com.angeldsis.louapi.HttpRequest;

public class doRPCasync implements HttpRequest {
	char[] buffer = new char[1024];
	static final String TAG = "doRPCasync";
	public void PostURL(final String urlIN, final String data, final Callback cb) {
		//AsyncTask<Integer,Integer,HttpReply> desync = new AsyncTask<Integer,Integer,HttpReply>() {
			//protected HttpReply doInBackground(Integer... arg0) {
				HttpReply reply = new HttpReply();
				try {
					URL url = new URL(urlIN);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setReadTimeout(30000);
					conn.setConnectTimeout(15000);
					conn.setRequestMethod("POST");
					conn.setDoOutput(true);
					HttpURLConnection.setFollowRedirects(false);
					byte[] raw_data = data.getBytes();
					conn.setFixedLengthStreamingMode(raw_data.length);
					conn.setRequestProperty("Content-Type", "application/json");
					
					OutputStream os = conn.getOutputStream();
					os.write(raw_data,0,raw_data.length);
					os.close();
					
					conn.connect();
					reply.code = conn.getResponseCode();
					//Log.v(TAG,"response code "+reply.code);
					reply.stream = conn.getInputStream();
					reply.size = conn.getContentLength();
					cb.done(reply);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.e(TAG,"IOException",e);
					HttpReply r2 = new HttpReply();
					r2.e = e;
					cb.done(r2);
				}
				//return null;
			//}
			//protected void onPostExecute(HttpReply reply) {
				//cb.done(reply);
			//}
		//};
		//desync.execute(0);
	}
}
