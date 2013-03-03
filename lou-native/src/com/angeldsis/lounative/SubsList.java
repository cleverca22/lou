package com.angeldsis.lounative;

import java.util.ArrayList;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.SWT;

import com.angeldsis.louapi.Log;
import com.angeldsis.louapi.RPC;
import com.angeldsis.louapi.data.SubRequest;

public class SubsList extends Shell {
	ArrayList<Button> buttons;
	RPC rpc;
	public SubsList(RPCWrap rpc, Display display) {
		this.rpc = rpc;
		setLayout(new GridLayout(1, false));
		
		buttons = new ArrayList<Button>();
		setText("subs list");
	}

	public void onSubListChanged() {
		for (Button b : buttons) {
			b.dispose();
		}
		buttons.clear();
		ArrayList<SubRequest> requests = rpc.state.subs;
		for (SubRequest s : requests) {
			if (s.state != 2) continue;
			Button b = new Button(this,SWT.NONE);
			Log.v("SubsList",b.toString());
			b.setText(s.giver.getName());
			b.addSelectionListener(new clicker(s));
			buttons.add(b);
			b.setVisible(true);
			Log.v("SubsList", buttons.toString());
		}
		this.layout();
	}
	private class clicker implements SelectionListener {
		SubRequest s;
		clicker(SubRequest s) {
			this.s = s;
		}
		@Override
		public void widgetSelected(SelectionEvent e) {
			Log.v("SubsList",s.toString());
			SubsList.this.rpc.CreateSubstitutionSession(s);
		}
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
	protected void checkSubclass() {}
}
