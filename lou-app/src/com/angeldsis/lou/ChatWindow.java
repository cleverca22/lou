package com.angeldsis.lou;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TextView;

import com.angeldsis.lou.chat.ChatHistory;
import com.angeldsis.louapi.ChatMsg;

public class ChatWindow extends SessionUser {
	static final String TAG = "ChatWindow";
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
	private NameClicked nameClicker;
	public void onCreate(Bundle b) {
		super.onCreate(b);
		if (Build.VERSION.SDK_INT > 13) initApi14();
		setContentView(R.layout.chat_window);
		//private_messages = new HashMap<String,PM>();
		channels = new HashMap<String,Channel>();
		
		mTabHost = (TabHost)findViewById(R.id.tabhost);
		mTabHost.setup();
		
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
		TabHost.TabSpec t2 = mTabHost.newTabSpec("alliance");
		t2.setContent(new TabMaker(temp));
		t2.setIndicator("alliance");
		mTabHost.addTab(t2);
		channels.put("alliance", temp);
		
		nameClicker = new NameClicked();
	}
	class TabMaker implements TabHost.TabContentFactory {
		Channel c;
		public TabMaker(Channel channel) {
			c = channel;
			
			//c.oldmessagelist = new LinearLayout(ChatWindow.this);
			//c.oldmessagelist.setOrientation(LinearLayout.VERTICAL);
			
			c.newmessagelist = new ListView(ChatWindow.this);
			c.adapter = new ChatHistoryAdapter();
			c.newmessagelist.setAdapter(c.adapter);
			LinearLayout.LayoutParams l = new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.MATCH_PARENT,1);
			c.newmessagelist.setLayoutParams(l);
			c.newmessagelist.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
			c.newmessagelist.setStackFromBottom(true);
			
			//c.scrollView = new ScrollView(ChatWindow.this);
			//c.scrollView.addView(c.oldmessagelist);
			//l = new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.MATCH_PARENT,1);
			//c.scrollView.setLayoutParams(l);

