package com.angeldsis.lou;

import java.util.ArrayList;
import java.util.Iterator;

import com.angeldsis.louapi.IncomingAttack;
import com.angeldsis.louapi.data.Coord;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class IncomingAttacks extends SessionUser {
	private static final String TAG = "IncomingAttacks";
	TextView city;
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.incoming_attacks);
		city = (TextView) findViewById(R.id.current_city);
	}
	public void session_ready() {
		Log.v(TAG,""+session.state.incoming_attacks.size());
		TableLayout l = (TableLayout) findViewById(R.id.table);
		addList(l,session.state.incoming_attacks);
		super.session_ready();
	}
	void addList(ViewGroup parent,ArrayList<IncomingAttack> list) {
		Iterator<IncomingAttack> i = list.iterator();
		while (i.hasNext()) {
			final IncomingAttack a = i.next();
			TableRow row = (TableRow) getLayoutInflater().inflate(R.layout.incoming_attack, parent, false);
			((TextView)row.findViewWithTag("defender")).setText(a.defender);
			String fmt = a.targetCityName;
			if (a.targetLocation != null) fmt += " "+a.targetLocation.format();
			setField(row,"target",fmt);
			((Button)row.findViewWithTag("target")).setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					Bundle args = acct.toBundle();
					Coord c = a.targetLocation;
					args.putInt("x",c.x);
					args.putInt("y",c.y);
					Intent i = new Intent(IncomingAttacks.this,ShowCoord.class);
					i.putExtras(args);
					startActivity(i);
					Log.v(TAG,a.targetCityName);
				}});
			setField(row,"attacker",a.sourcePlayerName);
			if (a.sourceAlliance != null) setField(row,"alliance",a.sourceAlliance.name);
			else setField(row,"alliance","null???");
			setField(row,"source",a.sourceCityName);
			if (a.total_strength_attacker > 0) {
				setField(row,"tsattacker",""+a.total_strength_attacker);
			} else {
				setField(row,"tsattacker","?");
			}
			if (a.total_strength_defender > 0) {
				setField(row,"tsdefender",""+a.total_strength_defender);
			} else {
				setField(row,"tsdefender","?");
			}
			setField(row,"nextwave",""+session.rpc.state.stepToString(a.end));
			parent.addView(row);
		}
	}
	void setField(TableRow row,String tag,String value) {
		((TextView)row.findViewWithTag(tag)).setText(value);
	}
	@Override public void onCityChanged() {
		city.setText(session.state.currentCity.name);
	}
}
