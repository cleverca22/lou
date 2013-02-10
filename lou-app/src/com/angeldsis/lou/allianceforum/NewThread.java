package com.angeldsis.lou.allianceforum;

import org.json2.JSONObject;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.angeldsis.lou.R;
import com.angeldsis.lou.SessionUser;
import com.angeldsis.louapi.RPC.RPCDone;

public class NewThread extends SessionUser implements RPCDone {
	private long forumID,threadID;
	@Override public void onCreate(Bundle sis) {
		super.onCreate(sis);
		setContentView(R.layout.new_thread);
		Bundle b = this.getIntent().getExtras();
		forumID = b.getLong("forumID");
		if (b.containsKey("threadID")) {
			threadID = b.getLong("threadID");
			((TextView)findViewById(R.id.title)).setVisibility(View.GONE);
		}
		else threadID = -1;
	}
	public void makeThread(View v) {
		String title = ((TextView)findViewById(R.id.title)).getText().toString();
		String message = ((TextView)findViewById(R.id.msg)).getText().toString();
		if (threadID == -1) session.rpc.CreateAllianceForumThread(forumID,title,message,this);
		else session.rpc.CreateAllianceForumPost(forumID,threadID,message,this);
	}
	@Override public void requestDone(JSONObject reply) {
		finish();
	}
}
