package com.angeldsis.lou;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.os.Bundle;
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
	Map<String,PM> private_messages;
	private NameClicked nameClicker;
	public void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.chat_window);
		general = new LinearLayout(this);
		alliance = new LinearLayout(this);
		private_messages = new HashMap<String,PM>();
		mTabHost = (TabHost)findViewById(R.id.tabhost);
		mTabHost.setup();
		TabHost.TabSpec t1 = mTabHost.newTabSpec("general");
		t1.setContent(new TabMaker(general));
		t1.setIndicator("General");
		mTabHost.addTab(t1);
		TabHost.TabSpec t2 = mTabHost.newTabSpec("alliance");
		t2.setContent(new TabMaker(alliance));
		t2.setIndicator("alliance");
		mTabHost.addTab(t2);
		nameClicker = new NameClicked();
	}
	class TabMaker implements TabHost.TabContentFactory {
		LinearLayout l;
		ScrollView s;
		public TabMaker(LinearLayout general) {
			l = general;
			l.setOrientation(LinearLayout.VERTICAL);
			s = new ScrollView(ChatWindow.this);
			s.addView(l);
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
				TabHost.TabSpec s = mTabHost.newTabSpec(p.tag);
				s.setContent(new TabMaker(p.l));
				s.setIndicator(p.tag);
				mTabHost.addTab(s);
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
			
			l.addView(channel);
			if (c.hascrown) {
				ImageView crown = new ImageView(this);
				crown.setImageDrawable(this.getResources().getDrawable(R.drawable.icon_lou_public_other_world));
				l.addView(crown);
			}
			l.addView(sender);
			l.addView(msg);
			
			if (c.c.equals("@A")) {
				channel.setText("[Alliance] ");
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
					TabHost.TabSpec s = mTabHost.newTabSpec(p.tag);
					s.setContent(new TabMaker(p.l));
					s.setIndicator(p.tag);
					mTabHost.addTab(s);
					private_messages.put(p.tag, p);
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
		}
	}
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
}
