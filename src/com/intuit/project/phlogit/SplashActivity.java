package com.intuit.project.phlogit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;

public class SplashActivity extends Activity {
    private Runnable exit;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        
        exit = new Runnable() {
			
			@Override
			public void run() {
				finish();
				startActivity(new Intent(SplashActivity.this, HomeActivity.class));
			}
		};
        
        ViewGroup background = (ViewGroup)findViewById(R.id.background);
        background.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
				startActivity(new Intent(SplashActivity.this, HomeActivity.class));
			}
		});
        
        handler.postDelayed(exit, 3500);
    }
    
    Handler handler = new Handler();
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	handler.removeCallbacks(exit);
    }
}