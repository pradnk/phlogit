package com.intuit.project.phlogit.socialnetwork;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.intuit.project.phlogit.R;


public class ErrorActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String error = getIntent().getStringExtra("error");
				
		if(TextUtils.isEmpty(error)) {
			error = "Bummer! We had an error. Please try again. Make sure to check your internet settings!";
		}
		setContentView(R.layout.error_view);
		TextView tv = (TextView)findViewById(R.id.error);
		tv.setText(error);
	}
}
