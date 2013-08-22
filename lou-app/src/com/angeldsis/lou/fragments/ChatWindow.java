package com.angeldsis.lou.fragments;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.angeldsis.lou.AccountWrap;
import com.angeldsis.lou.BBCode;
import com.angeldsis.lou.FragmentBase;
import com.angeldsis.lou.LouSessionMain;
import com.angeldsis.lou.R;
import com.angeldsis.lou.SessionKeeper;
import com.angeldsis.lou.SingleFragment;
import com.angeldsis.lou.BBCode.Span;
import com.angeldsis.lou.chat.ChatHistory;
import com.angeldsis.louapi.ChatMsg;

public class ChatWindow extends FragmentBase {
	//static final String TAG = "ChatWindow";
	private String TAG = "ChatWindow."+this.hashCode();
	private TabHost mTabHost;
	enum Type { pm,channel };
	class Channel {
		Type type;
		//ScrollView scrollView;
		//LinearLayout oldmessagelist;
		String tag,key;
		public LinearLayout wrapper;
		public ListView newmessagelist;
		ChatHistoryAdapter adapter;
	}
	Map<String,Channel> channels;
	//Handler h = new Handler();
	//boolean posted = false;
	private ViewGroup topView;
	
	public void onCreate(Bundle b) {
		super.onCreate(b);
		Log.v(TAG,"onCreate");
		setHasOptionsMenu(true);
		//private_messages = new HashMap<String,PM>();
	}
	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		topView = (ViewGroup) inflater.inflate(R.layout.chat_window, container, false);
		topView.findViewById(R.id.sendMsg).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				ChatWindow.this.sendMsg(v);
			}});
		topView.findViewById(R.id.dingOnMsg).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				ChatWindow.this.dingClick(v);
			}
		});
		mTabHost = (TabHost)(topView.findViewById(R.id.tabhost));
		mTabHost.setup();
		channels = new HashMap<String,Channel>();
		
		TabHost.TabSpec t1 = mTabHost.newTabSpec("general");
		Channel temp = new Channel();
		temp.type = Type.channel;
		temp.tag = "general";
		temp.key = "@C";
		t1.setContent(new TabMaker(temp));
		t1.setIndicator("General");
		mTabHost.addTab(t1);
		channels.put("general",temp);
		
		temp = new Channel();
		temp.type = Type.channel;
		temp.tag = "alliance";
		temp.key = "@A";
		TabHost.TabSpec t2 = mTabHost.newTabSpec("@A");
		t2.setContent(new TabMaker(temp));
		t2.setIndicator("alliance");
		mTabHost.addTab(t2);
		channels.put("alliance", temp);
		
		temp = new Channel();
		temp.type = Type.channel;
		temp.tag = "officer";
		temp.key = "@O";
		TabHost.TabSpec t3 = mTabHost.newTabSpec("@O");
		t3.setContent(new TabMaker(temp));
		t3.setIndicator("officer");
		mTabHost.addTab(t3);
		channels.put("officer", temp);

		if (savedInstanceState != null) {
			Log.v(TAG,"b: "+savedInstanceState);
			final String tab = savedInstanceState.getString("currentTab");
			if (tab != null) {
				Log.v(TAG,"restoring tab '"+tab+"'");
				mTabHost.setCurrentTabByTag(tab);
				Log.v(TAG,mTabHost.getCurrentTabTag());
			}
		}
		Bundle b2 = parent.getIntent().getExtras();
		String tab2 = b2.getString("currentTab");
		if (tab2 != null) {
			Log.v(TAG,"restoring tab2 '"+tab2+"'");
			mTabHost.setCurrentTabByTag(tab2);
			Log.v(TAG,mTabHost.getCurrentTabTag());
		}
		return topView;
	}
	@Override public void onSaveInstanceState(Bundle out) {
		out.putString("currentTab", mTabHost.getCurrentTabTag());
		super.onSaveInstanceState(out);
	}
	class TabMaker implements TabHost.TabContentFactory {
		Channel c;
		public TabMaker(Channel channel) {
			c = channel;
			
			//c.oldmessagelist = new LinearLayout(ChatWindow.this);
			//c.oldmessagelist.setOrientation(LinearLayout.VERTICAL);
			
			c.newmessagelist = new ListView(getActivity());
			c.adapter = new ChatHistoryAdapter();
			c.newmessagelist.setAdapter(c.adapter);
			LinearLayout.LayoutParams l = new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.MATCH_PARENT,1);
			c.newmessagelist.setLayoutParams(l);
			c.newmessagelist.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
			c.newmessagelist.setStackFromBottom(true);
			
			//c.scrollView = new ScrollView(ChatWindow.this);
			//c.scrollView.addView(c.oldmessagelist);
			//l = new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.MATCH_PARENT,1);
			//c.scrollView.setLayoutParams(l);

			c.wrapper = new LinearLayout(getActivity());
			//c.wrapper.addView(c.scrollView);
			c.wrapper.addView(c.newmessagelist);
		}
		@Override
		public View createTabContent(String tag) {
			return c.wrapper;
		}
	}
	class ChatHistoryAdapter extends BaseAdapter {
		private ChatHistory source;
		private Channel channel;
		private Calendar c3;
		SessionKeeper.Session leakme; // FIXME, ugly hack
		int lastCount = 0;
		//SparseArray<ChatMsg> cache;
		Drawable crown_drawable = getResources().getDrawable(R.drawable.icon_lou_public_other_world);
		ChatHistoryAdapter() {
			crown_drawable.setBounds(0, 0, crown_drawable.getIntrinsicWidth(), crown_drawable.getIntrinsicHeight());
			//cache = new SparseArray<ChatMsg>();
		}
		@Override
		public int getCount() {
			return lastCount;
		}
		private int getRealCount() {
			if (channel == null) {
				Log.v(TAG,this.toString()+" getCount null");
				return 0;
			}
			int count = source.getCount(channel.key);
			//Log.v(TAG,"getCount == "+count);
			return count;
		}
		@Override
		public ChatMsg getItem(final int position) {
			/*ChatMsg c = cache.get(position);
			if (c == null) {
				new AsyncTask<Void,Void,ChatMsg>() {
					@Override protected ChatMsg doInBackground(Void... params) {
						cache.put(position,source.getItem(channel.key,position));
						synchronized(h) {
							if (posted) return null;
							posted = true;
							h.postDelayed(new Runnable() {
								@Override public void run() {
									synchronized(h) {
										ChatHistoryAdapter.this.notifyDataSetChanged();
										posted = false;
									}
								}
							}, 1000);
						}
						return null;
					}
				}.execute();
			}
			return c;*/
			return source.getItem(channel.key,position);
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// when scrolling, this only gets called for things coming into view, to re-make them
			// if notifyDataSetChanged is called, this is re-ran for all visible elements
			//long start = System.currentTimeMillis();
			SpannableStringBuilder b = new SpannableStringBuilder();
			ChatMsg c = getItem(position);
			//Log.v(TAG,String.format("%s getView(%d,%s,%s) c=%s",channel.key,position,convertView,parent,c));
			
			if (c != null) {
				ArrayList<Span> spans = new ArrayList<Span>();
				if ((leakme == null) && (ChatWindow.this.parent.session != null)) leakme = ChatWindow.this.parent.session;
				if (leakme == null) throw new IllegalStateException("session was null!");
				if (leakme.state == null) throw new IllegalStateException("session.state was null!");
				c3.setTime(new Date(c.ts));
				formatTime(c3,b);
				b.append(' ');
				int start,end,end1;
				
				if (c.channel == null) {
					b.append(c.sender);
					b.append(" ");
					BBCode.parse(ChatWindow.this.parent,c.message,b,spans);
				} else if (c.channel.equals("@A")) {
					//start = b.length();
					//String all = getString(R.string.alliance);
					//end = start + all.length();
					//b.append(all);
					int green = getResources().getColor(R.color.chat_green);
					//b.setSpan(new ForegroundColorSpan(green), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					
					if (c.hascrown) {
						start = b.length();
						b.append("\uFFFC");
						end = b.length();
						b.setSpan(new ImageSpan(crown_drawable), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
					start = b.length();
					b.append(c.sender);
					end1 = b.length();
					
					b.append(": ");
					
					BBCode.parse(ChatWindow.this.parent,c.message,b,spans);
					end = b.length();
					
					b.setSpan(new ForegroundColorSpan(green), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					b.setSpan(new NameClicked(c.sender), start, end1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				} else if (c.channel.equals("@O")) {
					start = b.length();
					String officer = "[Officer]"; // FIXME, translation
					end = start + officer.length();
					b.append(officer);
					int color = getResources().getColor(R.color.chat_officer);
					b.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					
					if (c.hascrown) {
						start = b.length();
						b.append("\uFFFC");
						end = b.length();
						b.setSpan(new ImageSpan(crown_drawable), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
					
					start = b.length();
					b.append(c.sender);
					end1 = b.length();
					
					b.append(": ");
					
					BBCode.parse(ChatWindow.this.parent,c.message,b,spans);
					end = b.length();
					
					b.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					b.setSpan(new NameClicked(c.sender), start, end1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				} else if (c.channel.equals("privatein") || c.channel.equals("privateout")) {
					if (c.hascrown) {
						start = b.length();
						b.append("\uFFFC");
						end = b.length();
						b.setSpan(new ImageSpan(crown_drawable), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
					
					if (c.channel.equals("privateout")) b.append(leakme.state.self.getName()+": ");
					else b.append(c.sender+": ");
					BBCode.parse(ChatWindow.this.parent,c.message,b,spans);
				}
				else {
					b.append(c.channel);
					b.append(' ');
					if (c.hascrown) {
						start = b.length();
						b.append("\uFFFC");
						end = b.length();
						b.setSpan(new ImageSpan(crown_drawable), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
					start = b.length();
					b.append(c.sender);
					end1 = b.length();
					if (c.sender.charAt(0) != '@') b.setSpan(new NameClicked(c.sender), start, end1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	
					b.append(": ");
					BBCode.parse(ChatWindow.this.parent,c.message,b,spans);
				}
				for (Span s : spans) s.apply(b);
			} else {
				b.append("loading...");
			}
			
			ViewGroup row = (ViewGroup) convertView;
			if (row == null) {
				LayoutInflater f = ChatWindow.this.parent.getLayoutInflater();
				row = (ViewGroup) f.inflate(R.layout.chat_row, parent,false);
			}
			TextView msg = (TextView) row.findViewById(R.id.msg);
			msg.setText(b);
			msg.setMovementMethod(LinkMovementMethod.getInstance());
			//long end = System.currentTimeMillis();
			//Log.v(TAG,"getView took "+(end-start)+"ms");
			return row;
		}
		public void setSource(ChatHistory chat,Channel c) {
			source = chat;
			channel = c;
			update();
			c3 = Calendar.getInstance(ChatWindow.this.parent.session.state.tz);
		}
		public void update() {
			lastCount = getRealCount();
			notifyDataSetChanged();
		}
	}
	void formatTime(Calendar c3, SpannableStringBuilder b) {
		// i think this improves performance over the format
		int t;
		//StringBuilder b = new StringBuilder(11);
		b.append('[');
		// FIXME, try to find something better then ""+
		t = c3.get(Calendar.HOUR_OF_DAY); if (t < 10) b.append('0'); b.append(""+t);b.append(':');
		t = c3.get(Calendar.MINUTE); if (t < 10) b.append('0'); b.append(""+t);b.append(':');
		t = c3.get(Calendar.SECOND); if (t < 10) b.append('0'); b.append(""+t);b.append(']');
		//return b.toString();
		//return String.format("[%02d:%02d:%02d] ",c3.get(Calendar.HOUR_OF_DAY),c3.get(Calendar.MINUTE),c3.get(Calendar.SECOND));
	}
	public void onStart() {
		super.onStart();
		Log.v(TAG,"onStart");
	}
	public void session_ready() {
		if (parent == null) throw new IllegalStateException("parent was null, wtf");
		if (parent.session == null) throw new IllegalStateException("session was null, wtf");
		((ToggleButton)(topView.findViewById(R.id.dingOnMsg))).setChecked(parent.session.dingOnMessage);
		for (Channel c : channels.values()) {
			//c.oldmessagelist.removeAllViews();
			c.adapter.setSource(parent.session.chat,c);
		}
		Log.v(TAG,"open tags");
		for (String tag : parent.session.chat.openTags) {
			Log.v(TAG,"reopening tag "+tag);
			String key;
			if (tag.equals("@C")) key = "general";
			else if (tag.equals("@A")) key = "alliance";
			else if (tag.startsWith("pm_")) key = tag;
			else {
				Log.v(TAG,"unknown tag "+tag);
				key = tag;
			}
			Channel c = channels.get(key);
			if (c == null) {
				Log.v(TAG,"tag "+tag+" not found");
				if (key.startsWith("pm_")) {
					c = new Channel();
					Log.v(TAG,"made channel");
					c.tag = key.substring(3);
					c.key = key;
					c.type = Type.pm;
					TabHost.TabSpec s = mTabHost.newTabSpec(c.tag);
					s.setContent(new TabMaker(c));
					c.adapter.setSource(parent.session.chat, c);
					Log.v(TAG,"source set");
					s.setIndicator(c.tag);
					mTabHost.addTab(s);
					channels.put(key, c);
				}
			}
		}
		parent.session.rpc.pollSoon();
	}
	class NameClicked extends ClickableSpan {
		String name;
		public NameClicked(String sender) {
			name = sender;
		}
		@Override
		public void onClick(View v) {
			Log.v(TAG,"onClick");
			String tag = name;
			Channel c = channels.get("pm_"+tag);
			if (c == null) {
				c = new Channel();
				c.tag = tag;
				c.type = Type.pm;
				c.key = "pm_"+tag;
				Log.v(TAG,"new key for pm is "+c.key);
				TabHost.TabSpec s = mTabHost.newTabSpec(c.tag);
				s.setContent(new TabMaker(c));
				c.adapter.setSource(parent.session.chat, c);
				s.setIndicator(c.tag);
				mTabHost.addTab(s);
				channels.put("pm_"+c.tag, c);
			}
		}
	}
	@Override public boolean onChat(ArrayList<ChatMsg> recent) {
		long start = System.currentTimeMillis();
		Iterator<ChatMsg> i = recent.iterator();
		Activity a = getActivity();
		while (i.hasNext()) {
			// FIXME, remove most of this code
			ChatMsg c = i.next();
			TextView channel = new TextView(a);
			TextView sender = new TextView(a);
			TextView msg = new TextView(a);
			LinearLayout l = new LinearLayout(a);
			sender.setText(c.sender);
			sender.setClickable(true);
			msg.setText(" "+c.message); // FIXME, padding
			Channel currentChannel = channels.get("general");
			
			if (c.channel.equals("@A")) currentChannel = channels.get("alliance");
			else if (c.channel.equals("@C")) currentChannel = channels.get("general");
			else if (c.channel.equals("@O")) currentChannel = channels.get("officer");
			else if (c.channel.equals("privatein") || c.channel.equals("privateout")) {
				currentChannel = channels.get("pm_"+c.sender);
				if (currentChannel == null) {
					currentChannel = new Channel();
					Log.v(TAG,"made channel");
					currentChannel.tag = c.sender;
					currentChannel.key = "pm_"+c.sender;
					currentChannel.type = Type.pm;
					TabHost.TabSpec s = mTabHost.newTabSpec(currentChannel.tag);
					s.setContent(new TabMaker(currentChannel));
					currentChannel.adapter.setSource(parent.session.chat, currentChannel);
					Log.v(TAG,"source set");
					s.setIndicator(currentChannel.tag);
					mTabHost.addTab(s);
					channels.put("pm_"+currentChannel.tag, currentChannel);
				}
			}
			
			currentChannel.adapter.update();
			
			l.addView(channel);
			if (c.hascrown) {
				ImageView crown = new ImageView(a);
				crown.setImageDrawable(this.getResources().getDrawable(R.drawable.icon_lou_public_other_world));
				l.addView(crown);
			}
			l.addView(sender);
			l.addView(msg);
			
			if (c.channel.equals("@A")) {
				continue;
			}
			else if (c.channel.equals("privatein") || c.channel.equals("privateout")) {
				Channel c2 = channels.get("pm_"+c.sender);
			}
		}
		long end = System.currentTimeMillis();
		Log.v(TAG,"onChat time: "+(end-start));
		return true;
	}
	public void sendMsg(View v) {
		String tag = mTabHost.getCurrentTabTag();
		EditText m = (EditText) (topView.findViewById(R.id.message));
		String buffer = m.getText().toString();
		if (buffer.length() == 0) return;
		if (buffer.charAt(0) == '/') {} 
		else if (tag.equals("@A")) buffer = "/a "+buffer;
		else if (tag.equals("@O")) buffer = "/o "+buffer;
		else if (channels.containsKey("pm_"+tag)) {
			buffer = "/whisper "+tag+" "+buffer;
		}
		parent.userActive();
		parent.session.rpc.QueueChat(buffer);
		m.setText("");
	}
	@Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		//super.onCreateOptionsMenu(menu,inflater);
		menu.removeItem(R.id.open_chat);
	}
	public boolean onOptionsItemSelected(MenuItem item) {
		// FIXME, remove this?
		Log.v(TAG,"click! "+item.getItemId());
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent i = LouSessionMain.getIntent(parent.acct, getActivity());
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	public void onPause() {
		super.onPause();
		Log.v(TAG,"onPause()");
	}
	public void onStop() {
		super.onStop();
		Log.v(TAG,"onStop()");
	}
	public void dingClick(View v) {
		parent.session.dingOnMessage = ((ToggleButton)v).isChecked();
	}
	private static final Uri uri = Uri.parse("loudroid://chat");
	public static Intent getIntent(AccountWrap acct, String tag, Context context) {
		Bundle args = acct.toBundle();
		args.putSerializable("fragment",ChatWindow.class);
		args.putString("currentTab", tag);
		args.putString("one", "one");
		Intent intent = new Intent(context,SingleFragment.class);
		intent.putExtras(args);
		//intent.setData(uri);
		return intent;
	}
}
