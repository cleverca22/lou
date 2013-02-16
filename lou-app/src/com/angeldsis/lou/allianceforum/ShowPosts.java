package com.angeldsis.lou.allianceforum;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.angeldsis.lou.BBCode;
import com.angeldsis.lou.BBCode.Span;
import com.angeldsis.lou.R;
import com.angeldsis.lou.SessionUser;
import com.angeldsis.louapi.RPC.GetForumPostCallback;
import com.angeldsis.louapi.data.ForumPost;

/** shows all posts within a single thread
 * 
 * @author clever
 *
 */
public class ShowPosts extends SessionUser implements GetForumPostCallback {
	public class PostList extends ArrayAdapter<ForumPost> {
		public PostList(ForumPost[] out) {
			super(ShowPosts.this,0,out);
		}
		@Override public int getCount() {
			int count = super.getCount();
			Log.v("ShowPosts","count is "+count);
			return count;
		}
		@Override public View getView(int index,View out,ViewGroup parent) {
			Log.v("ShowPosts",String.format("getView(%d,%s,%s)",index,out,parent));
			if (out == null) {
				out = ShowPosts.this.getLayoutInflater().inflate(R.layout.forum_post_row, parent,false);
			}
			TextView msg = (TextView) out.findViewById(R.id.msg);
			TextView poster = (TextView) out.findViewById(R.id.poster);
			ForumPost p = getItem(index);
			SpannableStringBuilder b = new SpannableStringBuilder();
			ArrayList<Span> spans = new ArrayList<Span>();
			BBCode.parse(ShowPosts.this,p.msg,b, spans);
			for (Span s : spans) s.apply(b);
			msg.setText(b);
			msg.setMovementMethod(LinkMovementMethod.getInstance());
			poster.setText(p.playerName);
			return out;
		}
	}
	private ListView list;
	private long forumID,threadID;
	private boolean loaded = false;
	private String forumName,threadName;
	
	@Override public void onCreate(Bundle sis) {
		super.onCreate(sis);
		setContentView(R.layout.forum_post);
		list = (ListView) findViewById(R.id.list);
		Bundle b = this.getIntent().getExtras();
		forumID = b.getLong("forumID");
		forumName = b.getString("forumName");
		threadID = b.getLong("threadID");
		threadName = b.getString("threadName");
		setField(R.id.forum,forumName);
		setField(R.id.thread,threadName);
	}
	private void setField(int id,String val) {
		((TextView)findViewById(id)).setText(val);
	}
	public void session_ready() {
		if (loaded == false) refresh();
	}
	private void refresh() {
		session.rpc.GetAllianceForumPosts(forumID,threadID,this);
	}
	@Override
	public void done(ForumPost[] out) {
		loaded = true;
		PostList a = new PostList(out);
		list.setAdapter(a);
		Log.v("ShowPosts","out size "+out.length);
	}
	public void makeReply(View v) {
		Log.v("ShowPosts","makeReply");
		Intent i = new Intent(this,NewThread.class);
		i.putExtras(acct.toBundle());
		i.putExtra("forumID", forumID);
		i.putExtra("threadID", threadID);
		startActivity(i);
		loaded = false;
	}
}
