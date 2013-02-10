package com.angeldsis.lou.allianceforum;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.angeldsis.lou.R;
import com.angeldsis.lou.SessionUser;
import com.angeldsis.louapi.RPC.GetAllianceForumsCallback;
import com.angeldsis.louapi.data.AllianceForum;

public class AllianceForumList extends SessionUser implements GetAllianceForumsCallback, OnItemClickListener {
	AllianceForum[] forums = null;
	ListView list;
	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		setContentView(R.layout.alliance_forum_list);
		list = (ListView) findViewById(R.id.list);
		list.setOnItemClickListener(this);
	}
	@Override public void session_ready() {
		//if ((forums == null) || (forums.length == 0))
		refresh();
	}
	private void refresh() {
		session.rpc.GetAllianceForums(this);
	}
	@Override public void done(AllianceForum[] output) {
		ForumList a = new ForumList(this,output);
		forums = output;
		list.setAdapter(a);
	}
	class ForumList extends ArrayAdapter<AllianceForum> {
		public ForumList(Context context, AllianceForum[] output) {
			super(context,0,output);
		}
		@Override public long getItemId(int index) {
			return getItem(index).forumID;
		}
		@Override public View getView(int index,View out,ViewGroup parent) {
			ViewHolder h;
			if (out == null) {
				out = AllianceForumList.this.getLayoutInflater().inflate(R.layout.alliance_forum_row, parent,false);
				h = new ViewHolder();
				h.name = (TextView) out.findViewById(R.id.name);
				out.setTag(h);
			} else h = (ViewHolder) out.getTag();
			AllianceForum a = getItem(index);
			String msg = a.forumName;
			if (a.translatable) {
				Resources r = AllianceForumList.this.getResources();
				if (a.forumName.equals("@Announcements")) msg = r.getString(R.string.announcements);
				else if (a.forumName.equals("@General")) msg = r.getString(R.string.general);
				else if (a.forumName.equals("@Introduction")) msg = r.getString(R.string.introduction);
				else if (a.forumName.equals("@Offtopic")) msg = r.getString(R.string.offtopic);
			}
			h.translated = msg;
			msg = ""+a.hup+" "+msg;
			h.name.setText(msg);
			return out;
		}
	}
	private static class ViewHolder {
		public String translated;
		public TextView name;
	}
	@Override
	public void onItemClick(AdapterView<?> list, View row, int index, long id) {
		ViewHolder h = (ViewHolder) row.getTag();
		Intent i = new Intent(this,ShowForum.class);
		i.putExtras(acct.toBundle());
		i.putExtra("forumID", id);
		i.putExtra("forumName", h.translated);
		startActivity(i);
	}
}
