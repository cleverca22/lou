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
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TextView;

import com.angeldsis.louapi.ChatMsg;

public class ChatWindow extends SessionUser {
	static final String TAG = "ChatWindow";
	private TabHost mTabHost;
	LinearLayout general,alliance;
	class PM {
		LinearLayout l;
		String tag;
	}
	enum Type { pm,channel };
	class Channel {
		Type type;
		PM oldpm;
		public ScrollView scrollView;
	}
	Map<String,PM> private_messages;
	Map<String,Channel> channels;
	private NameClicked nameClicker;
	public void onCreate(Bundle b) {
		super.onCreate(b);
		if (Build.VERSION.SDK_INT > 13) initApi14();
		setContentView(R.layout.chat_window);
		general = new LinearLayout(this);
		alliance = new LinearLayout(this);
		private_messages = new HashMap<String,PM>();
		channels = new HashMap<String,Channel>();
		
		mTabHost = (TabHost)findViewById(R.id.tabhost);
		mTabHost.setup();
		
		TabHost.TabSpec t1 = mTabHost.newTabSpec("general");
		Channel temp = new Channel();
		temp.type = Type.channel;
		t1.setContent(new TabMaker(general,temp));
		t1.setIndicator("General");
		mTabHost.addTab(t1);
		channels.put("general",temp);
		
		temp = new Channel();
		temp.type = Type.channel;
		TabHost.TabSpec t2 = mTabHost.newTabSpec("alliance");
		t2.setContent(new TabMaker(alliance,temp));
		t2.setIndicator("alliance");
		mTabHost.addTab(t2);
		channels.put("alliance", temp);
		
		nameClicker = new NameClicked();
	}
	class TabMaker implements TabHost.TabContentFactory {
		LinearLayout l;
		ScrollView s;
		public TabMaker(LinearLayout general, Channel channel) {
			l = general;
			l.setOrientation(LinearLayout.VERTICAL);
			s = new ScrollView(ChatWindow.this);
			s.addView(l);
			channel.scrollView = s;
		}
		@Override
		public View createTabContent(String tag) {
			return s;
		}
	}
	public void onStart() {
		super.onStart();
	}
	public void session_ready() {
		general.removeAllViews();
		alliance.removeAllViews();
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
			PM p = private_messages.get(tag);
			if (p == null) {
				p = new PM();
				p.l = new LinearLayout(ChatWindow.this);
				p.tag = tag;
				Channel temp = new Channel();
				temp.type = Type.pm;
				TabHost.TabSpec s = mTabHost.newTabSpec(p.tag);
				s.setContent(new TabMaker(p.l,temp));
				s.setIndicator(p.tag);
				mTabHost.addTab(s);
				channels.put("pm_"+p.tag, temp);
				private_messages.put(p.tag, p);
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
			Channel currentChannel = null;
			
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
				alliance.addView(l);
			}
			else if (c.c.equals("privatein") || c.c.equals("privateout")) {
				PM p = private_messages.get(c.s);
				if (p == null) {
					p = new PM();
					p.l = new LinearLayout(this);
					p.tag = c.s;
					Channel temp = new Channel();
					temp.type = Type.pm;
					TabHost.TabSpec s = mTabHost.newTabSpec(p.tag);
					s.setContent(new TabMaker(p.l,temp));
					s.setIndicator(p.tag);
					mTabHost.addTab(s);
					channels.put("pm_"+p.tag, temp);
					currentChannel = temp;
					private_messages.put(p.tag, p);
				} else {
					currentChannel = channels.get("pm_"+p.tag);
				}
				if (c.c.equals("privateout")) sender.setText("clever: ");
				else sender.setText(c.s+": ");
				l.removeView(channel);
				p.l.addView(l);
			}
			else {
				channel.setText(c.c);
				general.addView(l);
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
		if (tag.equals("alliance")) buffer = "/a "+buffer;
		else if (private_messages.containsKey(tag)) {
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
