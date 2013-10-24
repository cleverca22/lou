package com.angeldsis.lou;

import com.angeldsis.louapi.LouState.City;
import com.angeldsis.louapi.RPC.GotPublicCityInfo;
import com.angeldsis.louapi.data.Coord;
import com.angeldsis.louapi.data.PublicCityInfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ShowCoord extends FragmentBase implements GotPublicCityInfo {
	private static final String TAG = "ShowCoord";
	int x,y;
	Coord self;
	TextView coord,owner;
	ViewGroup bookmarkState;
	Button switchto;
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle b) {
		super.onCreate(b);
		//if (Build.VERSION.SDK_INT > 13) initApi14();
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.coord,parent,false);
		coord = (TextView) root.findViewById(R.id.coord);
		owner = (TextView) root.findViewById(R.id.owner);
		bookmarkState = (ViewGroup) root.findViewById(R.id.bookmarkState);
		switchto = (Button) root.findViewById(R.id.switchto);
		return root;
	}
	@Override public void session_ready() {
		Bundle args = getArguments();
		if (args == null) {
			args = getActivity().getIntent().getExtras();
		}
		if (args.containsKey("x")) {
			x = args.getInt("x");
			y = args.getInt("y");
			self = new Coord(x,y);
			coord.setText(self.format());
			checkBookmarks();
			int cityid = Coord.toCityId(x,y);
			parent.session.rpc.GetPublicCityInfo(cityid,this);
		}
	}
	private void checkBookmarks() {
		SharedPreferences p = getActivity().getSharedPreferences("bookmarks", Context.MODE_PRIVATE);
		String[] bookmarks = p.getString("bookmarks", "").split(",");
		String findme = self.format();
		bookmarkState.removeAllViews();
		for (String b : bookmarks) {
			if (b.length() == 0) continue;
			Log.v(TAG,"bookmark :"+b);
			if (findme.equals(b)) {
				parent.getLayoutInflater().inflate(R.layout.remove_bookmark, bookmarkState);
				return;
			}
		}
		parent.getLayoutInflater().inflate(R.layout.add_bookmark, bookmarkState);
	}
	public void addBookmark(View v) {
		SharedPreferences p = parent.getSharedPreferences("bookmarks", Context.MODE_PRIVATE);
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
		owner.setText(p.player.getName());
		if (p.player.getId() == parent.session.rpc.state.self.getId()) {
			switchto.setVisibility(View.VISIBLE);
		} else {
			switchto.setVisibility(View.GONE);
		}
	}
	public void switchToCity(View v) {
		City c = parent.session.rpc.state.cities.get(self.toCityId());
		RPCWrap rpc = parent.session.rpc;
		rpc.state.changeCity(c);
		//finish();
	}
}
