package com.angeldsis.lou.reports;

import java.util.Date;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.angeldsis.lou.AndroidEnums;
import com.angeldsis.lou.R;
import com.angeldsis.lou.SessionUser;
import com.angeldsis.louapi.RPC.ReportCallback;
import com.angeldsis.louapi.Report;
import com.angeldsis.louapi.Report.ReportHalf;
import com.angeldsis.louapi.Report.UnitInfo;

public class ShowReport extends SessionUser implements ReportCallback, OnClickListener {
	private static final String TAG = "ShowReport";
	private ViewGroup sides;
	//ViewGroup side1,side2;
	TextView subject;
	ImageView outcome;
	@Override public void onCreate(Bundle sis) {
		super.onCreate(sis);
		setContentView(R.layout.show_report);
		sides = (ViewGroup) findViewById(R.id.sides);
		subject = (TextView) findViewById(R.id.subject);
		outcome = (ImageView) findViewById(R.id.outcome);
		findViewById(R.id.share).setOnClickListener(this);
		LayoutInflater i = getLayoutInflater();
	}
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void copyReport(CharSequence charSequence) {
		ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("Shared report",charSequence);
		clipboard.setPrimaryClip(clip);
		Toast.makeText(this, "Share string copied", Toast.LENGTH_SHORT).show();
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
		sides.removeAllViews();
		if (report.reportHeader.generalType == Report.types.general.combat) {
			switch (report.reportHeader.combatType) {
			case Report.types.combat.siege:
				setField(R.id.claim,String.format("claim power %d->%d",report.oldClaimPower,report.claimPower));
			case Report.types.combat.scout:
			case Report.types.combat.raidDungeon:
			case Report.types.combat.plunder:
			case Report.types.combat.assault:
			case Report.types.combat.raidBoss:
				Log.v("ShowReport",report.toString());
				subject.setText(report.reportHeader.toString()); // FIXME, adjust the formating
				if (report.reportHeader.image != null) {
					outcome.setImageResource(AndroidEnums.getReportIcon(report.reportHeader));
				}
				setField(R.id.share,"Share report: "+report.formatShareString());
				
				// FIXME, it shows in local right now, but clearly says the timezone
				setField(R.id.when,(new Date(report.reportHeader.timestamp)).toString());
				setField(R.id.objType,report.objType);
				setField(R.id.type,"type:"+report.reportHeader.generalType+" "+report.reportHeader.combatType);
				// FIXME, handle things better
				setupSection(report.attacker,true);
				setupSection(report.defender,false);
				break;
			default:
				setField(R.id.type,"type:"+report.reportHeader.generalType+" "+report.reportHeader.combatType);
				Log.v(TAG,"unknown combat type: "+report.reportHeader.combatType);
			}
		} else {
			Log.e(TAG,"not a combat report");
		}
	}
	private void setupSection(ReportHalf half, boolean attacker) {
		int altered_id;
		LayoutInflater i = getLayoutInflater();
		ViewGroup side = (ViewGroup) i.inflate(R.layout.report_half, sides,false);
		View info = side.findViewById(R.id.attackerInfo);
		if (attacker) {
			altered_id = R.string.trapped;
		} else {
			info.setVisibility(View.GONE);
			altered_id = R.string.fortified;
		}
		sides.addView(side);
		//Log.v(TAG,"half:"+half);
		if (half == null) {
			((TextView)side.findViewById(R.id.player_name)).setText("half null");
			return;
		}
		((TextView)side.findViewById(R.id.player_name)).setText(half.player);
		((TextView)side.findViewById(R.id.alliance_name)).setText(half.alliance);
		((TextView)side.findViewById(R.id.city)).setText(half.cityname);
		((TextView)side.findViewById(R.id.location)).setText(half.coord.format2());
		ViewGroup units = (ViewGroup) side.findViewById(R.id.units);
		((TextView)side.findViewById(R.id.altered)).setText(altered_id);
		if (half.units != null) setupUnits(units,half.units);
	}
	private void setupUnits(ViewGroup v, UnitInfo[] units2) {
		Log.v(TAG,"setupUnits("+v+","+units2+")");
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
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.share:
			if (Build.VERSION.SDK_INT > 11) copyReport(((TextView)v).getText());
		}
	}
}
