package com.angeldsis.lou;

import com.angeldsis.louapi.Account;
import com.angeldsis.louapi.RPC.SubRequestDone;
import com.angeldsis.louapi.data.SubRequest;
import com.angeldsis.louapi.data.SubRequest.Role;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class Options extends FragmentBase implements SubRequestDone, OnClickListener {
	private static final String TAG = "Options";
	SubRequest subOffer;
	private TextView subname,subname2;
	ViewFlipper flipper;
	private ViewGroup list;
	@Override public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle sis) {
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.options,parent,false);
		root.findViewById(R.id.sendSub).setOnClickListener(this);
		root.findViewById(R.id.clearSub).setOnClickListener(this);
		subname = (TextView) root.findViewById(R.id.subName);
		subname2 = (TextView) root.findViewById(R.id.subName2);
		flipper = (ViewFlipper) root.findViewById(R.id.flipper);
		list = (ViewGroup) root.findViewById(R.id.list);
		return root;
	}
	@Override public void session_ready() {
		onSubListChanged();
	}
	@Override
	public void onSubListChanged() {
		list.removeAllViews();
		subOffer = null;
		for (SubRequest s : parent.session.rpc.state.subs) {
			Log.v(TAG,s.toString());
			if (s.role == Role.giver) {
				subOffer = s;
				subname2.setText(s.receiver.getName());
				flipper.setDisplayedChild(1);
			}
			if (s.state != 2) continue;
			Button b = new Button(getActivity());
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
			parent.session.rpc.CreateSubstitutionSession(s,Options.this);
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
			parent.session.rpc.SubstitutionCreateReq(subname.getText().toString());
			break;
		case R.id.clearSub:
			parent.session.rpc.SubstitutionCancelReq(subOffer);
			break;
		}
	}
}
