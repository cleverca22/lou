package com.angeldsis.lou.allianceforum;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.angeldsis.lou.R;
import com.angeldsis.lou.SessionUser;
import com.angeldsis.louapi.RPC.GetAllianceForumsCallback;
import com.angeldsis.louapi.data.AllianceForum;

public class AllianceForumList extends SessionUser implements GetAllianceForumsCallback {
	ArrayList<AllianceForum> forums = new ArrayList<AllianceForum>();
	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		setContentView(R.layout.alliance_forum_list);
	}
	@Override public void session_ready() {
		if (forums.size() == 0) refresh();
	}
	private void refresh() {
		session.rpc.GetAllianceForums(this);
	}
	@Override public void done(AllianceForum[] output) {
		ForumList a = new ForumList(this,output);
		ListView list = (ListView) findViewById(R.id.list);
		list.setAdapter(a);
	}
	class ForumList extends ArrayAdapter<AllianceForum> {
		public ForumList(Context context, AllianceForum[] output) {
			super(context, R.layout.alliance_forum_row,R.id.name,output);
		}
	}
}
