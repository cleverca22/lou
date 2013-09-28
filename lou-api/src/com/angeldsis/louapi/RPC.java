package com.angeldsis.louapi;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.json2.JSONArray;
import org.json2.JSONException;
import org.json2.JSONObject;
import org.json2.JSONTokener;

import com.angeldsis.louapi.HttpUtil.HttpReply;
import com.angeldsis.louapi.LouState.City;
import com.angeldsis.louapi.data.AllianceForum;
import com.angeldsis.louapi.data.Coord;
import com.angeldsis.louapi.data.ForumPost;
import com.angeldsis.louapi.data.ForumThread;
import com.angeldsis.louapi.data.OrderTargetInfo;
import com.angeldsis.louapi.data.PublicCityInfo;
import com.angeldsis.louapi.data.SubRequest;
import com.angeldsis.louapi.data.World;
import com.angeldsis.louapi.world.WorldParser;
import com.angeldsis.louapi.world.WorldParser.WorldCallbacks;

public abstract class RPC extends Thread implements WorldCallbacks {
	private static final String TAG = "RPC";
	public Account account;
	String instanceid;
	String urlbase;
	public LouState state;
	int requestid;
	boolean cont,polling,running;
	private ArrayList<String> chat_queue;
	private DelayQueue<MyTimer> queue;
	AllianceAttackMonitor aam;
	Poller poller;
	private boolean needWorldParser;
	public WorldParser worldParser;
	World world = new World();
	public BuildQueueParser buildQueueParser;
	DefenseOverviewParser defenseOverviewParser;
	public EnlightenedCities enlightenedCities;
	public FoodWarningParser foodWarnings;
	private HttpUtil httpUtil;
	public boolean passive;
	private boolean needBuildQueueParser;
	Runnable ticker = new Runnable () {
		public void run() {
			tick();
		}
	};
	private QuestTracker questTracker;

