package com.angeldsis.lou.reports;

import java.util.Date;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.angeldsis.lou.R;
import com.angeldsis.lou.SessionUser;
import com.angeldsis.louapi.RPC.ReportCallback;
import com.angeldsis.louapi.Report;
import com.angeldsis.louapi.Report.ReportHalf;
import com.angeldsis.louapi.Report.UnitInfo;

public class ShowReport extends SessionUser implements ReportCallback {
	ViewGroup side1,side2;
	@Override
	public void onCreate(Bundle sis) {
		super.onCreate(sis);
		setContentView(R.layout.show_report);
		ViewGroup sides = (ViewGroup) findViewById(R.id.sides);
		LayoutInflater i = getLayoutInflater();
		side1 = (ViewGroup) i.inflate(R.layout.report_half, sides,false);
		side2 = (ViewGroup) i.inflate(R.layout.report_half, sides,false);
		sides.addView(side1);
		sides.addView(side2);
	}
	public void session_ready() {
		Intent msg = getIntent();
		Bundle args = msg.getExtras();
		int reportid = args.getInt("reportid");
		session.rpc.GetReport(reportid,this);
	}
	@Override
	public void done(Report report) {
		Log.v("ShowReport",report.toString());
		setField(R.id.share,report.share);
		setField(R.id.when,(new Date(report.d)).toString());
		setupHalf(side1,report.attacker,R.string.trapped);
		setupHalf(side2,report.defender,R.string.fortified);
	}
	private void setupHalf(ViewGroup side, ReportHalf half, int altered_id) {
		((TextView)side.findViewById(R.id.player_name)).setText(half.player);
		((TextView)side.findViewById(R.id.city)).setText(half.cityname);
		int both = half.coord;
		int lowbyte = 0xffff & both;
		int highbyte = (0xffff0000 & both) >> 16;
		((TextView)side.findViewById(R.id.location)).setText(String.format("C?? (%d:%d)",lowbyte,highbyte));
		ViewGroup units = (ViewGroup) side.findViewById(R.id.units);
		((TextView)side.findViewById(R.id.altered)).setText(altered_id);
		setupUnits(units,half.units);
	}
	private void setupUnits(ViewGroup v, UnitInfo[] units2) {
		Log.v("ShowReport","setupUnits("+v+","+units2+")");
		int i;
		Log.v("ShowReport","units count:"+units2.length);
		for (i = 0; i < units2.length; i++) {
			ViewGroup unit = (ViewGroup) getLayoutInflater().inflate(R.layout.unit_report, v,false);
			((ImageView)unit.findViewById(R.id.image)).setImageResource(R.drawable.icon_units_berserker);
			setField(unit,R.id.ordered,""+units2[i].ordered);
			setField(unit,R.id.neutralized,""+units2[i].s);
			setField(unit,R.id.lost,""+(units2[i].ordered - units2[i].survived));
			setField(unit,R.id.survived,""+units2[i].survived);
			Log.v("ShowReport","count: "+v.getChildCount()+"v:"+v+"unit:"+unit);
			v.addView(unit);
			Log.v("ShowReport","count:"+v.getChildCount());
		}
	}
	private void setField(ViewGroup v,int id,String value) {
		((TextView)v.findViewById(id)).setText(value);
	}
	private void setField(int id,String value) {
		((TextView)findViewById(id)).setText(value);
	}
}
