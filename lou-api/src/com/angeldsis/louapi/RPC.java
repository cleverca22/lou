package com.angeldsis.louapi;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.json2.JSONArray;
import org.json2.JSONException;
import org.json2.JSONObject;
import org.json2.JSONTokener;

import com.angeldsis.louapi.HttpRequest.HttpReply;
import com.angeldsis.louapi.LouState.City;
import com.angeldsis.louapi.data.AllianceForum;
import com.angeldsis.louapi.data.ForumPost;
import com.angeldsis.louapi.data.ForumThread;
import com.angeldsis.louapi.data.OrderTargetInfo;
import com.angeldsis.louapi.data.PublicCityInfo;
import com.angeldsis.louapi.data.SubRequest;
import com.angeldsis.louapi.data.World;
import com.angeldsis.louapi.world.WorldParser;
import com.angeldsis.louapi.world.WorldParser.WorldCallbacks;

public abstract class RPC extends Thread implements WorldCallbacks {
	static String TAG = "RPC";
	private Account account;
	String instanceid;
	String urlbase;
	public LouState state;
	int requestid;
	boolean cont,polling,running;
	private ArrayList<String> chat_queue;
	private DelayQueue<MyTimer> queue;
	AllianceAttackMonitor aam;
	Poller poller;
	WorldParser worldParser;
	World world = new World();

