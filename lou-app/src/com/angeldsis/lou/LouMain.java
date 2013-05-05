package com.angeldsis.lou;

import com.angeldsis.lou.home.Loading;
import com.nullwire.trace.ExceptionHandler;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class LouMain extends FragmentActivity {
	private static final String TAG = "LouMain";
	//private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAznzSvQ6HIxOE5lZwOf20F8/VbrTMcCm/cdyX3RKFt9bGYbeJPUfzSejt+g1H/EygbWigfE9F7gGOD8TcIpYxOzsskIrbb0gu53opK8mRiCYps88o35YbBSYF3p+GWhohzD9YDSpXdJMXBCwWAJPDEJrS0wPyUqp0ROGiA6Nj8KWj5C/XX/hxJR5EymMa5NIuHDcmc22P4CH5DnQBjU9D8cvKJOdIs8WUwL3fP4r2cyBewrLjreFBkUIpoKIkbVerYt91+8QAftqMzXItgR80lRW48eeabHQbJUag8yhxLd0+a6no7UM8MpRoZqNPc7FMrfHyJNHo7NH/cc0ROnvlkwIDAQAB";
	//private static final byte[] SALT = new byte[] { -100, -13, 60, -110, 29, -15, 65, 121, -65, 80, -13, -113, -62, -104, -52, 61, 70, -95, -114, -19 };

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		ExceptionHandler.register(this,"http://ext.earthtools.ca/backtrace.php");
		// super may re-create fragments without warning
		super.onCreate(savedInstanceState);
		// Debug.startMethodTracing();
		
		setTheme(SessionUser.getCurrentTheme(this));
		setContentView(R.layout.main);
		if (savedInstanceState != null) return;
		
		getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, new Loading()).commit();
	}
	protected void onStart() {
		super.onStart();
		Log.v(TAG, "onStart");
	}
		// AudioTrack click = new
		// AudioTrack(AudioManager.STREAM_MUSIC,44100,AudioTrack.CHANNEL_OUT_MONO,ENCODING_PCM_16BIT,20096,MODE_STATIC);
		// byte[] data;
		// click.write(data,0,20096);

	protected void onDestroy() {
		super.onDestroy();
		Log.v(TAG,"onDestroy");
	}
}