	public RPC(Account acct, LouState state,HttpUtil httpUtil) {
		this.httpUtil = httpUtil;
		this.account = acct;
		this.state = state;
		state.TAG = "LouState"+acct.worldid;
		aam = new AllianceAttackMonitor(this);
		enlightenedCities = new EnlightenedCities();
		foodWarnings = new FoodWarningParser();
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
		passive = !reset;
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
					obj.put("cityid",v.getCity().location.toCityId());
					obj.put("buildingid", v.visId);
					doRPC("GetBuildingInfo",obj,new RPCCallback() {
						@Override
						void requestDone(rpcreply r) throws JSONException,
								Exception {
							callback.requestDone((JSONObject) r.reply);
						}
					},5);
				} catch (JSONException e) {
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
					doRPC("GetLockboxURL",obj,new RPCCallback() {
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
					obj.put("cityid",v.getCity().location.toCityId());
					obj.put("buildingid", v.visId);
					doRPC("DemolishBuilding",obj,new RPCCallback() {
						@Override
						void requestDone(rpcreply r) throws JSONException,
								Exception {
							callback.requestDone((JSONObject) r.reply);
						}
					},5);
				} catch (JSONException e) {
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
					doRPC("ReportGetHeader",obj,new RPCCallback() {
						@Override
						void requestDone(rpcreply r) throws JSONException,
								Exception {
							JSONArray headers = (JSONArray) r.reply;
							final ReportHeader[] list = new ReportHeader[headers.length()]; // FIXME NullPointerException
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
					e.printStackTrace();
				}
			}
		});
	}
	public void SubstitutionAcceptReq(final SubRequest sr) {
		post(new Runnable() {
			public void run() {
				try {
					JSONObject obj = new JSONObject();
					obj.put("id", sr.id);
					obj.put("pid", sr.giver.getId());
					doRPC("SubstitutionAcceptReq",obj,new RPCCallback() {
						public void requestDone(rpcreply r) {
							Log.v(TAG,r.reply.toString());
						}
					},5);
				} catch (JSONException e) {
					e.printStackTrace();
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
					doRPC("SubstitutionCancleReq",obj,new RPCCallback() {
						public void requestDone(rpcreply r) {
							Log.v(TAG,r.reply.toString());
						}
					},5);
				} catch (JSONException e) {
					e.printStackTrace();
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
					doRPC("SubstitutionCreateReq",obj,new RPCCallback() {
						public void requestDone(rpcreply r) {
							Log.v(TAG,r.reply.toString());
						}
					},5);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	// FIXME dup?
	public void SubstitutionCancelReq(final SubRequest s) {
		post(new Runnable() {
			public void run() {
				try {
					JSONObject obj = new JSONObject();
					obj.put("id", s.id);
					obj.put("pid", s.receiver.getId());
					doRPC("SubstitutionCancelReq",obj,new RPCCallback() {
						public void requestDone(rpcreply r) {
							Log.v(TAG,r.reply.toString());
						}
					},5);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	public void CreateSubstitutionSession(final SubRequest s, final SubRequestDone cb) {
		post(new Runnable() {
			public void run() {
				try {
					JSONObject obj = new JSONObject();
					obj.put("id", s.id);
					obj.put("pid", s.giver.getId());
					doRPC("CreateSubstitutionSession",obj,new RPCCallback() {
						public void requestDone(final rpcreply r) {
							Log.v(TAG,r.reply.toString());
							runOnUiThread(new Runnable() {
								public void run() {
									String sessionid = (String) r.reply;
									startSubstituteSession(sessionid,s.giver.getId(),cb);
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
	public interface SubRequestDone {
		void allDone(Account acct2);
	};
	public abstract void startSubstituteSession(String sessionid,int playerid, SubRequestDone cb);
	public void GetReport(final int reportid,final ReportCallback cb) {
		post(new Runnable() {
			public void run() {
				try {
					JSONObject obj = new JSONObject();
					obj.put("id", reportid);
					doRPC("GetReport",obj,new RPCCallback() {
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
					doRPC("GetSharedReport",obj,new RPCCallback() {
						@Override
						void requestDone(rpcreply r) throws JSONException,
								Exception {
							final Report rr = new Report((JSONObject) r.reply);
							runOnUiThread(new Runnable() {
								@Override public void run() {
									cb.done(rr);
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
	public void UpgradeBuilding(final City c, final int coord, final int structureid, final UpgradeStarted cb) {
		post(new Runnable() {
			@Override
			public void run() {
				try {
					JSONObject obj = new JSONObject();
					obj.put("cityid", c.location.toCityId());
					obj.put("buildingid", coord);
					obj.put("buildingType", structureid);
					obj.put("isPaid", true);
					doRPC("UpgradeBuilding",obj,new RPCCallback() {
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
					obj.put("cityid", c.location.toCityId());
					doRPC("GetBuildingUpgradeInfo",obj,new RPCCallback() {
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
			onEjected("OPENFAIL");
			// FIXME, better error msg
			return;
		}
		JSONObject obj = new JSONObject();
		try {
			obj.put("reset", reset);
			doRPC("OpenSession",obj,new RPCCallback() {
				@Override
				void requestDone(rpcreply reply) throws JSONException,Exception {
					Log.v(TAG,account.sessionid+" http code:"+reply.http_code);
					JSONObject r2 = (JSONObject) reply.reply;
					int r = r2.getInt("r");
					Log.v(TAG,r2.toString(1));
					instanceid = r2.getString("i");
					if ("00000000-0000-0000-0000-000000000000".equals(instanceid)) {
						// FIXME, inform the user of the failure
						// FIXME, ask ea what exactly causes this failure?
						onEjected("OPENREJECT");
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
			e.printStackTrace();
		}
	}
	public void GetServerInfo(final RPCDone rpcDone) {
		JSONObject obj = new JSONObject();
		try {
			obj.put("time", System.currentTimeMillis());
			doRPC("GetServerInfo",obj,new RPCCallback() {
				void requestDone(rpcreply reply) throws JSONException {
					JSONObject r = (JSONObject) reply.reply;
					if (r == null) throw new IllegalStateException("unexpected null code:"+reply.http_code);
					state.parseServerInfo(r);
					rpcDone.requestDone(r);
				}
			},5);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	private void doRPC(final String function,final JSONObject request, final RPCCallback rpcCallback, final int retry) throws JSONException {
		if (retry == 0) {
			System.out.println("too many tryies");
			// FIXME, disconnect the user?
			return;
		}
		setThreadActive(true);

		if (function == "OpenSession") request.put("session", account.sessionid);
		else request.put("session", instanceid);
		byte[] raw_data = request.toString().getBytes();
		final int requestsize = raw_data.length;

		final long netstart = System.currentTimeMillis();
		try {
			HttpReply reply1 = httpUtil.postUrl(urlbase + function,raw_data);
			if (reply1.e != null) {
				Log.v(TAG, "exception is "+reply1.e);
				Log.e(TAG,"exception when doing rpc call, retrying",reply1.e);
				doRPC(function,request,rpcCallback,retry - 1);
				return;
				//throw new IllegalStateException("unexpected error",reply1.e);
			}
			long netstop = System.currentTimeMillis();
			rpcreply reply2 = new rpcreply();
			reply2.http_code = reply1.code;
			int networktime = (int) (netstop - netstart);
			long start = System.currentTimeMillis();
			if (reply1.contentLength == 0) {
				reply2.raw_reply = null;
			} else {
				try {
					// averaging 100-200ms per call
					reply2.reply = new JSONTokener(new InputStreamReader(reply1.stream)).nextValue();
					//Log.v(TAG, String.format("parsing took %dms",end-start));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					reply2.raw_reply = reply1.stream;
				}
			}
			long end = System.currentTimeMillis();
			logRequest(requestsize,reply1.contentLength,function,networktime,(int)(end-start));
			try {
				rpcCallback.requestDone(reply2);
			} catch (JSONException e) {
				e.printStackTrace();
				throw new IllegalStateException("unexpected exception ",e);
			} catch (Exception e) {
				e.printStackTrace();
				throw new IllegalStateException("unexpected exception ",e);
			}
		/*} catch (MalformedURLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "wtf, file not found?? " + urlbase + function);
			stopPolling();
			onEjected();
			stopLooping();
			return;*/
		} catch (DnsError e) {
			Log.w(TAG,"dns error, retrying "+urlbase);
			try {
				doRPC(function,request,rpcCallback,retry - 1);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		} catch (TimeoutError e) {
			Log.e(TAG, function + " exception from http req, retrying "+retry+" more times",e);
			try {
				doRPC(function,request,rpcCallback,retry - 1);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
	}
	public void logRequest(int req,int reply,String func, int networktime, int parse1) {
		//Log.v(TAG,String.format("SIZE %s(%d) == %d",func,req,reply));
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
			doRPC("GetPlayerInfo",obj,new RPCCallback () {
				@Override
				void requestDone(rpcreply r) throws JSONException, Exception {
					try {
					state.processPlayerInfo((JSONObject) r.reply);
					rpcDone.requestDone((JSONObject) r.reply);
					} catch (NullPointerException e) {
						Log.e(TAG,r.reply.toString());
						Log.e(TAG, "internal error",e);
						RPC.this.onEjected("GETFAIL");
					}
					runOnUiThread(new Runnable() {
						public void run() {
							cityListChanged();
							onCityChanged();
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
					doRPC("GetAllianceForums",obj,new RPCCallback() {
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
	public abstract void onCityChanged();
	public abstract void cityListChanged();
	public void Poll() {
		//Log.v(TAG,"Poll");
		try {
			JSONObject obj = new JSONObject();
			obj.put("requestid", requestid);
			requestid++;
			City c = state.currentCity;
			ArrayList<String> requests = new ArrayList<String>();
			if (!passive) {
				if (c != null) {
					requests.add("CITY:"+c.location.toCityId());
					if (state.fetchVis) {
						requests.add("VIS:c:"+state.currentCity.location.toCityId()+":0:-1085:-638:775:565:"+state.currentCity.visreset); // FIXME last field is reset, check webfrontend.vis.Main.js for others
					}
				}
				if (chat_queue.size() > 0) {
					String msg = chat_queue.remove(0);
					requests.add("CHAT:"+msg);
					if (chat_queue.size() > 0) {
						Log.v(TAG,"need to poll again");
						queue.remove(poller);
						poller.pollSoon();
						queue.add(poller);
					}
				} else requests.add("CHAT:");
				requests.add("REPORT:");
				requests.add("SERVER:");
				requests.add("SUBSTITUTION:");
				requests.add("PLAYER:"+ (state.getFullPlayerData ? "a" : ""));
				requests.add("QUEST:");
			}
			if (enlightenedCities != null) {
				requests.add("ECO:"+enlightenedCities.getRequestDetails());
			}
			requests.add("TIME:"+System.currentTimeMillis());
			if (checkAlliance()) requests.add("ALLIANCE:");
			if (state.userActivity) {
				state.userActivity = false;
				requests.add("UA:");
			}
			if ((worldParser != null) && worldParser.isEnabled()) {
				requests.add("WORLD:"+worldParser.getRequestDetails());
			}
			if (foodWarnings != null) {
				requests.add("FOODO:"+foodWarnings.getRequestDetails());
			}
			aam.getRequestDetails(requests);
			requests.add("TE:");
			if (needBuildQueueParser && (buildQueueParser != null)) {
				requests.add("BQO:"+buildQueueParser.getRequestDetails());
			}
			if (defenseOverviewParser != null) requests.add("DEFO:"+defenseOverviewParser.getRequestDetails());
			
			// FIXME
			StringBuilder b = new StringBuilder();
			Iterator<String> i = requests.iterator();
			String s = i.next();
			b.append(s);
			while (i.hasNext()) {
				s = i.next();
				b.append("\f");
				b.append(s);
			}
			obj.put("requests",b.toString());
			doRPC("Poll",obj,new RPCCallback() {
				void requestDone(rpcreply r) throws JSONException {
					int x;
					if (r.reply == null) return;
					JSONArray reply = (JSONArray) r.reply;
					for (x = 0; x < reply.length(); x++) {
						JSONObject obj = (JSONObject) reply.get(x);
						// FIXME, disable for production
						int reply_size = obj.toString().length();
						String C = obj.getString("C");
						RPC.this.logPollRequest(C,reply_size);
						handlePollPacket(C,obj);
					}
				}
			},10);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void logPollRequest(String c, int reply_size) {
	}
	private boolean checkAlliance() {
		if (state.checkOnline) return state.checkOnline;
		if (aam.alwaysMonitor) return aam.alwaysMonitor;
		return uiActive();
	}
	public abstract boolean uiActive();
	void handlePollPacket(String C, JSONObject p) throws JSONException {
		boolean showName = true;
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
		} else if (C.equals("ECO")) {
			if (enlightenedCities != null) enlightenedCities.parse(p,this);
		} else if (C.equals("FOODO")) {
			if (foodWarnings != null) foodWarnings.parse(p,this);
		} else if (C.equals("BQO")) {
			if (buildQueueParser != null) buildQueueParser.parse(p.getJSONArray("D"),this);
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
			state.processCityPacket(D,world);
			runOnUiThread(new Runnable () {public void run() {
				gotCityData();
			}});
		} else if (C.equals("CHAT")) {
			showName = false;
			JSONArray D = p.getJSONArray("D");
			int i;
			final ArrayList<ChatMsg> recent = new ArrayList<ChatMsg>();
			for (i = 0; i < D.length(); i++) {
				recent.add(new ChatMsg(D.getJSONObject(i)));
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
			final String D = p.getString("D");
			if (D.equals("CLOSED")) {
				this.stopPolling();
				runOnUiThread(new Runnable() {
					public void run () {
						onEjected(D);
					}
				});
				stopLooping();
			} else if (D.equals("GAMEOVER")) {
				this.stopPolling();
				runOnUiThread(new Runnable() {
					public void run () {
						onEjected(D);
					}
				});
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
			state.parseAllianceUpdate(D);
			showName = false;
		} else if (C.equals("ALL_AT")) {
			aam.parseReply(p.optJSONObject("D"));
		} else if (C.equals("SUBSTITUTION")) {
			JSONObject D = p.optJSONObject("D");
			state.parseSubs(D);
		} else if (C.equals("TE")) {
			JSONObject D = p.getJSONObject("D");
			//Log.v(TAG,"TE: packet "+D.toString());
		} else if (C.equals("DEFO")) {
			if (defenseOverviewParser != null) defenseOverviewParser.parse(p.getJSONArray("D"),this);
		} else if (C.equals("QUEST")) {
			JSONObject D = p.getJSONObject("D");
			if (questTracker == null) questTracker = new QuestTracker();
			questTracker.update(D);
		} else {
			Log.v(TAG,"unexpected Poll data "+C+" "+p.toString());
		}
		//if (showName) Log.v(TAG,"Poll packet "+C+" size: "+p.toString().getBytes().length);
	}
	public abstract void onReportCountUpdate();
	public abstract void onNewAttack(IncomingAttack a);
	/** called when the session is ended, usually by logging in elsewhere 
	 * @param string **/
	abstract public void onEjected(String string);
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
	 * also includes purified res
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
				e.printStackTrace();
			}
		}
	}
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
			long start=0,mindelay=0,maxdelay=0;
			try {
				t = queue.peek();
				mindelay = maxdelay = 60000;
				if (t != null) {
					mindelay = t.getDelay(TimeUnit.MILLISECONDS);
					maxdelay = t.maxtarget - System.currentTimeMillis();
				}
				else {
					Log.v(TAG, "no work found, maybe thread should stop?");
				}
				if (mindelay < 0) mindelay = 100;
				//Log.v(TAG,"delay "+maxdelay);
				setTimer(maxdelay); // sets the android alarm manager, to wake the device from sleep mode, up to max-min ms late
				setThreadActive(false);
				start = System.currentTimeMillis();
				Thread.sleep(mindelay);
			} catch (InterruptedException e) {
				Log.v(TAG,"who woke me!");
			}
			long end = System.currentTimeMillis();
			long overrun = (end-start)-mindelay;
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
	protected abstract Timeout getMaxPoll();
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
		interrupt();
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
		long mintarget;
		long maxtarget;
		public MyTimer() {}
		public MyTimer(Runnable r) {
			this.r = r;
			mintarget = maxtarget = 0;
		}
		public void setDelay(Timeout maxPoll) {
			mintarget = System.currentTimeMillis() + maxPoll.min;
			maxtarget = System.currentTimeMillis() + maxPoll.max;
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
			return arg0.convert(mintarget - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
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
			mintarget = maxtarget = 0;
		}
		public void pollSoon() {
			long timepassed = System.currentTimeMillis() - lastpoll;
			if (timepassed > 2000) mintarget = maxtarget = System.currentTimeMillis();
			else mintarget = maxtarget = System.currentTimeMillis() + (2000 - timepassed);
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
					doRPC("GetAllianceForumThreads", obj,  new RPCCallback(){
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
					doRPC("GetAllianceForumPosts",obj,new RPCCallback() {
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
					doRPC("CreateAllianceForumThread",obj,new RPCCallback(){
						@Override
						void requestDone(rpcreply r) {
							boolean reply = (Boolean) r.reply;
							Log.v(TAG,"post made, reply: "+reply);
							callback.requestDone(null);
						}},5);
				} catch (JSONException e) {
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
					doRPC("CreateAllianceForumPost",obj,new RPCCallback(){
						@Override
						void requestDone(rpcreply r) {
							boolean reply = (Boolean) r.reply;
							Log.v(TAG,"post made, reply: "+reply);
							callback.requestDone(null);
						}},5);
				} catch (JSONException e) {
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
					doRPC("GetPublicCityInfo",obj,new RPCCallback() {
						void requestDone(rpcreply r) {
							Log.v(TAG,"GetPublicCityInfo reply:"+r.reply);
							final PublicCityInfo p = new PublicCityInfo(world,(JSONObject) r.reply);
							runOnUiThread(new Runnable() {
								public void run() {
									callback.done(p);
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
	public interface GotPublicCityInfo {
		void done(PublicCityInfo p);
	}
	public void GetOrderTargetInfo(final City currentCity, final int x, final int y, final GotOrderTargetInfo callback) {
		post(new Runnable() {
			public void run() {
				JSONObject obj = new JSONObject();
				try {
					obj.put("cityid", currentCity.location.toCityId());
					obj.put("x", x);
					obj.put("y", y);
					doRPC("GetOrderTargetInfo",obj,new RPCCallback() {
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
					e.printStackTrace();
				}
			}
		});
	}
	public void TradeDirect(final City currentCity, final int[] resources, final boolean byland,
			final String targetPlayer, final Coord coord, final boolean palaceSupport, final TradeDirectDone callback) {
		post(new Runnable() {
			public void run() {
				JSONObject obj = new JSONObject();
				try {
					obj.put("cityid", currentCity.location.toCityId());
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
					obj.put("targetCity", coord.format());
					obj.put("palaceSupport", palaceSupport);
					doRPC("TradeDirect",obj,new RPCCallback() {
						void requestDone(rpcreply r) throws JSONException {
							final int reply = (Integer) r.reply;
							runOnUiThread(new Runnable() {
								public void run() {
									callback.done(reply);
								}
							});
							pollSoon();
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
	public abstract void onBuildQueueUpdate();
	public void setBuildQueueWatching(boolean b) {
		if (b) {
			if (buildQueueParser == null) buildQueueParser = new BuildQueueParser(state);
			needBuildQueueParser = true;
			pollSoon();
		}
		else needBuildQueueParser = false;
	}
	public void BuildingQueuePayAll(int id) {
		bqoMethod("BuildingQueuePayAll",id);
	}
	public void QueueMinisterBuildOrder(int id) {
		bqoMethod("QueueMinisterBuildOrder",id);
	}
	public void BuildingQueueFill(int id) {
		bqoMethod("BuildingQueueFill",id);
	}
	private void bqoMethod(final String method, final int id) {
		post(new Runnable() {
			public void run() {
				JSONObject obj = new JSONObject();
				try {
					obj.put("cityid", id);
					doRPC(method,obj,new RPCCallback() {
						void requestDone(rpcreply r) throws JSONException {
							Log.v(TAG,r.reply.toString());
							pollSoon();
						}
					},5);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	public void setWorldEnabled(boolean b) {
		if (b) {
			if (worldParser == null) {
				Log.v(TAG,"making new world parser");
				worldParser = new WorldParser(this);
			}
			needWorldParser = true;
		} else {
			Log.v(TAG,"saving world parser for later");
			needWorldParser = false;
		}
	}
	public void onTrimMemory() {
		if (!needWorldParser && (worldParser != null)) {
			Log.v(TAG,"releasing world parsr");
			worldParser = null;
		}
		if (!needBuildQueueParser && (buildQueueParser != null)) {
			Log.v(TAG,"releasing bqo parser");
			buildQueueParser = null;
		}
	}
	public static class NewOrder {
		public int repeat;
		public NewOrder() {
			repeat = 1;
		}
	}
	public void OrderUnits(final City city, final JSONArray units, final Coord target, final int raidTimeReferenceType, final OrderUnitsCallback cb, final NewOrder order) {
		post(new Runnable() {
			public void run() {
				JSONObject obj = new JSONObject();
				try {
					obj.put("cityid", city.location.toCityId());
					obj.put("units", units);
					obj.put("targetPlayer", "");
					obj.put("targetCity", target.format());
					obj.put("order", 8);
					obj.put("transport", 1);
					obj.put("createCity", "");
					obj.put("timeReferenceType", 1);
					obj.put("referenceTimeUTCMillis",0);
					obj.put("raidTimeReferenceType",raidTimeReferenceType); // 0normal, 1 repeat until done
					obj.put("raidReferenceTimeUTCMillis", 0);
					obj.put("iUnitOrderOptions", 0);
					obj.put("iOrderCountRaid", order.repeat);
					Log.v(TAG,"OrderUnits:"+obj.toString());
					doRPC("OrderUnits",obj,new RPCCallback() {
						void requestDone(rpcreply r) throws JSONException {
							Log.v(TAG,r.reply.toString());
							pollSoon();
							JSONObject r2 = (JSONObject) r.reply;
							final int r0 = r2.getInt("r0");
							final int r1 = r2.getInt("r1");
							runOnUiThread(new Runnable() {
								public void run() {
									cb.done(r0,r1);
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
	public interface OrderUnitsCallback {

		void done(int r0, int r1);}
	public void setDefenseOverviewEnabled(boolean b) {
		if (b) {
			Log.v(TAG,"DEFO enabled!");
			defenseOverviewParser = new DefenseOverviewParser();
			pollSoon();
		} else {
			defenseOverviewParser = null;
		}
	}
	public abstract void onDefenseOverviewUpdate();
	public abstract void onEnlightenedCityChanged();
	public abstract void onFoodWarning();
	public void ResourceToVoid(final City city, final JSONArray counts) {
		post(new Runnable() {
			public void run() {
				JSONObject obj = new JSONObject();
				try {
					obj.put("cityid", city.location.toCityId());
					obj.put("res",counts);
					Log.v(TAG,obj.toString());
					doRPC("ResourceToVoid",obj,new RPCCallback() {
						@Override void requestDone(rpcreply r) throws JSONException, Exception {
							Log.v(TAG,r.reply.toString()); // FIXME, check value
							pollSoon();
						}
					},5);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	public void IGMGetFolders(final MailBoxCallback mailBox) {
		// FIXME, remember the results
		post(new Runnable() {
			public void run() {
				JSONObject obj = new JSONObject();
				try {
					doRPC("IGMGetFolders",obj,new RPCCallback() {
						@Override void requestDone(rpcreply r) throws JSONException, Exception {
							JSONArray mailboxes = (JSONArray) r.reply;
							int x;
							MailBoxFolder[] folders = new MailBoxFolder[mailboxes.length()];
							for (x=0; x<mailboxes.length(); x++) {
								JSONObject boxin = mailboxes.getJSONObject(x);
								folders[x] = new MailBoxFolder(boxin);
							}
							mailBox.done(folders);
						}
					},5);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	public void IGMGetMsgCount(final MailBoxFolder folder,final MessageCountCallback cb) {
		post(new Runnable() {
			public void run() {
				JSONObject obj = new JSONObject();
				try {
					obj.put("folder", folder.id);
					doRPC("IGMGetMsgCount",obj,new RPCCallback() {
						@Override void requestDone(rpcreply r) throws JSONException, Exception {
							cb.gotCount((Integer)r.reply,folder);
						}
					},5);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	/**
	 * start and end are inclusive
	 * @param start
	 * @param end
	 * @param folder
	 * @param sort
	 * @param ascending
	 * @param direction
	 * @param cb
	 */
	public void IGMGetMsgHeader(final int start, final int end, final MailBoxFolder folder, final int sort,
			final boolean ascending, final boolean direction, final MessageHeaderCallback cb) {
		post(new Runnable() {
			public void run() {
				JSONObject obj = new JSONObject();
				try {
					obj.put("folder", folder.id);
					obj.put("start", start);
					obj.put("end", end);
					obj.put("sort",sort);
					obj.put("ascending",ascending);
					obj.put("direction",direction);
					doRPC("IGMGetMsgHeader",obj,new RPCCallback() {
						@Override void requestDone(rpcreply r) throws JSONException, Exception {
							JSONArray headers = (JSONArray) r.reply; // FIXME, use java objects
							int x;
							final MailHeader[] out = new MailHeader[headers.length()];
							for (x=0; x<headers.length(); x++) {
								out[x] = new MailHeader(headers.getJSONObject(x));
							}
							runOnUiThread(new Runnable() {
								public void run() {
									cb.gotHeaders(out);
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
	public void IGMGetMsg(final MailHeader mh, final GotMailMessage cb) {
		post(new Runnable() {
			public void run() {
				JSONObject obj = new JSONObject();
				try {
					obj.put("id", mh.id);
					doRPC("IGMGetMsg",obj,new RPCCallback() {
						void requestDone(final rpcreply r) {
							runOnUiThread(new Runnable() {
								public void run() {
									cb.gotMailMessage((String) r.reply);
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
	public void IGMBulkSendMsg(final String to, final String cc, final String subject, final String body) {
		post(new Runnable() {
			public void run() {
				JSONObject obj = new JSONObject();
				try {
					obj.put("targets", to);
					obj.put("ccTargets", cc);
					obj.put("subject", subject);
					obj.put("body", body);
					doRPC("IGMBulkSendMsg",obj,new RPCCallback() {
						void requestDone(final rpcreply r) {
							runOnUiThread(new Runnable() {
								public void run() {
									Log.v(TAG,"reply"+r.reply);
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
	public interface GotMailMessage {
		void gotMailMessage(String message);
	}
	public interface MailBoxCallback {
		void done(MailBoxFolder[] folders);
	}
	public interface MessageCountCallback {
		void gotCount(int count, MailBoxFolder folder);
	}
	public interface MessageHeaderCallback {
		void gotHeaders(MailHeader[] out);
	}
}
