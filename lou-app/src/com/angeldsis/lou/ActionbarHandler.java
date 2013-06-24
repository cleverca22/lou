package com.angeldsis.lou;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;

import com.angeldsis.lou.SessionKeeper.Session;
import com.angeldsis.lou.allianceforum.AllianceForumList;
import com.angeldsis.lou.city.SendTrade;
import com.angeldsis.lou.fragments.ChatWindow;
import com.angeldsis.lou.fragments.FoodWarnings;
import com.angeldsis.lou.fragments.ShrineMonitor;
import com.angeldsis.lou.world.DungeonList;
import com.angeldsis.louapi.RPC.GetLockboxURLDone;

public class ActionbarHandler {
	private static final String TAG = "ActionbarHandler";
	public static boolean handleMenu(MenuItem item, final Activity a,AccountWrap acct, Session session) {
		Intent i;
		switch (item.getItemId()) {
		case R.id.open_chat:
			if (a instanceof FragmentUser) {
				FragmentUser fu = (FragmentUser) a;
				FragmentTransaction ft = fu.getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.main_frame, new ChatWindow());
				ft.addToBackStack(null);
				ft.commit();
			} else {
				i = new Intent(a,SingleFragment.class);
				i.putExtras(acct.toBundle());
				i.putExtra("fragment", ChatWindow.class);
				a.startActivity(i);
			}
			return true;
		case R.id.city:
			Log.v(TAG,"opening city view");
			long heapSize = Runtime.getRuntime().maxMemory() / 1024 / 1024;
			if (heapSize > 15) {
				i = new Intent(a,CityView.class);
				i.putExtras(acct.toBundle());
				a.startActivity(i);
			} else {
				AlertDialog.Builder b = new AlertDialog.Builder(a);
				b.setMessage(R.string.low_ram);
				b.setPositiveButton(R.string.ok, null);
				AlertDialog d = b.create();
				d.show();
			}
			return true;
		case R.id.subs:
			i = new Intent(a,Options.class);
			i.putExtras(acct.toBundle());
			a.startActivity(i);
			return true;
		case R.id.options:
			i = new Intent(a,Settings.class);
			Log.v(TAG,"opening settings "+i);
			a.startActivity(i);
			return true;
		case R.id.logout:
			session.logout();
			a.finish();
			return true;
		case R.id.getfunds:
			session.rpc.GetLockboxURL(new GetLockboxURLDone() {
				@Override public void done(String reply) {
					Log.v(TAG,"got url:"+reply);
					Uri location = Uri.parse(reply);
					Intent buyFunds = new Intent(Intent.ACTION_VIEW,location);
					a.startActivity(buyFunds);
				}});
			return true;
		case R.id.allianceForum:
			i = new Intent(a,AllianceForumList.class);
			i.putExtras(acct.toBundle());
			a.startActivity(i);
			return true;
		case R.id.sendTrade:
			i = new Intent(a,SendTrade.class);
			i.putExtras(acct.toBundle());
			a.startActivity(i);
			return true;
		case R.id.bqo:
			i = new Intent(a,BuildQueueOverview.class);
			i.putExtras(acct.toBundle());
			a.startActivity(i);
			return true;
		case R.id.dungeonlist:
			i = new Intent(a,DungeonList.class);
			i.putExtras(acct.toBundle());
			a.startActivity(i);
			return true;
		case R.id.idleunits:
			i = new Intent(a,IdleUnits.class);
			i.putExtras(acct.toBundle());
			a.startActivity(i);
			return true;
		case R.id.update:
			Uri location = Uri.parse("http://andoria.angeldsis.com/apks/LouMain.apk");
			i = new Intent(Intent.ACTION_VIEW,location);
			a.startActivity(i);
			return true;
		case R.id.el_city_list:
			i = new Intent(a,EnlightenedCityList.class);
			i.putExtras(acct.toBundle());
			a.startActivity(i);
			return true;
		case R.id.foodWarning:
			if (a instanceof FragmentUser) {
				FragmentUser fu = (FragmentUser) a;
				FragmentTransaction trans = fu.getSupportFragmentManager().beginTransaction();
				trans.replace(R.id.main_frame, new CityCore());
				trans.replace(R.id.second_frame, new FoodWarnings());
				trans.addToBackStack(null);
				trans.commit();
			} else {
				i = FoodWarnings.getIntent(acct, a);
				a.startActivity(i);
			}
			return true;
		case R.id.cityCore:
			i = new Intent(a,SingleFragment.class);
			i.putExtras(acct.toBundle());
			i.putExtra("fragment", CityCore.class);
			a.startActivity(i);
			return true;
		case R.id.shrine_monitor:
			i = new Intent(a,SingleFragment.class);
			i.putExtras(acct.toBundle());
			i.putExtra("fragment", ShrineMonitor.class);
			a.startActivity(i);
			return true;
		}
		return false;
	}
}
