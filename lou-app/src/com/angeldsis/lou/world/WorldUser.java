package com.angeldsis.lou.world;

import android.util.Log;

import com.angeldsis.lou.SessionUser;
import com.angeldsis.louapi.data.Coord;
import com.angeldsis.louapi.world.WorldParser;

public class WorldUser extends SessionUser {
	private static final String TAG = "WorldUser";
	public void session_ready() {
		super.session_ready();
		Log.v(TAG,"enabling world");
		session.rpc.setWorldEnabled(true);
		WorldParser p = session.rpc.worldParser;
		Coord c = session.rpc.state.currentCity.location;
		int col = c.x/32;
		int row = c.y/32;
		p.mincol = p.maxcol = col;
		p.minrow = p.maxrow = row;
	}
	public void onStop() {
		Log.v(TAG,"disabling world");
		if (session != null) session.rpc.setWorldEnabled(false);
		super.onStop();
	}
}
