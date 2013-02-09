package com.angeldsis.lou.allianceforum;

import java.util.ArrayList;

import android.os.Bundle;
import android.text.SpannableStringBuilder;
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

public class ShowPosts extends SessionUser implements GetForumPostCallback {
	public class PostList extends ArrayAdapter<ForumPost> {
		public PostList(ForumPost[] out) {
			super(ShowPosts.this,0,out);
		}
		@Override public View getView(int index,View out,ViewGroup parent) {
			if (out == null) {
				out = ShowPosts.this.getLayoutInflater().inflate(R.layout.forum_post_row, parent,false);
			}
			TextView msg = (TextView) out.findViewById(R.id.msg);
			ForumPost p = getItem(index);
			SpannableStringBuilder b = new SpannableStringBuilder();
			ArrayList<Span> spans = new ArrayList<Span>();
			BBCode.parse(ShowPosts.this,p.msg,b, spans);
			for (Span s : spans) s.apply(b);
			msg.setText(b);
			return out;
		}
	}
	private ListView list;
	private long forumID,threadID;
	private boolean loaded = false;
	
	@Override public void onCreate(Bundle sis) {
		super.onCreate(sis);
		setContentView(R.layout.forum_post);
		list = (ListView) findViewById(R.id.list);
		Bundle b = this.getIntent().getExtras();
		forumID = b.getLong("forumID");
		threadID = b.getLong("threadID");
	}
	public void session_ready() {
		if (loaded == false) refresh();
	}
	private void refresh() {
		session.rpc.GetAllianceForumPosts(forumID,threadID,this);
	}
	@Override
	public void done(ForumPost[] out) {
		PostList a = new PostList(out);
		list.setAdapter(a);
	}
}