			c.wrapper = new LinearLayout(ChatWindow.this);
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
		@Override
		public int getCount() {
			if (channel == null) {
				Log.v(TAG,this.toString()+" getCount null");
				return 0;
			}
			int count = source.getCount(channel.key);
			//Log.v(TAG,"getCount == "+count);
			return count;
		}
		@Override
		public ChatMsg getItem(int position) {
			//long start = System.currentTimeMillis();
			ChatMsg c = source.getItem(channel.key,position);
			//long end = System.currentTimeMillis();
			//Log.v(TAG,"getItem took "+(end-start));
			return c;
		}
		@Override
		public long getItemId(int position) {
			Log.v(TAG,"getItemId");
			// TODO Auto-generated method stub
			return 0;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// when scrolling, this only gets called for things coming into view, to re-make them
			// if notifyDataSetChanged is called, this is re-ran for all visible elements
			//long start = System.currentTimeMillis();
			View returnme;
			ChatWindow context = ChatWindow.this;
			ChatMsg c = getItem(position);
			//Log.v(TAG,String.format("%s getView(%d,%s,%s) c=%s",channel.key,position,convertView,parent,c));
			TextView timestamp = new TextView(context);
			TextView channel = new TextView(context);
			TextView sender = new TextView(context);
			TextView msg = new TextView(context);
			LinearLayout l = new LinearLayout(context);
			
			if (session == null) throw new IllegalStateException("session was null!");
			if (session.state == null) throw new IllegalStateException("session.state was null!");
			Calendar c3 = Calendar.getInstance(session.state.tz);
			c3.setTime(new Date(c.ts));
			timestamp.setText(String.format("[%02d:%02d:%02d] ",c3.get(Calendar.HOUR_OF_DAY),c3.get(Calendar.MINUTE),c3.get(Calendar.SECOND)));
			l.addView(timestamp);
			
			sender.setText(c.sender);
			sender.setOnClickListener(nameClicker);
			sender.setClickable(true);
			msg.setText(" "+c.message); // FIXME, padding
			
			l.addView(channel);
			if (c.hascrown) {
				ImageView crown = new ImageView(context);
				crown.setImageDrawable(context.getResources().getDrawable(R.drawable.icon_lou_public_other_world));
				l.addView(crown);
			}
			l.addView(sender);
			l.addView(msg);
			
			if (c.channel == null) {
				returnme = l;
			} else if (c.channel.equals("@A")) {
				channel.setText(getString(R.string.alliance));
				int green = context.getResources().getColor(R.color.chat_green);
				channel.setTextColor(green);
				sender.setTextColor(green);
				msg.setTextColor(green);
				returnme = l;
			}
			else if (c.channel.equals("privatein") || c.channel.equals("privateout")) {
				if (c.channel.equals("privateout")) sender.setText(session.state.self.getName()+": ");
				else sender.setText(c.sender+": ");
				l.removeView(channel);
				returnme = l;
			}
			else {
				channel.setText(c.channel);
				returnme = l;
			}
			//long end = System.currentTimeMillis();
			//Log.v(TAG,"getView took "+(end-start)+"ms");
			return returnme;
		}
		public void setSource(ChatHistory chat,Channel c) {
			Log.v(TAG,this.toString()+" setSource");
			source = chat;
			channel = c;
			this.notifyDataSetChanged();
		}
	}
	public void onStart() {
		super.onStart();
	}
	public void session_ready() {
		for (Channel c : channels.values()) {
			//c.oldmessagelist.removeAllViews();
			c.adapter.setSource(session.chat,c);
		}
		onChat(session.state.chat_history);
	}
	class NameClicked implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			TextView sender = (TextView) v;
			String tag = sender.getText().toString();
			Channel c = channels.get("pm_"+tag);
			if (c == null) {
				c = new Channel();
				c.tag = tag;
				c.type = Type.pm;
				c.key = "pm_"+tag;
				Log.v(TAG,"new key for pm is "+c.key);
				TabHost.TabSpec s = mTabHost.newTabSpec(c.tag);
				s.setContent(new TabMaker(c));
				c.adapter.setSource(session.chat, c);
				s.setIndicator(c.tag);
				mTabHost.addTab(s);
				channels.put("pm_"+c.tag, c);
			}
		}
	}
	public void onChat(ArrayList<ChatMsg> recent) {
		Iterator<ChatMsg> i = recent.iterator();
		while (i.hasNext()) {
			ChatMsg c = i.next();
			TextView channel = new TextView(this);
			TextView sender = new TextView(this);
			TextView msg = new TextView(this);
			LinearLayout l = new LinearLayout(this);
			sender.setText(c.sender);
			sender.setOnClickListener(nameClicker);
			sender.setClickable(true);
			msg.setText(" "+c.message); // FIXME, padding
			Channel currentChannel = channels.get("general");
			
			if (c.channel.equals("@A")) currentChannel = channels.get("alliance");
			else if (c.channel.equals("@C")) currentChannel = channels.get("general");
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
					currentChannel.adapter.setSource(session.chat, currentChannel);
					Log.v(TAG,"source set");
					s.setIndicator(currentChannel.tag);
					mTabHost.addTab(s);
					channels.put("pm_"+currentChannel.tag, currentChannel);
				}
			}
			
			currentChannel.adapter.notifyDataSetChanged();
			
			l.addView(channel);
			if (c.hascrown) {
				ImageView crown = new ImageView(this);
				crown.setImageDrawable(this.getResources().getDrawable(R.drawable.icon_lou_public_other_world));
				l.addView(crown);
			}
			l.addView(sender);
			l.addView(msg);
			
			if (c.channel.equals("@A")) {
				return;
			}
			else if (c.channel.equals("privatein") || c.channel.equals("privateout")) {
				Channel c2 = channels.get("pm_"+c.sender);
			}
		}
	}
	public void sendMsg(View v) {
		String tag = mTabHost.getCurrentTabTag();
		EditText m = (EditText) findViewById(R.id.message);
		String buffer = m.getText().toString();
		if (buffer.length() == 0) return;
		if (buffer.charAt(0) == '/') {} 
		else if (tag.equals("alliance")) buffer = "/a "+buffer;
		else if (channels.containsKey("pm_"+tag)) {
			buffer = "/whisper "+tag+" "+buffer;
		}
		userActive();
		session.rpc.QueueChat(buffer);
		m.setText("");
	}
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.removeItem(R.id.open_chat);
		return true;
	}
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.v(TAG,"click! "+item.getItemId());
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent i = new Intent(this,LouSessionMain.class);
			i.putExtras(acct.toBundle());
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
