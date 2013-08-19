package com.angeldsis.lou.world;

import android.util.Log;

import com.angeldsis.lou.FragmentBase;
import com.angeldsis.lou.SessionKeeper.Session;
import com.angeldsis.lou.SessionUser;
import com.angeldsis.louapi.data.Coord;
import com.angeldsis.louapi.world.WorldParser;

public abstract class WorldUser extends FragmentBase {
	private static final String TAG = "WorldUser";
	public void session_ready() {
		Log.v(TAG,"enabling world");
		parent.session.rpc.setWorldEnabled(true);
		resetFocus();
		WorldParser p = parent.session.rpc.worldParser;
		p.enable();
	}
	public void onStop() {
		Log.v(TAG,"disabling world");
		Session s = parent.session;
		s.rpc.worldParser.disable();
		if (parent.session != null) parent.session.rpc.setWorldEnabled(false);
		super.onStop();
	}
	public void resetFocus() {
		WorldParser p = parent.session.rpc.worldParser;
		Coord c = parent.session.rpc.state.currentCity.location;
		int col = c.x/32;
		int row = c.y/32;
		p.mincol = col - 1;
		p.maxcol = col + 1;
		
		p.minrow = row - 1;
		p.maxrow = row + 1;
	}
}
