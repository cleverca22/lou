package com.angeldsis.lou.reports;

import java.util.Date;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.angeldsis.lou.AndroidEnums;
import com.angeldsis.lou.R;
import com.angeldsis.lou.SessionUser;
import com.angeldsis.louapi.RPC.ReportCallback;
import com.angeldsis.louapi.Report;
import com.angeldsis.louapi.Report.ReportHalf;
import com.angeldsis.louapi.Report.UnitInfo;

public class ShowReport extends SessionUser implements ReportCallback {
	private static final String TAG = "ShowReport";
	ViewGroup side1,side2;
	@Override public void onCreate(Bundle sis) {
		super.onCreate(sis);
		setContentView(R.layout.show_report);
		ViewGroup sides = (ViewGroup) findViewById(R.id.sides);
		LayoutInflater i = getLayoutInflater();
		side1 = (ViewGroup) i.inflate(R.layout.report_half, sides,false);
		side2 = (ViewGroup) i.inflate(R.layout.report_half, sides,false);
		sides.addView(side1);
		sides.addView(side2);
	}
	@Override public void session_ready() {
		Intent msg = getIntent();
		Bundle args = msg.getExtras();
		if (args.containsKey("reportid")) {
			int reportid = args.getInt("reportid");
			Log.v(TAG,"reportid:"+reportid);
			session.rpc.GetReport(reportid,this);
		} else if (args.containsKey("shareid")) {
			String shareid = args.getString("shareid").replace("-", "");
			Log.v(TAG,"shareid:"+shareid);
			session.rpc.GetSharedReport(shareid,this);
		}
	}
	@Override
	public void done(Report report) {
		if (report.reportHeader.generalType == Report.types.general.combat) {
			switch (report.reportHeader.combatType) {
			case Report.types.combat.scout:
			case Report.types.combat.raidDungeon:
			case Report.types.combat.plunder:
			case Report.types.combat.siege:
				Log.v("ShowReport",report.toString());
				setField(R.id.share,report.share);
				setField(R.id.when,(new Date(report.reportHeader.timestamp)).toString());
				setField(R.id.objType,report.objType);
				setField(R.id.type,"type:"+report.reportHeader.generalType);
				setupHalf(side1,report.attacker,R.string.trapped);
				setupHalf(side2,report.defender,R.string.fortified);
				break;
			default:
				Log.v(TAG,"unknown combat type: "+report.reportHeader.combatType);
			}
		} else {
			Log.e(TAG,"not a combat report");
		}
	}
	private void setupHalf(ViewGroup side, ReportHalf half, int altered_id) {
		Log.v(TAG,"half:"+half);
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
			UnitInfo u = units2[i];
			ViewGroup unit = (ViewGroup) getLayoutInflater().inflate(R.layout.unit_report, v,false);
			int imageid = AndroidEnums.getUnitImage(u.type);
			((ImageView)unit.findViewById(R.id.image)).setImageResource(imageid);
			setField(unit,R.id.ordered,""+u.ordered);
			setField(unit,R.id.neutralized,""+u.s);
			setField(unit,R.id.lost,""+(u.ordered - u.survived));
			setField(unit,R.id.survived,""+u.survived);
			Log.v("ShowReport","count: "+v.getChildCount()+"v:"+v+"unit:"+unit);
			v.addView(unit);
			Log.v("ShowReport","count:"+v.getChildCount());
			Log.v(TAG,String.format("type:%d s:%d ordered:%d survived:%d",u.type,u.s,u.ordered,u.survived));
		}
	}
	private void setField(ViewGroup v,int id,String value) {
		((TextView)v.findViewById(id)).setText(value);
	}
	private void setField(int id,String value) {
		((TextView)findViewById(id)).setText(value);
	}
}
