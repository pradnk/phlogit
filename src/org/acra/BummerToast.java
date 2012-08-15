/*
 * Copyright 2010 Intuit, Inc. All rights reserved.
 */
package org.acra;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.Toast;

public class BummerToast extends Toast {

	public BummerToast(Context context) {
		super(context);
		try {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			setDuration(Toast.LENGTH_LONG);
			setGravity(Gravity.TOP | Gravity.FILL_HORIZONTAL | Gravity.FILL_VERTICAL, 0, 0);
		} catch (Throwable r) {
			return;
		}
	}
}
