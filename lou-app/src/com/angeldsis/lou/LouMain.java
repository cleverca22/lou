package com.angeldsis.lou;

import com.angeldsis.lou.home.Loading;
import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.Policy;
import com.google.android.vending.licensing.ServerManagedPolicy;

import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class LouMain extends FragmentActivity {
	private static final String TAG = "LouMain";
	private LicenseChecker mChecker;
	private MyLicenseCheckerCallback mLicenseCheckerCallback;
	private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAznzSvQ6HIxOE5lZwOf20F8/VbrTMcCm/cdyX3RKFt9bGYbeJPUfzSejt+g1H/EygbWigfE9F7gGOD8TcIpYxOzsskIrbb0gu53opK8mRiCYps88o35YbBSYF3p+GWhohzD9YDSpXdJMXBCwWAJPDEJrS0wPyUqp0ROGiA6Nj8KWj5C/XX/hxJR5EymMa5NIuHDcmc22P4CH5DnQBjU9D8cvKJOdIs8WUwL3fP4r2cyBewrLjreFBkUIpoKIkbVerYt91+8QAftqMzXItgR80lRW48eeabHQbJUag8yhxLd0+a6no7UM8MpRoZqNPc7FMrfHyJNHo7NH/cc0ROnvlkwIDAQAB";
	private static final byte[] SALT = new byte[] { -100, -13, 60, -110, 29, -15, 65, 121, -65, 80, -13, -113, -62, -104, -52, 61, 70, -95, -114, -19 };

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// super may re-create fragments without warning
		super.onCreate(savedInstanceState);
		// Debug.startMethodTracing();
		
		if (savedInstanceState != null) return;
		
		setContentView(R.layout.main);
		getSupportFragmentManager().beginTransaction().add(R.id.main_frame, new Loading()).commit();
		String deviceId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
		mLicenseCheckerCallback = new MyLicenseCheckerCallback();
		mChecker = new LicenseChecker(this, new ServerManagedPolicy(this,
						new AESObfuscator(SALT,getPackageName(),deviceId)),BASE64_PUBLIC_KEY);
		mChecker.checkAccess(mLicenseCheckerCallback);
	}
	protected void onStart() {
		super.onStart();
		Log.v(TAG, "onStart");
	}

		// AudioTrack click = new
		// AudioTrack(AudioManager.STREAM_MUSIC,44100,AudioTrack.CHANNEL_OUT_MONO,ENCODING_PCM_16BIT,20096,MODE_STATIC);
		// byte[] data;
		// click.write(data,0,20096);

	private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
		@Override
		public void allow(int reason) {
			if (isFinishing()) return;
			Log.v(TAG,"Allowed");
		}
		@Override
		public void dontAllow(int reason) {
			if (isFinishing()) return;
			if (reason == Policy.RETRY) {
				// FIXME, handle this better
				// disabled so kindle can handle it
				//mChecker.checkAccess(mLicenseCheckerCallback);
				Log.v(TAG,"need to retry");
			}else {
				// FIXME give a better error
				Log.v(TAG,"not allowed");
				LouMain.this.finish();
			}
		}
		@Override
		public void applicationError(int errorCode) {
			if (isFinishing()) return;
			Log.v(TAG,"error "+errorCode);
		}
	}
	protected void onDestroy() {
		super.onDestroy();
		mChecker.onDestroy();
	}
}