	public RPC(Account acct, LouState state) {
		worldParser = new WorldParser();
		
		this.account = acct;
		this.state = state;
		aam = new AllianceAttackMonitor(this);
		requestid = 0;
		urlbase = "http://prodgame"+acct.serverid+".lordofultima.com/"+acct.pathid+"/Presentation/Service.svc/ajaxEndpoint/";
		chat_queue = new ArrayList<String>();
		queue = new DelayQueue<MyTimer>();
		polling = false;
		poller = new Poller();
		synchronized(this) {
			//Log.v(TAG,"starting thread in constructor");
			//running = true;
			//start();
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
							callback.requestDone((JSONObject) r.reply);
						}
					},5);
				} catch (JSONException e) {
					// FIXME
					e.printStackTrace();
				}
			}
		});
	}
	public void GetLockboxURL(final GetLockboxURLDone callback) {
		post(new Runnable () {
			public void run() {
				try {
					JSONObject obj = new JSONObject();
					doRPC("GetLockboxURL",obj,RPC.this,new RPCCallback() {
						@Override void requestDone(final rpcreply r) {
							runOnUiThread(new Runnable() {
								public void run() {
									callback.done((String)r.reply);
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
	// FIXME, untested
	public void DemolishBuilding(final LouVisData v, final RPCDone callback) {
		post(new Runnable() {
			public void run() {
				try {
					JSONObject obj = new JSONObject();
					obj.put("cityid",v.getCity().cityid);
					obj.put("buildingid", v.visId);
					doRPC("DemolishBuilding",obj,RPC.this,new RPCCallback() {
						@Override
						void requestDone(rpcreply r) throws JSONException,
								Exception {
							callback.requestDone((JSONObject) r.reply);
						}
					},5);
				} catch (JSONException e) {
					// FIXME
					e.printStackTrace();
				}
			}
		});
	}
	public void ReportGetHeader(final String sPlayerName,final int city, final int start,final int end,
			final int sort, final boolean ascending, final int mask, final ReportHeaderCallback cb) {
		post(new Runnable() {
			public void run() {
				try {
					JSONObject obj = new JSONObject();
					obj.put("sPlayerName", sPlayerName);
					obj.put("city", city);
					obj.put("start", start);
					obj.put("end",end);
					obj.put("sort",sort);
					obj.put("ascending",ascending);
					obj.put("mask",mask);
					doRPC("ReportGetHeader",obj,RPC.this,new RPCCallback() {
						@Override
						void requestDone(rpcreply r) throws JSONException,
								Exception {
							JSONArray headers = (JSONArray) r.reply;
							final ReportHeader[] list = new ReportHeader[headers.length()];
							int i;
							for (i = 0; i < headers.length(); i++) {
								list[i] = new ReportHeader(headers.optJSONObject(i));
							}
							runOnUiThread(new Runnable() {
								public void run() {
									cb.done(list);
								}
							});
						}
					},5);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	public void SubstitutionAcceptReq(final int subid, final int playerid) {
		post(new Runnable() {
			public void run() {
				try {
					JSONObject obj = new JSONObject();
					obj.put("id", subid);
					obj.put("pid", playerid);
					doRPC("SubstitutionAcceptReq",obj,RPC.this,new RPCCallback() {
						public void requestDone(rpcreply r) {
							Log.v(TAG,r.reply.toString());
						}
					},5);
				} catch (JSONException e) {
					e.printStackTrace(); /// FIXME
				}
			}
		});
	}
	/** run on a SubRequest with role=giver
	 * @param s
	 */
	public void SubstitutionCancleReq(final SubRequest s) {
		post(new Runnable() {
			public void run() {
				try {
					JSONObject obj = new JSONObject();
					obj.put("id", s.id);
					obj.put("pid", s.giver.getId());
					doRPC("SubstitutionCancleReq",obj,RPC.this,new RPCCallback() {
						public void requestDone(rpcreply r) {
							Log.v(TAG,r.reply.toString());
						}
					},5);
				} catch (JSONException e) {
					e.printStackTrace(); /// FIXME
				}
			}
		});
	}
	public void SubstitutionCreateReq(final String name) {
		post(new Runnable() {
			public void run() {
				try {
					JSONObject obj = new JSONObject();
					obj.put("name", name);
					doRPC("SubstitutionCreateReq",obj,RPC.this,new RPCCallback() {
						public void requestDone(rpcreply r) {
							Log.v(TAG,r.reply.toString());
						}
					},5);
				} catch (JSONException e) {
					e.printStackTrace(); /// FIXME
				}
			}
		});
	}
	public void CreateSubstitutionSession(final SubRequest s) {
		post(new Runnable() {
			public void run() {
				try {
					JSONObject obj = new JSONObject();
					obj.put("id", s.id);
					obj.put("pid", s.giver.getId());
					doRPC("CreateSubstitutionSession",obj,RPC.this,new RPCCallback() {
						public void requestDone(final rpcreply r) {
							Log.v(TAG,r.reply.toString());
							runOnUiThread(new Runnable() {
								public void run() {
									String sessionid = (String) r.reply;
									startSubstituteSession(sessionid);
								}
							});
						}
					},5);
				} catch (JSONException e) {
					e.printStackTrace(); /// FIXME
				}
			}
		});
	}
	public abstract void startSubstituteSession(String sessionid);
	public void GetReport(final int reportid,final ReportCallback cb) {
		post(new Runnable() {
			public void run() {
				try {
					JSONObject obj = new JSONObject();
					obj.put("id", reportid);
					doRPC("GetReport",obj,RPC.this,new RPCCallback() {
						@Override
						void requestDone(rpcreply r) throws JSONException,
								Exception {
							final Report report = new Report((JSONObject) r.reply);
							runOnUiThread(new Runnable() {
								public void run() {
									cb.done(report);
								}
							});
						}
					},5);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	public interface ReportCallback {
		void done(Report report);
	}
	public interface ReportHeaderCallback {
		void done(ReportHeader[] list);
	}
	public void GetSharedReport(final String sharestring, final ReportCallback cb) {
		post(new Runnable () {
			public void run() {
				try {
					JSONObject obj = new JSONObject();
					obj.put("id",sharestring);
					doRPC("GetSharedReport",obj,RPC.this,new RPCCallback() {
						@Override
						void requestDone(rpcreply r) throws JSONException,
								Exception {
							Log.v(TAG,((JSONObject)r.reply).toString(1));
							final Report rr = new Report((JSONObject) r.reply);
							runOnUiThread(new Runnable() {
								@Override public void run() {
									cb.done(rr);
								}
							});
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
						@Override void requestDone(rpcreply r) throws JSONException {
							pollSoon();
							//Log.v(TAG,r.reply.toString(1));
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
							//Log.v(TAG,r.reply.toString(1));
						}
					},5);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	private void post(Runnable r) {
		MyTimer t = new MyTimer(r);
		Log.v(TAG,"adding to queue");
		queue.add(t);
		if (Thread.currentThread() != this) {
			synchronized(this) {
				if (running) {
					Log.v(TAG,"interupting network thread");
					interrupt();
				} else {
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
					JSONObject r2 = (JSONObject) reply.reply;
					int r = r2.getInt("r");
					Log.v(TAG,r2.toString(1));
					instanceid = r2.getString("i");
					if ("00000000-0000-0000-0000-000000000000".equals(instanceid)) {
						onEjected();
						return;
					}
					if (r < 0) {
						Thread.sleep(1000);
						OpenSession(reset,callback2,retry_count+1);
						return;
					}
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
					state.parseServerInfo((JSONObject)reply.reply);
					rpcDone.requestDone((JSONObject) reply.reply);
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
					} else if (reply.e instanceof FileNotFoundException) {
						Log.e(TAG, "wtf, file not found??");
						stopPolling();
						onEjected();
						stopLooping();
					} else {
						Log.e(TAG, "exception from http req, retrying "+retry+" more times",reply.e);
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
						long start = System.currentTimeMillis();
						// averaging 100-200ms per call
						reply2.reply = new JSONTokener(new InputStreamReader(reply.stream)).nextValue();
						long end = System.currentTimeMillis();
						Log.v(TAG, String.format("parsing took %dms",end-start));
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
		public Object reply;
		public int http_code;
		public InputStream raw_reply;
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
					state.processPlayerInfo((JSONObject) r.reply);
					//Log.v(TAG+".GetPlayerInfo",r.reply.toString(1));
					rpcDone.requestDone((JSONObject) r.reply);
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
	public void GetAllianceForums(final GetAllianceForumsCallback cb) {
		post(new Runnable() {
			@Override
			public void run() {
				JSONObject obj = new JSONObject();
				try {
					doRPC("GetAllianceForums",obj,RPC.this,new RPCCallback() {
						@Override void requestDone(rpcreply r) throws JSONException, Exception {
							JSONArray forums = (JSONArray) r.reply;
							int i;
							final AllianceForum[] output = new AllianceForum[forums.length()];
							for (i=0; i<forums.length(); i++) {
								JSONObject x = forums.getJSONObject(i);
								output[i] = new AllianceForum(x);
							}
							runOnUiThread(new Runnable() {
								public void run() {
									cb.done(output);
								}
							});
						}}, 5);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	public interface GetAllianceForumsCallback {
		void done(AllianceForum[] output);
	}
	public abstract void runOnUiThread(Runnable r);
	public abstract void cityChanged();
	public abstract void cityListChanged();
	public void Poll() {
		Log.v(TAG,"Poll");
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
				if (chat_queue.size() > 0) {
					Log.v(TAG,"need to poll again");
					queue.remove(poller);
					poller.pollSoon();
					queue.add(poller);
				}
			} else requests = requests + "\fCHAT:";
			requests += "\fPLAYER:";
			if (state.getFullPlayerData)requests += "a";
			requests += "\fTIME:"+System.currentTimeMillis();
			requests += "\fREPORT:";
			requests += "\fSERVER:";
			requests += "\fALLIANCE:";
			requests += "\fSUBSTITUTION:";
			if (state.userActivity) {
				state.userActivity = false;
				requests += "\fUA:";
			}
			if (worldParser != null) {
				requests += "\fWORLD:"+worldParser.getRequestDetails();
				//Log.v(TAG,requests);
			}
			requests += aam.getRequestDetails();
			requests += "\fTE:";
			obj.put("requests",requests);
			doRPC("Poll",obj,this,new RPCCallback() {
				void requestDone(rpcreply r) throws JSONException {
					int x;
					if (r.reply == null) return;
					JSONArray reply = (JSONArray) r.reply;
					for (x = 0; x < reply.length(); x++) {
						JSONObject obj = (JSONObject) reply.get(x);
						handlePollPacket(obj);
					}
				}
			},10);
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
		} else if (C.equals("WORLD")) {
			if (worldParser != null) worldParser.parse(p,this);
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
				state.currentCity.resources[i-1].set(d,b,m,step,state);
			}
			state.processCityPacket(D);
			runOnUiThread(new Runnable () {public void run() {
				gotCityData();
			}});
		} else if (C.equals("CHAT")) {
			showName = false;
			JSONArray D = p.getJSONArray("D");
			int i;
			final ArrayList<ChatMsg> recent = new ArrayList<ChatMsg>();
			synchronized (state.chat_history) {
				for (i = 0; i < D.length(); i++) {
					ChatMsg c = new ChatMsg(D.getJSONObject(i));
					state.chat_history.add(c);
					recent.add(c);
				}
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
			state.viewed_reports = viewed;
			state.unviewed_reports = unviewed;
			runOnUiThread(new Runnable() {
				public void run () {
					onReportCountUpdate();
				}
			});
		} else if (C.equals("ALLIANCE")) {
			JSONObject D = p.optJSONObject("D");
			//Log.v(TAG,D.toString(1));
			state.parseAllianceUpdate(D);
			showName = false;
		} else if (C.equals("ALL_AT")) {
			aam.parseReply(p.optJSONObject("D"));
		} else if (C.equals("SUBSTITUTION")) {
			JSONObject D = p.optJSONObject("D");
			state.parseSubs(D);
		} else {
			Log.v(TAG,"unexpected Poll data "+C+" "+p.toString());
		}
		if (showName) Log.v(TAG,"Poll packet "+C);
	}
	public abstract void onReportCountUpdate();
	public abstract void onNewAttack(IncomingAttack a);
	/** called when the session is ended, usually by logging in elsewhere **/
	abstract public void onEjected();
	/** queues a chat message like /a hello\n **/
	public void QueueChat(String message) {
		synchronized (this) {
			chat_queue.add(message);
			queue.remove(poller);
			poller.pollSoon(); // adjusts the time of the event
			queue.add(poller);
			interrupt(); // makes the thread re-check it
		}
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
		LouVisData[] changes = new LouVisData[u.length()];
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
					changes[x] = parsed;
				}
			} else { // if it was found
				runOnUiThread(new uiUpdate(parsed,structure));
			}
		}
		runOnUiThread(new visObjMade(changes));
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
		LouVisData[] v;
		visObjMade(LouVisData[] changes) { this.v = changes; }
		public void run() {
			RPC.this.onVisObjAdded(v);
		}
	}
	public abstract void onVisObjAdded(LouVisData[] v);
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
	Runnable ticker = new Runnable () {
		public void run() {
			tick();
		}
	};
	public void run() {
		cont = true;
		while (cont) {
			MyTimer t;
			while ((t = queue.poll()) != null) {
				setThreadActive(true);
				Runnable r = t.getRunnable();
				r.run();
				
			}
			runOnUiThread(ticker);
			if (polling && !queue.contains(poller)) {
				poller.setDelay(getMaxPoll());
				queue.add(poller);
			}
			long start=0,maxdelay=0;
			try {
				t = queue.peek();
				maxdelay = 60000;
				if (t != null) maxdelay = t.getDelay(TimeUnit.MILLISECONDS);
				else {
					Log.v(TAG, "no work found, maybe thread should stop?");
				}
				if (maxdelay < 0) maxdelay = 100;
				//Log.v(TAG,"delay "+maxdelay);
				setTimer(maxdelay);
				setThreadActive(false);
				start = System.currentTimeMillis();
				Thread.sleep(maxdelay);
			} catch (InterruptedException e) {
				Log.v(TAG,"who woke me!");
			}
			long end = System.currentTimeMillis();
			long overrun = (end-start)-maxdelay;
			if (overrun > 10) Log.v(TAG,String.format("%d sleep ran %d too late",maxdelay,overrun));
		}
		Log.v(TAG,"dying");
		setThreadActive(false);
		running = false;
	}
	public void setThreadActive(boolean b) {
	}
	/** on android, this helps with the AlarmManager
	 */
	public void setTimer(long maxdelay) {
	}
	// Poll must occur more often then 5mins
	protected abstract int getMaxPoll();
	public void startPolling() {
		polling = true;
		synchronized(this) {
			if (!queue.contains(poller)) {
				poller.pollSoon();
				queue.add(poller);
			}
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
	/** called when state.resources and queue have been updated */
	public abstract void gotCityData();
	public abstract void onSubListChanged();
	public void refreshConfig(boolean monitor) {
		aam.refreshConfig(monitor);
	}
	private class MyTimer implements Delayed {
		Runnable r;
		long target;
		public MyTimer() {}
		public MyTimer(Runnable r) {
			this.r = r;
			target = 0;
		}
		public void setDelay(int maxPoll) {
			target = System.currentTimeMillis() + maxPoll;
		}
		public Runnable getRunnable() {
			return r;
		}
		@Override
		public int compareTo(Delayed arg0) {
			// TODO Auto-generated method stub
			long self = getDelay(TimeUnit.MILLISECONDS);
			long other = arg0.getDelay(TimeUnit.MILLISECONDS);
			Log.v(TAG,String.format("compareTo %d %d",self,other));
			if (other < self) return 1;
			else if (other > self) return -1;
			return 0;
		}
		@Override
		public long getDelay(TimeUnit arg0) {
			return arg0.convert(target - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		}
	}
	private class Poller extends MyTimer {
		long lastpoll;
		public Poller() {
			r = new Runnable () {
				public void run() {
					lastpoll = System.currentTimeMillis();
					Poll();
				}
			};
			target = 0;
		}
		public void pollSoon() {
			long timepassed = System.currentTimeMillis() - lastpoll;
			if (timepassed > 2000) target = System.currentTimeMillis();
			else target = System.currentTimeMillis() + (2000 - timepassed);
		}
	}
	public void pollSoon() {
		synchronized(this) {
			queue.remove(poller);
			poller.pollSoon();
			queue.add(poller);
			interrupt();
		}
	}
	public interface GetLockboxURLDone {
		public void done(String reply);
	}
	public void GetAllianceForumThreads(final long forumID, final GotForumThreads cb) {
		post(new Runnable(){
			@Override public void run() {
				JSONObject obj = new JSONObject();
				try {
					obj.put("forumID", forumID);
					doRPC("GetAllianceForumThreads", obj, RPC.this, new RPCCallback(){
						@Override void requestDone(rpcreply r) throws JSONException {
							JSONArray a = (JSONArray) r.reply;
							int i;
							final ForumThread[] out = new ForumThread[a.length()];
							for (i=0; i<a.length(); i++) {
								JSONObject o = a.getJSONObject(i);
								out[i] = new ForumThread(o);
							}
							runOnUiThread(new Runnable() {
								@Override public void run() {
									cb.done(out);
								}
							});
						}
					}, 5);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	public interface GotForumThreads {
		void done(ForumThread[] out);
	}
	public void GetAllianceForumPosts(final long forumID, final long threadID, final GetForumPostCallback cb) {
		post(new Runnable(){
			@Override public void run() {
				JSONObject obj = new JSONObject();
				try {
					obj.put("forumID", forumID);
					obj.put("threadID", threadID);
					doRPC("GetAllianceForumPosts",obj,RPC.this,new RPCCallback() {
						@Override void requestDone(rpcreply r) throws JSONException {
							JSONArray a = (JSONArray) r.reply;
							int i;
							final ForumPost[] out = new ForumPost[a.length()];
							for (i=0; i<a.length(); i++) {
								JSONObject o = a.getJSONObject(i);
								out[i] = new ForumPost(o);
							}
							runOnUiThread(new Runnable() {
								@Override public void run() {
									cb.done(out);
								}
							});
						}
					},5);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	public interface GetForumPostCallback {
		void done(ForumPost[] out);
	}
	public void CreateAllianceForumThread(final long forumID, final String title, final String message,final RPCDone callback) {
		post(new Runnable(){
			@Override public void run() {
				JSONObject obj = new JSONObject();
				try {
					obj.put("forumID", forumID);
					obj.put("threadTitle", title);
					obj.put("firstPostMessage", message);
					doRPC("CreateAllianceForumThread",obj,RPC.this,new RPCCallback(){
						@Override
						void requestDone(rpcreply r) {
							boolean reply = (Boolean) r.reply;
							Log.v(TAG,"post made, reply: "+reply);
							callback.requestDone(null);
						}},5);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	public void CreateAllianceForumPost(final long forumID, final long threadID,
			final String message,final RPCDone callback) {
		post(new Runnable(){
			@Override public void run() {
				JSONObject obj = new JSONObject();
				try {
					obj.put("forumID", forumID);
					obj.put("threadID", threadID);
					obj.put("postMessage", message);
					Log.v(TAG,"CreateAllianceForumPost:"+obj.toString());
					doRPC("CreateAllianceForumPost",obj,RPC.this,new RPCCallback(){
						@Override
						void requestDone(rpcreply r) {
							boolean reply = (Boolean) r.reply;
							Log.v(TAG,"post made, reply: "+reply);
							callback.requestDone(null);
						}},5);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	public void GetPublicCityInfo(final int cityid, final GotPublicCityInfo callback) {
		post(new Runnable() {
			public void run() {
				JSONObject obj = new JSONObject();
				try {
					obj.put("id", cityid);
					doRPC("GetPublicCityInfo",obj,RPC.this,new RPCCallback() {
						void requestDone(rpcreply r) {
							Log.v(TAG,r.reply.toString());
							final PublicCityInfo p = new PublicCityInfo(world,(JSONObject) r.reply);
							runOnUiThread(new Runnable() {
								public void run() {
									callback.done(p);
								}
							});
						}
					},5);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	public interface GotPublicCityInfo {
		void done(PublicCityInfo p);
	}
	public void GetOrderTargetInfo(final City currentCity, final int x, final int y, final GotOrderTargetInfo callback) {
		post(new Runnable() {
			public void run() {
				JSONObject obj = new JSONObject();
				try {
					obj.put("cityid", currentCity.cityid);
					obj.put("x", x);
					obj.put("y", y);
					doRPC("GetOrderTargetInfo",obj,RPC.this,new RPCCallback() {
						void requestDone(rpcreply r) throws JSONException {
							Log.v(TAG,r.reply.toString());
							final OrderTargetInfo p = new OrderTargetInfo(world,(JSONObject) r.reply);
							runOnUiThread(new Runnable() {
								public void run() {
									callback.done(p);
								}
							});
						}
					},5);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	public void TradeDirect(final City currentCity, final int[] resources, final boolean byland,
			final String targetPlayer, final String targetcity, final boolean palaceSupport, final TradeDirectDone callback) {
		post(new Runnable() {
			public void run() {
				JSONObject obj = new JSONObject();
				try {
					obj.put("cityid", currentCity.cityid);
					int i;
					JSONArray r = new JSONArray();
					for (i=1; i<5; i++) {
						if (resources[i-1] > 0) {
							JSONObject o = new JSONObject();
							o.put("t", i);
							o.put("c", resources[i-1]);
							r.put(o);
						}
					}
					obj.put("res", r);
					obj.put("tradeTransportType", byland ? 1 : 2);
					obj.put("targetPlayer", targetPlayer);
					obj.put("targetCity", targetcity);
					obj.put("palaceSupport", palaceSupport);
					doRPC("TradeDirect",obj,RPC.this,new RPCCallback() {
						void requestDone(rpcreply r) throws JSONException {
							final int reply = (Integer) r.reply;
							runOnUiThread(new Runnable() {
								public void run() {
									callback.done(reply);
								}
							});
						}
					},5);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	public interface TradeDirectDone {
		void done(int reply);
	}
	public interface GotOrderTargetInfo {
		void done(OrderTargetInfo p);
	}
}
