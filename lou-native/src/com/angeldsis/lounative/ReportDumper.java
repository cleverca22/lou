package com.angeldsis.lounative;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.angeldsis.louapi.Log;
import com.angeldsis.louapi.RPC;
import com.angeldsis.louapi.RPC.ReportCallback;
import com.angeldsis.louapi.Report;
import com.angeldsis.louapi.RPC.ReportHeaderCallback;
import com.angeldsis.louapi.ReportHeader;

public class ReportDumper implements ReportHeaderCallback, ReportCallback {
	private static final String TAG = "ReportDumper";
	ArrayList<ReportHeader> queue;
	RPC rpc;
	int start,end,total;
	private FileOutputStream os;
	public ReportDumper(RPCWrap rpc2) {
		rpc = rpc2;
		queue = new ArrayList<ReportHeader>();
	}
	public void dumpReports(File out) {
		try {
			os = new FileOutputStream(out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		total = rpc.state.unviewed_reports + rpc.state.viewed_reports;
		start = 0;
		end = 99;
		recurse();
	}
	private void recurse() {
		Log.v(TAG,String.format("start: %d, size: %d, total: %d saved: %d",start,end,total,queue.size()));
		rpc.ReportGetHeader("kashikoi",-1,start,end,1,false,200575,this);
	}
	@Override
	public void done(ReportHeader[] list) {
		for (ReportHeader h : list) {
			queue.add(h);
		}
		if (queue.size() < total) {
			start = start + 100;
			end = start + 99;
			recurse();
		} else processList();
	}
	private void processList() {
		Log.v(TAG,queue.size()+" remaining");
		if (queue.size() == 0) {
			try {
				os.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		ReportHeader h = queue.remove(0);
		if (h.generalType == Report.types.general.combat) {
			if ((h.combatType == Report.types.combat.raidDungeon) || 
					(h.combatType == Report.types.combat.raidBoss)) {
				processList();
				return;
			}
			rpc.GetReport(h.id, this);
		} else processList();
	}
	@Override
	public void done(Report report) {
		String msg = report.reportHeader.toString()+"\n";
		try {
			os.write(msg.getBytes());
			Thread.sleep(1200);
			processList();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
