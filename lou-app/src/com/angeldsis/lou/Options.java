package com.angeldsis.lou;

import com.angeldsis.louapi.data.SubRequest;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class Options extends SessionUser {
	@Override public void onCreate(Bundle sis) {
		super.onCreate(sis);
		setContentView(R.layout.options);
	}
	@Override public void session_ready() {
		onSubListChanged();
	}
	@Override
	public void onSubListChanged() {
		ViewGroup list = (ViewGroup) findViewById(R.id.list);
		list.removeAllViews();
		for (SubRequest s : session.rpc.state.subs) {
			if (s.state != 2) continue;
			Button b = new Button(this);
			b.setText(s.giver.getName());
			b.setOnClickListener(new clicker(s));
			list.addView(b);
		}
	}
	private class clicker implements OnClickListener {
		SubRequest s;
		public clicker(SubRequest s) {
			this.s = s;
		}
		@Override
		public void onClick(View v) {
			session.rpc.CreateSubstitutionSession(s);
		}
	}
}
