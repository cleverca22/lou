package com.angeldsis.louapi;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

import org.json2.JSONArray;
import org.json2.JSONException;
import org.json2.JSONObject;
import org.json2.JSONTokener;

import com.angeldsis.louapi.HttpRequest.HttpReply;
import com.angeldsis.louapi.LouState.City;

public abstract class RPC extends Thread {
	static String TAG = "RPC";
	private Account account;
	String instanceid;
	String urlbase;
	public LouState state;
	int requestid;
	boolean cont,polling,running;
	private ArrayList<String> chat_queue;
	private ArrayList<Runnable> queue;
	AllianceAttackMonitor aam;

	public RPC(Account acct, LouState state) {
		this.account = acct;
		this.state = state;
		aam = new AllianceAttackMonitor(this);
		requestid = 0;
		urlbase = "http://prodgame"+acct.serverid+".lordofultima.com/"+acct.pathid+"/Presentation/Service.svc/ajaxEndpoint/";
		chat_queue = new ArrayList<String>();
		queue = new ArrayList<Runnable>();
		polling = false;
		synchronized(this) {
			running = true;
			start();
		}
	}
	public void OpenSession(final boolean reset,final RPCDone callback) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				OpenSession(reset,callback,0);
			}
		};
		post(r);
	}
	public void GetBuildingInfo(final LouVisData v, final RPCDone callback) {
		post(new Runnable() {
			public void run() {
				try {
					JSONObject obj = new JSONObject();
					obj.put("cityid",v.getCity().cityid);
					obj.put("buildingid", v.visId);
					doRPC("GetBuildingInfo",obj,RPC.this,new RPCCallback() {
						@Override
						void requestDone(rpcreply r) throws JSONException,
								Exception {
							callback.requestDone(r.reply);
						}
					},5);
				} catch (JSONException e) {
					// FIXME
					e.printStackTrace();
				}
			}
		});
	}
	public void UpgradeBuilding(final City c, final int coord, final int structureid, final UpgradeStarted cb) {
		post(new Runnable() {
			@Override
			public void run() {
				try {
					JSONObject obj = new JSONObject();
					obj.put("cityid", c.cityid);
					obj.put("buildingid", coord);
					obj.put("buildingType", structureid);
					obj.put("isPaid", true);
					doRPC("UpgradeBuilding",obj,RPC.this,new RPCCallback() {
						@Override
						void requestDone(rpcreply r) throws JSONException {
							Log.v(TAG,r.reply.toString(1));
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									cb.started();
								}
							});
						}
					},5);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	public interface UpgradeStarted {
		void started();
	}
	public void GetBuildingUpgradeInfo(final City c,final int coord) {
		post(new Runnable() {
			@Override
			public void run() {
				try {
					JSONObject obj = new JSONObject();
					obj.put("buildingPlace",coord);
					obj.put("cityid", c.cityid);
					doRPC("GetBuildingUpgradeInfo",obj,RPC.this,new RPCCallback() {
						@Override
						void requestDone(rpcreply r) throws JSONException,
								Exception {
							Log.v(TAG,r.reply.toString(1));
						}
					},5);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	private void post(Runnable r) {
		queue.add(r);
		if (Thread.currentThread() != this) {
			synchronized(this) {
				if (running) interrupt();
				else {
					Log.v(TAG,"starting thread for post");
					running = true;
					start();
				}
			}
		}
	}
	private void OpenSession(final boolean reset,final RPCDone callback2, final int retry_count) {
		if (retry_count > 10) {
			Log.e(TAG,"too many retrys");
			onEjected();
			// FIXME, better error msg
			return;
		}
		JSONObject obj = new JSONObject();
		try {
			obj.put("reset", reset);
			doRPC("OpenSession",obj,this,new RPCCallback() {
				@Override
				void requestDone(rpcreply reply) throws JSONException,Exception {
					Log.v(TAG,account.sessionid+" http code:"+reply.http_code);
					int r = reply.reply.getInt("r");
					Log.v(TAG,reply.reply.toString(1));
					if (r < 0) {
						Thread.sleep(1000);
						OpenSession(reset,callback2,retry_count+1);
						return;
					}
					instanceid = reply.reply.getString("i");
					callback2.requestDone(null);
				}
			},5);
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
					//Log.v(TAG+".GetServerInfo",reply.reply.toString(1));
					rpcDone.requestDone(reply.reply);
				}
			},5);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	private void doRPC(final String function,final JSONObject request, final RPC parent, final RPCCallback rpcCallback, final int retry) throws JSONException {
		if (retry == 0) {
			System.out.println("too many treies");
			return;
		}
		if (function == "OpenSession") request.put("session", parent.account.sessionid);
		else request.put("session", parent.instanceid);
		HttpRequest req2 = new HttpRequest();
		HttpRequest.Callback cb = new HttpRequest.Callback() {
			public void done(HttpReply reply) {
				rpcreply reply2 = new rpcreply();
				if (reply.e != null) {
					if (reply.e instanceof UnknownHostException) {
						Log.w(TAG,"dns error, retrying");
					} else {
						Log.e(TAG, "exception from http req, retrying",reply.e);
					}
					try {
						doRPC(function,request,parent,rpcCallback,retry - 1);
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					return;
				}
				reply2.http_code = reply.code;

				if (reply.size == 0) {
					reply2.raw_reply = null;
				} else {
					try {
						Object t = new JSONTokener(new InputStreamReader(reply.stream)).nextValue();
						if (function.equals("Poll")) reply2.replyArray = (JSONArray) t;
						else reply2.reply = (JSONObject) t;
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						reply2.raw_reply = reply.stream;
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
		return;
	}
	/** creates an instance of a class implementing HttpRequest */
	class rpcreply {
		public JSONObject reply;
		public int http_code;
		public InputStream raw_reply;
		public JSONArray replyArray;
	}
	private abstract class RPCCallback {
		abstract void requestDone(rpcreply r) throws JSONException, Exception;
	}
	public interface RPCDone {
		public void requestDone(JSONObject reply);
	}
	public void GetPlayerInfo(final RPCDone rpcDone) {
		JSONObject obj = new JSONObject();
		try {
			doRPC("GetPlayerInfo",obj,this,new RPCCallback () {
				@Override
				void requestDone(rpcreply r) throws JSONException, Exception {
					state.processPlayerInfo(r.reply);
					//Log.v(TAG+".GetPlayerInfo",r.reply.toString(1));
					rpcDone.requestDone(r.reply);
					runOnUiThread(new Runnable() {
						public void run() {
							cityListChanged();
							cityChanged();
						}
					});
				}
			},5);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public abstract void runOnUiThread(Runnable r);
	public abstract void cityChanged();
	public abstract void cityListChanged();
	public void Poll() {
		try {
			JSONObject obj = new JSONObject();
			obj.put("requestid", requestid);
			requestid++;
			String requests = "CITY:"+state.currentCity.cityid;
			if (state.fetchVis) {
				requests += "\fVIS:c:"+state.currentCity.cityid+":0:-1085:-638:775:565:"+state.currentCity.visreset; // FIXME last field is reset, check webfrontend.vis.Main.js for others
			}
			if (chat_queue.size() > 0) {
				String msg = chat_queue.remove(0);
				requests = requests + "\fCHAT:"+msg;
			} else requests = requests + "\fCHAT:";
			requests += "\fPLAYER:";
			requests += "\fTIME:"+System.currentTimeMillis();
			requests += "\fREPORT:";
			requests += "\fALLIANCE:";
			requests += aam.getRequestDetails();
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
			},5);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	void handlePollPacket(JSONObject p) throws JSONException {
		boolean showName = true;
		String C = p.getString("C");
		if (C.equals("TIME")) {
			JSONObject D = p.optJSONObject("D");
			long refTime = D.optLong("Ref");
			int stepTime = D.optInt("Step");
			int diff = D.optInt("Diff");
			int serverOffset = D.optInt("o") * 60 * 60 * 1000;
			state.setTime(refTime,stepTime,diff,serverOffset);
		} else if (C.equals("VIS")) {
			JSONObject D = p.getJSONObject("D");
			parseVIS(D);
		} else if (C.equals("CITY")) {
			// refer to webfrontend.data.City.js dispatchResults for more info
			JSONObject D = p.getJSONObject("D");
			//Log.v(TAG,D.toString(1));
			JSONArray r = D.getJSONArray("r");
			int x;
			for (x = 0; x < r.length(); x++) {
				JSONObject item = r.getJSONObject(x);
				int i = item.getInt("i"); // id
				int m = item.getInt("m"); // max
				double b = item.getDouble("b"); // last value
				double d = item.getDouble("d"); // gain per sec
				int step = item.getInt("s");
				state.currentCity.resources[i-1].set(d,b,m,step);
			}
			state.processCityPacket(D);
			runOnUiThread(new Runnable () {public void run() {
			gotCityData();
			}});
		} else if (C.equals("CHAT")) {
			JSONArray D = p.getJSONArray("D");
			int i;
			final ArrayList<ChatMsg> recent = new ArrayList<ChatMsg>(); 
			for (i = 0; i < D.length(); i++) {
				ChatMsg c = new ChatMsg(D.getJSONObject(i));
				state.chat_history.add(c);
				recent.add(c);
			}
			runOnUiThread(new Runnable () {public void run() {
			onChat(recent);
			}});
			//Log.v(TAG,D.toString(1));
		} else if (C.equals("PLAYER")) {
			// refer to webfrontend.data.Player.js dispatchResults for more info
			JSONObject D = p.getJSONObject("D");
			state.parsePlayerUpdate(D);
			runOnUiThread(new Runnable () {
				public void run() {
					onPlayerData();
				}
			});
		} else if (C.equals("SYS")) {
			Log.v(TAG,p.toString(1));
			if (p.getString("D").equals("CLOSED")) {
				this.stopPolling();
				onEjected();
				stopLooping();
			}
		} else if (C.equals("REPORT")) {
			JSONObject D = p.optJSONObject("D");
			final int viewed = D.optInt("v");
			final int unviewed = D.optInt("u");
			runOnUiThread(new Runnable() {
				public void run () {
					onReportCountUpdate(viewed,unviewed);
				}
			});
		} else if (C.equals("ALLIANCE")) {
			JSONObject D = p.optJSONObject("D");
			//Log.v(TAG,D.toString(1));
			state.parseAllianceUpdate(D);
			showName = false;
		} else if (C.equals("ALL_AT")) {
			aam.parseReply(p.optJSONObject("D"));
		} else {
			Log.v(TAG,"unexpected Poll data "+C);
		}
		if (showName) Log.v(TAG,"Poll packet "+C);
	}
	public abstract void onReportCountUpdate(int viewed,int unviewed);
	public abstract void onNewAttack(IncomingAttack a);
	/** called when the session is ended, usually by logging in elsewhere **/
	abstract public void onEjected();
	/** queues a chat message like /a hello\n **/
	public void QueueChat(String message) {
		chat_queue.add(message);
		interrupt();
	}
	public abstract void onChat(ArrayList<ChatMsg> recent);
	/** called after state.gold and state.incoming_attacks is updated
	 */
	public abstract void onPlayerData();
	void parseVIS(JSONObject D) throws JSONException {
		City c = state.currentCity;
		if (c.visreset == 1) {
			c.visData.clear();
		}
		JSONArray u = D.getJSONArray("u");
		int x;
		for (x = 0; x < u.length(); x++) {
			JSONObject structure = u.getJSONObject(x);
			LouVisData parsed = null,temp = null;
			
			// find the existing structure to update
			int findme = structure.optInt("i");
			Iterator<LouVisData> i = c.visData.iterator();
			while (i.hasNext()) {
				temp = i.next();
				if (temp.visId == findme) {
					parsed = temp;
					break;
				}
			}
			if (parsed == null) { // if it was not found
				int type = structure.getInt("t");
				switch (type) {
				case 3: // CityObject
					break;
				case 4:
					parsed = new CityBuilding(c,structure,CityBuilding.BUILDING);
					break;
				case 5: // CityBuildingPlace
					break;
				case 9: // CityResField
					parsed = new CityResField(c,structure);
					break;
				case 10: // CityFortification
					parsed = new CityBuilding(c,structure,CityBuilding.WALL);
					break;
				case 13:
					// FIXME return new webfrontend.vis.CityWallLevel(this, bI.l, bI.i);
					break;
				default:
					Log.v(TAG,"unhandled VIS type "+type);
				}
				if (parsed != null) {
					parsed.type = type;
					c.addVisObj(parsed);
					runOnUiThread(new visObjMade(parsed));
				}
			} else { // if it was found
				runOnUiThread(new uiUpdate(parsed,structure));
			}
		}
		if (c.visreset == 1) {
			c.visreset = 0;
			runOnUiThread(new Runnable () {public void run() {
				visDataReset();
			}});
		} else {
			runOnUiThread(new Runnable () {public void run() {
			visDataUpdated();
			}});
		}
	}
	private class visObjMade implements Runnable {
		LouVisData v;
		visObjMade(LouVisData v) { this.v = v; }
		public void run() {
			RPC.this.onVisObjAdded(v);
		}
	}
	public abstract void onVisObjAdded(LouVisData v);
	private class uiUpdate implements Runnable {
		LouVisData v;
		JSONObject s;
		uiUpdate(LouVisData v, JSONObject s) {
			this.v = v;
			this.s = s;
		}
		public void run() {
			try {
				v.update(s);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void run() {
		cont = true;
		while (cont) {
			while (queue.size() > 0) {
				Runnable r = queue.remove(0);
				Log.v(TAG,"running queued item");
				r.run();
			}
			tick();
			// FIXME, may cause multiple parallel requests if they take over 10sec
			// nvm, Poll is now syncronys
			if (polling) Poll();
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
			}
		}
		Log.v(TAG,"dying");
		running = false;
	}
	public void startPolling() {
		polling = true;
		synchronized(this) {
			if (!running) {
				running = true;
				start();
			}
		}
	}
	public void stopPolling() {
		polling = false;
		interrupt();
	}
	public void stopLooping() {
		cont = false;
	}
	/** called when all state.visData has been reloaded */
	public abstract void visDataReset();
	public abstract void visDataUpdated();
	/** called when Poll happens */
	public abstract void tick();
	/** called when state.resources has been updated */
	public abstract void gotCityData();
}
