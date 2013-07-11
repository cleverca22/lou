package com.angeldsis.lou;

import com.angeldsis.louapi.Account;
import com.angeldsis.louapi.RPC.SubRequestDone;
import com.angeldsis.louapi.data.SubRequest;
import com.angeldsis.louapi.data.SubRequest.Role;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class Options extends SessionUser implements SubRequestDone, OnClickListener {
	private static final String TAG = "Options";
	SubRequest subOffer;
	private TextView subname,subname2;
	ViewFlipper flipper;
	@Override public void onCreate(Bundle sis) {
		super.onCreate(sis);
		setContentView(R.layout.options);
		findViewById(R.id.sendSub).setOnClickListener(this);
		findViewById(R.id.clearSub).setOnClickListener(this);
		subname = (TextView) findViewById(R.id.subName);
		subname2 = (TextView) findViewById(R.id.subName2);
		flipper = (ViewFlipper) findViewById(R.id.flipper);
	}
	@Override public void session_ready() {
		onSubListChanged();
	}
	@Override
	public void onSubListChanged() {
		ViewGroup list = (ViewGroup) findViewById(R.id.list);
		list.removeAllViews();
		subOffer = null;
		for (SubRequest s : session.rpc.state.subs) {
			Log.v(TAG,s.toString());
			if (s.role == Role.giver) {
				subOffer = s;
				subname2.setText(s.receiver.getName());
				flipper.setDisplayedChild(1);
			}
			if (s.state != 2) continue;
			Button b = new Button(this);
			b.setText(s.giver.getName());
			b.setOnClickListener(new clicker(s));
			list.addView(b);
		}
		if (subOffer == null) flipper.setDisplayedChild(0);
	}
	private class clicker implements OnClickListener {
		SubRequest s;
		public clicker(SubRequest s) {
			this.s = s;
		}
		@Override
		public void onClick(View v) {
			session.rpc.CreateSubstitutionSession(s,Options.this);
		}
	}
	@Override
	public void allDone(Account acct2) {
		// TODO Auto-generated method stub
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sendSub:
			session.rpc.SubstitutionCreateReq(subname.getText().toString());
			break;
		case R.id.clearSub:
			session.rpc.SubstitutionCancelReq(subOffer);
			break;
		}
	}
}
