package com.angeldsis.lou;

import com.angeldsis.lou.home.Webview;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class louLogin extends Fragment {
	static String TAG = "louLogin";
	EditText username,password;
	private CheckBox savepw;
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.v(TAG,"onCreateView");
		ViewGroup v = (ViewGroup) inflater.inflate(R.layout.login_page, container,false);
		username = ((EditText)v.findViewById(R.id.username));
		this.password = ((EditText)v.findViewById(R.id.password));
		this.savepw = ((CheckBox)v.findViewById(R.id.save_pw));
		((Button)v.findViewById(R.id.login)).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				louLogin.this.doLogin(null);
			}
		});
		
		String email = getActivity().getSharedPreferences("main",Context.MODE_PRIVATE).getString("email", null);
		String password = getActivity().getSharedPreferences("main",Context.MODE_PRIVATE).getString("password", null);
		if (email != null && password != null) {
			username.setText(email);
			this.password.setText(password);
			this.savepw.setChecked(true);
		}
		Log.v(TAG,"getting service");
		return v;
	}
	public void doLogin(View view) {
		String username = this.username.getText().toString();
		String password = this.password.getText().toString();
		boolean savePw = savepw.isChecked();
		if (savePw) {
			SharedPreferences.Editor trans = getActivity().getSharedPreferences("main", Context.MODE_PRIVATE).edit();
			trans.putString("email", username);
			trans.putString("password", password);
			trans.commit();
		}
		FragmentTransaction trans = getActivity().getSupportFragmentManager().beginTransaction();
		Webview v = new Webview();
		Bundle b = new Bundle();
		b.putString("username", username);
		b.putString("password", password);
		v.setArguments(b);
		trans.replace(R.id.main_frame, v);
		trans.commit();
	}
}
