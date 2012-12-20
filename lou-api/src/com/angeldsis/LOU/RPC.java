package com.angeldsis.LOU;

import java.io.InputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.angeldsis.LOU.HttpRequest.HttpReply;

public abstract class RPC extends Thread {
	static String TAG = "lou.RPC";
	private Account account;
	String instanceid;
	String urlbase;
	public LouState state;
	int requestid;
	boolean cont;

	public RPC(Account acct, LouState state) {
		this.account = acct;
		this.state = state;
		requestid = 0;
		urlbase = "http://prodgame"+acct.serverid+".lordofultima.com/"+acct.pathid+"/Presentation/Service.svc/ajaxEndpoint/";
	}
	public void OpenSession(final boolean reset,final RPCDone callback2, final int retry_count) {
		if (retry_count > 10) {
			Log.e(TAG,"too many retrys");
			return;
		}
		JSONObject obj = new JSONObject();
		try {
			obj.put("reset", reset);
			doRPC("OpenSession",obj,this,new RPCCallback() {
				@Override
				void requestDone(rpcreply reply) throws JSONException,Exception {
					Log.v(TAG,"http code:"+reply.http_code);
					int r = reply.reply.getInt("r");
					if (r < 0) {
						Thread.sleep(1000);
						OpenSession(reset,callback2,retry_count+1);
						return;
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
					Log.v(TAG+".GetServerInfo",reply.reply.toString(1));
					rpcDone.requestDone(reply.reply);
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	private InputStream doRPC(final String function,JSONObject request, RPC parent, final RPCCallback rpcCallback) throws JSONException {
		if (function == "OpenSession") request.put("session", parent.account.sessionid);
		else request.put("session", parent.instanceid);
		HttpRequest req2 = newHttpRequest();
		HttpRequest.Callback cb = new HttpRequest.Callback() {
			public void done(HttpReply reply) {
				rpcreply reply2 = new rpcreply();
				reply2.http_code = reply.code;

				String json = reply.body;
				//Log.v("louRPCREPLY",json);
				if (json.length() == 0) {
					reply2.raw_reply = "";
				} else {
					try {
						Object t = new JSONTokener(json).nextValue();
						if (function.equals("Poll")) reply2.replyArray = (JSONArray) t;
						else reply2.reply = (JSONObject) t;
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						reply2.raw_reply = json;
					}
				}
				try {
					rpcCallback.requestDone(reply2);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		req2.PostURL(urlbase + function, request.toString(), cb);
		return null;
	}
	/** creates an instance of a class implementing HttpRequest */
	public abstract HttpRequest newHttpRequest();
	class rpcreply {
		public JSONObject reply;
		public int http_code;
		public String raw_reply;
		public JSONArray replyArray;
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
					Log.v(TAG+".GetPlayerInfo",r.reply.toString(1));
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
			String requests = "CITY:"+state.currentCity.cityid;
			if (state.visData.size() == 0) requests = requests + "\fVIS:c:"+state.currentCity.cityid+":0:-1085:-638:775:565:1"; // FIXME
			obj.put("requests",requests);
			doRPC("Poll",obj,this,new RPCCallback() {
				void requestDone(rpcreply r) throws JSONException {
					int x;
					if (r.replyArray == null) return;
					for (x = 0; x < r.replyArray.length(); x++) {
						JSONObject obj = (JSONObject) r.replyArray.get(x);
						handlePollPacket(obj);
					}
				}
			});
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	void handlePollPacket(JSONObject p) throws JSONException {
		String C = p.getString("C");
		Log.v(TAG,"Poll packet "+C);
		if (C.equals("VIS")) {
			JSONObject D = p.getJSONObject("D");
			parseVIS(D);
		} else if (C.equals("CITY")) {
			JSONObject D = p.getJSONObject("D");
			JSONArray r = D.getJSONArray("r");
			int x;
			for (x = 0; x < r.length(); x++) {
				JSONObject item = r.getJSONObject(x);
				int i = item.getInt("i"); // id
				int m = item.getInt("m"); // max
				double b = item.getDouble("b"); // last value
				double d = item.getDouble("d"); // gain per sec
				Log.v(TAG,"resource "+i+" count "+b+"/"+m);
				state.resources[i-1].set(d,b,m);
			}
			gotCityData();
		} else {
			Log.v(TAG,"unexpected Poll data "+C);
		}
	}
	void parseVIS(JSONObject D) throws JSONException {
		JSONArray u = D.getJSONArray("u");
		int x;
		for (x = 0; x < u.length(); x++) {
			JSONObject structure = u.getJSONObject(x);
			LouVisData parsed = null;
			int type = structure.getInt("t");
			switch (type) {
			case 3: // CityObject
				break;
			case 4:
				parsed = new CityBuilding(structure,CityBuilding.BUILDING);
				break;
			case 5: // CityBuildingPlace
				break;
			case 9: // CityResField
				parsed = new CityResField(structure);
				break;
			case 10: // CityFortification
				parsed = new CityBuilding(structure,CityBuilding.WALL);
				break;
			default:
				Log.v(TAG,"unhandled VIS type "+type);
			}
			if (parsed != null) {
				parsed.type = type;
				state.addVisObj(parsed);
			}
		}
		visDataReset();
	}
	public void run() {
		while (cont) {
			tick();
			// FIXME, may cause multiple parallel requests if they take over 10sec
			this.Poll();
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void startPolling() {
		cont = true;
		this.start();
	}
	public void stopPolling() {
		cont = false;
		interrupt();
	}
	/** called when all state.visData has been reloaded */
	public abstract void visDataReset();
	/** called when Poll happens */
	public abstract void tick();
	/** called when state.resources has been updated */
	public abstract void gotCityData();
}
