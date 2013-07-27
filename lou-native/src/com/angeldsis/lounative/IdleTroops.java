package com.angeldsis.lounative;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;

import com.angeldsis.louapi.LouState.City;
import com.angeldsis.louapi.RPC;
import org.eclipse.swt.custom.ScrolledComposite;

public class IdleTroops extends Shell {
	private static final String TAG = "IdleTroops";
	private Text txtCityName;
	private Text txtZerks;
	private Text text;
	private Text text_1;
	RPC rpc;
	private ScrolledComposite list;
	CoreSession session;
	public IdleTroops(RPCWrap rpc, CoreSession session) {
		this.session = session;
		this.rpc = rpc;
		setLayout(new GridLayout(3, false));
		
		txtCityName = new Text(this, SWT.BORDER | SWT.READ_ONLY);
		txtCityName.setText("city name");
		txtCityName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		txtZerks = new Text(this, SWT.BORDER | SWT.READ_ONLY);
		txtZerks.setText("zerks");
		txtZerks.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(this, SWT.NONE);
		
		text = new Text(this, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		text_1 = new Text(this, SWT.BORDER);
		text_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(this, SWT.NONE);
		
		list = new ScrolledComposite(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		list.setExpandHorizontal(true);
		list.setExpandVertical(true);
		list.setLayout(new GridLayout(3, false));
		
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		rpc.setDefenseOverviewEnabled(true);
	}
	protected void checkSubclass() {
	}
	public void onDefenseOverviewUpdate() {
		Composite container = new Composite(list,SWT.NONE);
		container.setLayout(new GridLayout(3, false));
		//container.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		for (final City c : rpc.state.cities.values()) {
			int zerks = 0;
			if (c.units != null) {
				if (c.units[6] != null) {
					zerks = c.units[6].c;
				}
			}
			
			if (zerks == 0) continue;
			
			Text t1 = new Text(container,SWT.BORDER);
			t1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			t1.setText(c.name);
			Text t2 = new Text(container,SWT.BORDER);
			t2.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false,1,1));
			t2.setText(""+zerks);
			
			Button b3 = new Button(container,0);
			b3.setText("change to city");
			if (rpc.state.currentCity == c) b3.setEnabled(false);
			b3.addSelectionListener(new Clicker(){
				@Override public void clicked() {
					rpc.state.changeCity(c);
				}});
		}
		list.setContent(container);
		this.layout();
	}
	public void cityChanged() {
		onDefenseOverviewUpdate();
	}
	@Override public void dispose() {
		session.idleTroops = null;
		rpc.setDefenseOverviewEnabled(false);
		super.dispose();
	}
}
