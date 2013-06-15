package com.angeldsis.lou.home;

import java.util.Iterator;

import com.angeldsis.lou.AccountWrap;
import com.angeldsis.lou.LoggingIn;
import com.angeldsis.lou.R;
import com.angeldsis.lou.SessionKeeper;
import com.angeldsis.lou.SessionKeeper.Session;
import com.angeldsis.louapi.Account;
import com.angeldsis.louapi.RPC.SubRequestDone;
import com.angeldsis.louapi.data.SubRequest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SubstituteLogin extends Fragment implements SubRequestDone {
	private static final String TAG = "SubstituteLogin";
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Bundle b = getArguments();
		int sessionid = b.getInt("id");
		int subid = b.getInt("sub_id");
		Iterator<Session> i = SessionKeeper.getInstance().sessions.iterator();
		while (i.hasNext()) {
			Session s = i.next();
			if (s.sessionid != sessionid) continue;
			Iterator<SubRequest> i2 = s.state.subs.iterator();
			while (i2.hasNext()) {
				SubRequest sr = i2.next();
				if (sr.state != 2) continue;
				if (sr.id != subid) continue;
				Log.v(TAG,"found sub "+sr.giver.getName());
				s.rpc.CreateSubstitutionSession(sr,this);
				break;
			}
			break;
		}
		return inflater.inflate(R.layout.loading, container, false);
	}
	@Override public void allDone(Account acct2) {
		Intent login = new Intent(getActivity(), LoggingIn.class);
		login.putExtras(((AccountWrap) acct2).toBundle());
		
		// FIXME, use the fragment back stack
		//getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new ServerList()).commit();
		startActivity(login);
	}
}
