package com.angeldsis.LOU;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.AsyncTask;
import android.util.Log;

public class RPC {
	static String TAG = "lou.RPC";
	private Account account;
	String instanceid;
	String urlbase;
	LouState state;
	int requestid;

	public RPC(Account acct, LouState state) {
		this.account = acct;
		this.state = state;
		requestid = 0;
		urlbase = "http://prodgame"+acct.serverid+".lordofultima.com/"+acct.pathid+"/Presentation/Service.svc/ajaxEndpoint/";
	}
	public void OpenSession(boolean reset,final RPCDone callback2) {
		JSONObject obj = new JSONObject();
		try {
			obj.put("reset", reset);
			Log.v(TAG,obj.toString());
			doRPC("OpenSession",obj,this,new RPCCallback() {
				@Override
				void requestDone(rpcreply reply) throws JSONException,Exception {
					Log.v(TAG,"http code:"+reply.http_code);
					int r = reply.reply.getInt("r");
					if (r != 1) {
						throw new Exception("r was "+r);
					}
					instanceid = reply.reply.getString("i");
					callback2.requestDone(null);
				}
			});
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void GetServerInfo(final RPCDone rpcDone) {
		JSONObject obj = new JSONObject();
		try {
			obj.put("time", System.currentTimeMillis());
			doRPC("GetServerInfo",obj,this,new RPCCallback() {
				void requestDone(rpcreply reply) throws JSONException {
					Log.v(TAG,"http code:"+reply.http_code);
					Log.v(TAG,reply.reply.toString(1));
					rpcDone.requestDone(reply.reply);
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	private InputStream doRPC(String function,JSONObject request, RPC parent, RPCCallback rpcCallback) throws JSONException {
		rpcreq req = new rpcreq();
		req.function = function;
		req.request = request;
		if (function == "OpenSession") req.request.put("session", parent.account.sessionid);
		else req.request.put("session", parent.instanceid);
		doRPCasync desync = (doRPCasync) new doRPCasync(rpcCallback,urlbase).execute(req);
		return null;
	}
	class rpcreply {
		public JSONObject reply;
		public int http_code;
		public String raw_reply;
		public JSONArray replyArray;
	}
	class rpcreq {
		String function;
		JSONObject request;
	}
	private class doRPCasync extends AsyncTask<rpcreq,Integer,rpcreply> {
		RPCCallback callback;
		String urlbase;
		public doRPCasync(RPCCallback rpcCallback, String urlbase) {
			callback = rpcCallback;
			this.urlbase = urlbase;
		}
		@Override
		protected rpcreply doInBackground(rpcreq... params) {
			rpcreply reply = new rpcreply();
			try {
				rpcreq req = params[0];
				URL url = new URL(urlbase + req.function);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setReadTimeout(30000);
				conn.setConnectTimeout(15000);
				conn.setRequestMethod("POST");
				HttpURLConnection.setFollowRedirects(false);
				String data = req.request.toString();
				conn.setFixedLengthStreamingMode(data.getBytes().length);
				conn.setRequestProperty("Content-Type", "application/json");
				PrintWriter out = new PrintWriter(conn.getOutputStream());
				out.print(data);
				out.close();
				conn.connect();
				reply.http_code = conn.getResponseCode();
				Log.v(TAG,"response code "+reply.http_code);
				char[] buffer = new char[1024];
				int size;
				StringBuilder buf = new StringBuilder();
				InputStreamReader reply1 = new InputStreamReader(conn.getInputStream()); // FIXME, change char encoding?
				while ((size = reply1.read(buffer, 0, 1024)) != -1) {
					buf.append(buffer,0,size);
				}
				String json = buf.toString();
				Log.v("louRPCREPLY",json);
				try {
					Object t = new JSONTokener(json).nextValue();
					if (req.function.equals("Poll")) reply.replyArray = (JSONArray) t;
					else reply.reply = (JSONObject) t;
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					reply.raw_reply = json;
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return reply;
		}
		protected void onPostExecute(rpcreply r) {
			try {
				callback.requestDone(r);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private abstract class RPCCallback {
		abstract void requestDone(rpcreply r) throws JSONException, Exception;
	}
	public abstract class RPCDone {
		public abstract void requestDone(JSONObject reply);
	}
	public void GetPlayerInfo(final RPCDone rpcDone) {
		JSONObject obj = new JSONObject();
		try {
			doRPC("GetPlayerInfo",obj,this,new RPCCallback () {
				@Override
				void requestDone(rpcreply r) throws JSONException, Exception {
					state.processPlayerInfo(r.reply);
					Log.v(TAG,r.reply.toString(1));
					rpcDone.requestDone(r.reply);
				}
			});
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void Poll() {
		try {
			JSONObject obj = new JSONObject();
			obj.put("requestid", requestid);
			requestid++;
			String requests = "CITY:"+state.currentCity.cityid+
					"\fVIS:c:"+state.currentCity.cityid+":0:-1085:-638:775:565:1"; // FIXME
			obj.put("requests",requests);
			doRPC("Poll",obj,this,new RPCCallback() {
				void requestDone(rpcreply r) throws JSONException {
					int x;
					for (x = 0; x < r.replyArray.length(); x++) {
						JSONObject obj = (JSONObject) r.replyArray.get(x);
						String C = obj.getString("C");
						JSONObject D = obj.getJSONObject("D");
						Log.v(TAG,"Poll packet "+C);
						Log.v(TAG,D.toString(1));
					}
				}
			});
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
