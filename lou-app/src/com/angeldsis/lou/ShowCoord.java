package com.angeldsis.lou;

import com.angeldsis.louapi.LouState.City;
import com.angeldsis.louapi.RPC.GotPublicCityInfo;
import com.angeldsis.louapi.data.Coord;
import com.angeldsis.louapi.data.PublicCityInfo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ShowCoord extends SessionUser implements GotPublicCityInfo {
	private static final String TAG = "ShowCoord";
	int x,y;
	Coord self;
	public void onCreate(Bundle b) {
		super.onCreate(b);
		if (Build.VERSION.SDK_INT > 13) initApi14();
		setContentView(R.layout.coord);
	}
	@Override public void session_ready() {
		Intent msg = getIntent();
		Bundle args = msg.getExtras();
		if (args.containsKey("x")) {
			x = args.getInt("x");
			y = args.getInt("y");
			self = new Coord(x,y);
			((TextView)findViewById(R.id.coord)).setText(self.format());
			checkBookmarks();
			int cityid = Coord.toCityId(x,y);
			session.rpc.GetPublicCityInfo(cityid,this);
		}
	}
	private void checkBookmarks() {
		SharedPreferences p = this.getSharedPreferences("bookmarks", Context.MODE_PRIVATE);
		String[] bookmarks = p.getString("bookmarks", "").split(",");
		String findme = self.format();
		ViewGroup frame = (ViewGroup) findViewById(R.id.bookmarkState);
		frame.removeAllViews();
		for (String b : bookmarks) {
			if (b.length() == 0) continue;
			Log.v(TAG,"bookmark :"+b);
			if (findme.equals(b)) {
				getLayoutInflater().inflate(R.layout.remove_bookmark, frame);
				return;
			}
		}
		getLayoutInflater().inflate(R.layout.add_bookmark, frame);
	}
	public void addBookmark(View v) {
		SharedPreferences p = this.getSharedPreferences("bookmarks", Context.MODE_PRIVATE);
		String bookmarks = p.getString("bookmarks", "");
		if (bookmarks.length() == 0) bookmarks = self.format();
		else bookmarks = bookmarks + ","+ self.format();
		Editor e = p.edit();
		e.putString("bookmarks", bookmarks);
		e.apply();
		checkBookmarks();
	}
	@Override
	public void done(PublicCityInfo p) {
		((TextView)findViewById(R.id.owner)).setText(p.player.getName());
		Button switchto = (Button) findViewById(R.id.switchto);
		if (p.player.getId() == session.rpc.state.self.getId()) {
			switchto.setVisibility(View.VISIBLE);
		} else {
			switchto.setVisibility(View.GONE);
		}
	}
	public void switchToCity(View v) {
		City c = session.rpc.state.cities.get(self.toCityId());
		session.rpc.state.changeCity(c);
		finish();
	}
}
