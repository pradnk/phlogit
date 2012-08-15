package org.acra;


/**
 * This is a helper class which is used to change the reporting
 * mode for ErrorReporter.Since the setter method is package protected,
 * this util class is created in the same package.
 * 
 * @author vmishra
 */
public class ErrorReporterUtil {

	public static void changeReportingMode(ErrorReporter errorReporter,
			ReportingInteractionMode newReportingInteractionMode)
	{
		errorReporter.setReportingInteractionMode(newReportingInteractionMode);
	}

	public static void handleError(Throwable e, String extraInfo) {
		ErrorReporter.getInstance().handleSilentException(e, extraInfo, false);
	}

}
