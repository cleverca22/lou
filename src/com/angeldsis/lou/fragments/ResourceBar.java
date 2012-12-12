package com.angeldsis.lou.fragments;

import com.angeldsis.lou.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ResourceBar extends Fragment {
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.resource_bar,container,false);
	}
	public void setLevels(int wood, int stone, int iron, int food) {
		((TextView)getActivity().findViewById(R.id.woodC)).setText(""+wood);
		((TextView)getActivity().findViewById(R.id.stoneC)).setText(""+stone);
		((TextView)getActivity().findViewById(R.id.ironC)).setText(""+iron);
		((TextView)getActivity().findViewById(R.id.foodC)).setText(""+food);
	}
}
