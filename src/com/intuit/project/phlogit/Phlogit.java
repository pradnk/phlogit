package com.intuit.project.phlogit;

import org.acra.ACRA;
import org.acra.ErrorReporter;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.content.Context;

@ReportsCrashes(formKey = "", mode = ReportingInteractionMode.TOAST, customReportContent = {
		ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME,
		ReportField.PHONE_MODEL, ReportField.ANDROID_VERSION, ReportField.TOTAL_MEM_SIZE,
		ReportField.AVAILABLE_MEM_SIZE, ReportField.STACK_TRACE, ReportField.USER_COMMENT,
		ReportField.USER_CRASH_DATE, ReportField.CRASH_OR_DIAGNOSTICS, ReportField.DEV_COMMENT,
		ReportField.DEVICE_ID, ReportField.MARRIED, ReportField.USER_STATE, ReportField.LOGCAT,
		ReportField.USER_ID
}, resNotifTickerText = R.string.app_name, resNotifTitle = R.string.app_name, resNotifText = R.string.app_name, resNotifIcon = android.R.drawable.stat_notify_error, // optional.
// default is a warning sign
resDialogText = R.string.app_name, resDialogIcon = android.R.drawable.ic_dialog_info, // optional.
// default is a warning sign
resDialogTitle = R.string.app_name, // optional. default is your application name
resDialogCommentPrompt = R.string.app_name, // optional. when defined,
// adds a user text field
// input with this text
// resource as a label
resDialogOkToast = R.string.app_name, // optional. displays a Toast message when
// the user accepts to send a report.
resToastText = R.string.app_name)
public class Phlogit extends Application {

	private static Phlogit context = null;

	public Phlogit() {
		if (context == null) {
			context = this;
		}
	}

	public static Context getPhlogitApplicationContext() {
		return context;
	}
	
	@Override
	public void onCreate() {
		// Setup the custom Report Sender.
		ErrorReporter.getInstance().setReportSender(null);
		ACRA.init(this);
		super.onCreate();
	}
}
