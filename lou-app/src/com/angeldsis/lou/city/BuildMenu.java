package com.angeldsis.lou.city;

import com.angeldsis.lou.CityView;
import com.angeldsis.lou.R;
import com.angeldsis.louapi.LouState.City;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class BuildMenu extends DialogFragment {
	private long cityid;
	private int coord;
	static final private String TAG = "BuildMenu";
	public static BuildMenu newInstance(City city,int coord) {
		Bundle args = new Bundle();
		args.putLong("cityid", city.getCityid());
		args.putInt("coord", coord);
		BuildMenu f = new BuildMenu();
		f.setArguments(args);
		return f;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle b = getArguments();
		cityid = b.getLong("cityid");
		coord = b.getInt("coord");
		setStyle(DialogFragment.STYLE_NO_TITLE,android.R.style.Theme_Holo_Dialog);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.build_dialog, container,false);
		link_button(v,R.id.cottage,4);
		link_button(v,R.id.marketplace,5);
		link_button(v,R.id.sawmill,7);
		link_button(v,R.id.hideout,9);
		link_button(v,R.id.stonemason,10);
		link_button(v,R.id.foundry,11);
		link_button(v,R.id.townhouse,13);
		link_button(v,R.id.barracks,14);
		link_button(v,R.id.guardhouse,15);
		link_button(v,R.id.training_ground,16);
		link_button(v,R.id.warehouse,20);
		link_button(v,R.id.moonglow,36);
		link_button(v,R.id.ranger,41);
		link_button(v,R.id.woodcutter,47);
		link_button(v,R.id.quary,48);
		link_button(v,R.id.iron_mine,49);
		link_button(v,R.id.farm,50);
		return v;
	}
	void link_button(View v,int id,final int type) {
		((ImageButton)v.findViewById(id)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((CityView)getActivity()).do_build(cityid,type, coord);
				dismiss();
			}
		});
	}
}
