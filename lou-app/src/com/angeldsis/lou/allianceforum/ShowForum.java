package com.angeldsis.lou.allianceforum;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.angeldsis.lou.R;
import com.angeldsis.lou.SessionUser;
import com.angeldsis.louapi.RPC.GotForumThreads;
import com.angeldsis.louapi.data.ForumThread;

public class ShowForum extends SessionUser implements OnItemClickListener, GotForumThreads {
	public class ThreadList extends ArrayAdapter<ForumThread> {
		public ThreadList(ShowForum showForum, ForumThread[] out) {
			super(showForum,0,out);
		}
		@Override public long getItemId(int index) {
			return getItem(index).threadID;
		}
		@Override public View getView(int index,View out,ViewGroup parent) {
			if (out == null) {
				out = ShowForum.this.getLayoutInflater().inflate(R.layout.forum_thread_row, parent,false);
			}
			TextView topic = (TextView) out.findViewById(R.id.topic);
			ImageView seen = (ImageView) out.findViewById(R.id.seen);
			ForumThread t = getItem(index);
			topic.setText(t.tt);
			if(t.hup) seen.setImageResource(R.drawable.icon_new_post);
			else seen.setImageResource(R.drawable.icon_old_post);
			return out;
		}
	}
	ForumThread[] forums = null;
	private ListView list;
	private long forumID;
	private String forumName;
	@Override public void onCreate(Bundle sis) {
		super.onCreate(sis);
		setContentView(R.layout.forum_threads);
		list = (ListView) findViewById(R.id.list);
		list.setOnItemClickListener(this);
		Bundle b = this.getIntent().getExtras();
		forumID = b.getLong("forumID");
		forumName = b.getString("forumName");
		((TextView)findViewById(R.id.forum)).setText(forumName);
	}
	@Override public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long rowid) {
		Log.v("ShowForum",String.format("GetAllianceForumPosts(%d,%d)",forumID,rowid));
		Intent i = new Intent(this,ShowPosts.class);
		i.putExtras(acct.toBundle());
		i.putExtra("forumID", forumID);
		i.putExtra("forumName",forumName);
		i.putExtra("threadID", rowid);
		i.putExtra("threadName",((ThreadList)list.getAdapter()).getItem(arg2).tt);
		startActivity(i);
	}
	@Override public void session_ready() {
		//if ((forums == null) || (forums.length == 0)) 
		refresh();
	}
	private void refresh() {
		session.rpc.GetAllianceForumThreads(forumID,this);
	}
	@Override
	public void done(ForumThread[] out) {
		ThreadList a = new ThreadList(this,out);
		forums = out;
		list.setAdapter(a);
	}
	public void newThread(View v) {
		Intent i = new Intent(this,NewThread.class);
		i.putExtras(acct.toBundle());
		i.putExtra("forumID", forumID);
		startActivity(i);
	}
}
