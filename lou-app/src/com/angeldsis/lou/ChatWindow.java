package com.angeldsis.lou;

import java.util.ArrayList;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TextView;

import com.angeldsis.louapi.ChatMsg;

public class ChatWindow extends SessionUser {
	static final String TAG = "ChatWindow";
	private TabHost mTabHost;
	enum Type { pm,channel };
	class Channel {
		Type type;
		ScrollView scrollView;
		LinearLayout oldmessagelist;
		String tag;
		public LinearLayout wrapper;
		public ListView newmessagelist;
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
		t1.setContent(new TabMaker(temp));
		t1.setIndicator("General");
		mTabHost.addTab(t1);
		channels.put("general",temp);
		
		temp = new Channel();
		temp.type = Type.channel;
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
			
			c.oldmessagelist = new LinearLayout(ChatWindow.this);
			c.oldmessagelist.setOrientation(LinearLayout.VERTICAL);
			
			c.newmessagelist = new ListView(ChatWindow.this);
			
			c.wrapper = new LinearLayout(ChatWindow.this);
			c.wrapper.addView(c.oldmessagelist);
			c.wrapper.addView(c.newmessagelist);
			
			c.scrollView = new ScrollView(ChatWindow.this);
			c.scrollView.addView(c.wrapper);
		}
		@Override
		public View createTabContent(String tag) {
			return c.scrollView;
		}
	}
	public void onStart() {
		super.onStart();
	}
	public void session_ready() {
		for (Channel c : channels.values()) c.oldmessagelist.removeAllViews();
		onChat(session.state.chat_history);
	}
	@Override
	public void visDataReset() {
		// TODO Auto-generated method stub
	}
	@Override
	public void onPlayerData() {
		// TODO Auto-generated method stub
	}
	@Override
	public void gotCityData() {
		// TODO Auto-generated method stub
	}
	@Override
	public void tick() {
		// TODO Auto-generated method stub
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
				TabHost.TabSpec s = mTabHost.newTabSpec(c.tag);
				s.setContent(new TabMaker(c));
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
			sender.setText(c.s);
			sender.setOnClickListener(nameClicker);
			sender.setClickable(true);
			msg.setText(" "+c.m); // FIXME, padding
			Channel currentChannel = channels.get("general");
			
			if (c.c.equals("@A")) currentChannel = channels.get("alliance");
			else if (c.c.equals("@C")) currentChannel = channels.get("general");
			
			l.addView(channel);
			if (c.hascrown) {
				ImageView crown = new ImageView(this);
				crown.setImageDrawable(this.getResources().getDrawable(R.drawable.icon_lou_public_other_world));
				l.addView(crown);
			}
			l.addView(sender);
			l.addView(msg);
			
			if (c.c.equals("@A")) {
				channel.setText(getString(R.string.alliance));
				int green = this.getResources().getColor(R.color.chat_green);
				channel.setTextColor(green);
				sender.setTextColor(green);
				msg.setTextColor(green);
				currentChannel.oldmessagelist.addView(l);
			}
			else if (c.c.equals("privatein") || c.c.equals("privateout")) {
				Channel c2 = channels.get("pm_"+c.s);
				if (c2 == null) {
					c2 = new Channel();
					c2.tag = c.s;
					c2.type = Type.pm;
					TabHost.TabSpec s = mTabHost.newTabSpec(c2.tag);
					s.setContent(new TabMaker(c2));
					s.setIndicator(c2.tag);
					mTabHost.addTab(s);
					channels.put("pm_"+c2.tag, c2);
					currentChannel = c2;
				} else {
					currentChannel = c2;
				}
				if (c.c.equals("privateout")) sender.setText(session.state.self.getName()+": ");
				else sender.setText(c.s+": ");
				l.removeView(channel);
				c2.oldmessagelist.addView(l);
			}
			else {
				channel.setText(c.c);
				currentChannel.oldmessagelist.addView(l);
			}
			if (currentChannel != null) {
				delayScroll.postDelayed(new Delayer(currentChannel.scrollView), 100);
			}
		}
	}
	class Delayer implements Runnable {
		ScrollView s;
		Delayer(ScrollView s) {
			this.s = s;
		}
		public void run() {
			s.smoothScrollBy(0, 100);
		}
	}
	Handler delayScroll = new Handler();
	public void sendMsg(View v) {
		String tag = mTabHost.getCurrentTabTag();
		EditText m = (EditText) findViewById(R.id.message);
		String buffer = m.getText().toString();
		if (buffer.charAt(0) == '/') {} 
		else if (tag.equals("alliance")) buffer = "/a "+buffer;
		else if (channels.containsKey("pm_"+tag)) {
			buffer = "/whisper "+tag+" "+buffer;
		}
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
