package org.acra;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * <p>
 * Provides utilities for manipulating and examining <code>Throwable</code> objects.
 * </p>
 */
public class ExceptionUtils {

	public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

	public static final String[] EMPTY_STRING_ARRAY = new String[0];

	public static final String PATH_SEPARATOR = System.getProperty("path.separator");

	public static final String LINE_SEPARATOR = System.getProperty("line.separator");

	/**
	 * <p>
	 * Used when printing stack frames to denote the start of a wrapped exception.
	 * </p>
	 * <p>
	 * Package private for accessibility by test suite.
	 * </p>
	 */
	static final String WRAPPED_MARKER = " [wrapped] ";

	// Lock object for CAUSE_METHOD_NAMES
	private static final Object CAUSE_METHOD_NAMES_LOCK = new Object();

	/**
	 * <p>
	 * The names of methods commonly used to access a wrapped exception.
	 * </p>
	 */
	private static String[] CAUSE_METHOD_NAMES = {
			"getCause",
			"getNextException",
			"getTargetException",
			"getException",
			"getSourceException",
			"getRootCause",
			"getCausedByException",
			"getNested",
			"getLinkedException",
			"getNestedException",
			"getLinkedCause",
			"getThrowable",
	};

	/**
	 * <p>
	 * The Method object for Java 1.4 getCause.
	 * </p>
	 */
	private static final Method THROWABLE_CAUSE_METHOD;

	/**
	 * <p>
	 * The Method object for Java 1.4 initCause.
	 * </p>
	 */
	private static final Method THROWABLE_INITCAUSE_METHOD;

	static {
		Method causeMethod;
		try {
			causeMethod = Throwable.class.getMethod("getCause", null);
		} catch (Exception e) {
			causeMethod = null;
		}
		THROWABLE_CAUSE_METHOD = causeMethod;
		try {
			causeMethod = Throwable.class.getMethod("initCause", new Class[] {
					Throwable.class
			});
		} catch (Exception e) {
			causeMethod = null;
		}
		THROWABLE_INITCAUSE_METHOD = causeMethod;
	}

	/**
	 * <p>
	 * Public constructor allows an instance of <code>ExceptionUtils</code> to be created, although
	 * that is not normally necessary.
	 * </p>
	 */
	public ExceptionUtils() {
		super();
	}

	/**
	 * Returns the given list as a <code>String[]</code>.
	 * 
	 * @param list a list to transform.
	 * @return the given list as a <code>String[]</code>.
	 */
	private static String[] toArray(List list) {
		return (String[]) list.toArray(new String[list.size()]);
	}

	// -----------------------------------------------------------------------
	/**
	 * <p>
	 * Introspects the <code>Throwable</code> to obtain the cause.
	 * </p>
	 * <p>
	 * The method searches for methods with specific names that return a <code>Throwable</code>
	 * object. This will pick up most wrapping exceptions, including those from JDK 1.4, and
	 * {@link org.apache.commons.lang.exception.NestableException NestableException}. The method
	 * names can be added to using {@link #addCauseMethodName(String)}.
	 * </p>
	 * <p>
	 * The default list searched for are:
	 * </p>
	 * <ul>
	 * <li><code>getCause()</code></li>
	 * <li><code>getNextException()</code></li>
	 * <li><code>getTargetException()</code></li>
	 * <li><code>getException()</code></li>
	 * <li><code>getSourceException()</code></li>
	 * <li><code>getRootCause()</code></li>
	 * <li><code>getCausedByException()</code></li>
	 * <li><code>getNested()</code></li>
	 * </ul>
	 * <p>
	 * In the absence of any such method, the object is inspected for a <code>detail</code> field
	 * assignable to a <code>Throwable</code>.
	 * </p>
	 * <p>
	 * If none of the above is found, returns <code>null</code>.
	 * </p>
	 * 
	 * @param throwable the throwable to introspect for a cause, may be null
	 * @return the cause of the <code>Throwable</code>, <code>null</code> if none found or null
	 *         throwable input
	 * @since 1.0
	 */
	public static Throwable getCause(Throwable throwable) {
		synchronized (CAUSE_METHOD_NAMES_LOCK) {
			return getCause(throwable, CAUSE_METHOD_NAMES);
		}
	}

	/**
	 * <p>
	 * Introspects the <code>Throwable</code> to obtain the cause.
	 * </p>
	 * <ol>
	 * <li>Try known exception types.</li>
	 * <li>Try the supplied array of method names.</li>
	 * <li>Try the field 'detail'.</li>
	 * </ol>
	 * <p>
	 * A <code>null</code> set of method names means use the default set. A <code>null</code> in the
	 * set of method names will be ignored.
	 * </p>
	 * 
	 * @param throwable the throwable to introspect for a cause, may be null
	 * @param methodNames the method names, null treated as default set
	 * @return the cause of the <code>Throwable</code>, <code>null</code> if none found or null
	 *         throwable input
	 * @since 1.0
	 */
	public static Throwable getCause(Throwable throwable, String[] methodNames) {
		if (throwable == null) {
			return null;
		}
		Throwable cause = getCauseUsingWellKnownTypes(throwable);
		if (cause == null) {
			if (methodNames == null) {
				synchronized (CAUSE_METHOD_NAMES_LOCK) {
					methodNames = CAUSE_METHOD_NAMES;
				}
			}
			for (int i = 0; i < methodNames.length; i++) {
				String methodName = methodNames[i];
				if (methodName != null) {
					cause = getCauseUsingMethodName(throwable, methodName);
					if (cause != null) {
						break;
					}
				}
			}

			if (cause == null) {
				cause = getCauseUsingFieldName(throwable, "detail");
			}
		}
		return cause;
	}

	/**
	 * <p>
	 * Introspects the <code>Throwable</code> to obtain the root cause.
	 * </p>
	 * <p>
	 * This method walks through the exception chain to the last element, "root" of the tree, using
	 * {@link #getCause(Throwable)}, and returns that exception.
	 * </p>
	 * <p>
	 * From version 2.2, this method handles recursive cause structures that might otherwise cause
	 * infinite loops. If the throwable parameter has a cause of itself, then null will be returned.
	 * If the throwable parameter cause chain loops, the last element in the chain before the loop
	 * is returned.
	 * </p>
	 * 
	 * @param throwable the throwable to get the root cause for, may be null
	 * @return the root cause of the <code>Throwable</code>, <code>null</code> if none found or null
	 *         throwable input
	 */
	public static Throwable getRootCause(Throwable throwable) {
		List list = getThrowableList(throwable);
		return (list.size() < 2 ? null : (Throwable) list.get(list.size() - 1));
	}

	/**
	 * <p>
	 * Finds a <code>Throwable</code> for known types.
	 * </p>
	 * <p>
	 * Uses <code>instanceof</code> checks to examine the exception, looking for well known types
	 * which could contain chained or wrapped exceptions.
	 * </p>
	 * 
	 * @param throwable the exception to examine
	 * @return the wrapped exception, or <code>null</code> if not found
	 */
	private static Throwable getCauseUsingWellKnownTypes(Throwable throwable) {
		if (throwable instanceof SQLException) {
			return ((SQLException) throwable).getNextException();
		} else if (throwable instanceof InvocationTargetException) {
			return ((InvocationTargetException) throwable).getTargetException();
		} else {
			return null;
		}
	}

	/**
	 * <p>
	 * Finds a <code>Throwable</code> by method name.
	 * </p>
	 * 
	 * @param throwable the exception to examine
	 * @param methodName the name of the method to find and invoke
	 * @return the wrapped exception, or <code>null</code> if not found
	 */
	private static Throwable getCauseUsingMethodName(Throwable throwable, String methodName) {
		Method method = null;
		try {
			method = throwable.getClass().getMethod(methodName, null);
		} catch (NoSuchMethodException ignored) {
			// exception ignored
		} catch (SecurityException ignored) {
			// exception ignored
		}

		if (method != null && Throwable.class.isAssignableFrom(method.getReturnType())) {
			try {
				return (Throwable) method.invoke(throwable, EMPTY_OBJECT_ARRAY);
			} catch (IllegalAccessException ignored) {
				// exception ignored
			} catch (IllegalArgumentException ignored) {
				// exception ignored
			} catch (InvocationTargetException ignored) {
				// exception ignored
			}
		}
		return null;
	}

	/**
	 * <p>
	 * Finds a <code>Throwable</code> by field name.
	 * </p>
	 * 
	 * @param throwable the exception to examine
	 * @param fieldName the name of the attribute to examine
	 * @return the wrapped exception, or <code>null</code> if not found
	 */
	private static Throwable getCauseUsingFieldName(Throwable throwable, String fieldName) {
		Field field = null;
		try {
			field = throwable.getClass().getField(fieldName);
		} catch (NoSuchFieldException ignored) {
			// exception ignored
		} catch (SecurityException ignored) {
			// exception ignored
		}

		if (field != null && Throwable.class.isAssignableFrom(field.getType())) {
			try {
				return (Throwable) field.get(throwable);
			} catch (IllegalAccessException ignored) {
				// exception ignored
			} catch (IllegalArgumentException ignored) {
				// exception ignored
			}
		}
		return null;
	}

	/**
	 * <p>
	 * Returns the list of <code>Throwable</code> objects in the exception chain.
	 * </p>
	 * <p>
	 * A throwable without cause will return an array containing one element - the input throwable.
	 * A throwable with one cause will return an array containing two elements. - the input
	 * throwable and the cause throwable. A <code>null</code> throwable will return an array of size
	 * zero.
	 * </p>
	 * <p>
	 * From version 2.2, this method handles recursive cause structures that might otherwise cause
	 * infinite loops. The cause chain is processed until the end is reached, or until the next item
	 * in the chain is already in the result set.
	 * </p>
	 * 
	 * @see #getThrowableList(Throwable)
	 * @param throwable the throwable to inspect, may be null
	 * @return the array of throwables, never null
	 */
	public static Throwable[] getThrowables(Throwable throwable) {
		List list = getThrowableList(throwable);
		return (Throwable[]) list.toArray(new Throwable[list.size()]);
	}

	/**
	 * <p>
	 * Returns the list of <code>Throwable</code> objects in the exception chain.
	 * </p>
	 * <p>
	 * A throwable without cause will return a list containing one element - the input throwable. A
	 * throwable with one cause will return a list containing two elements. - the input throwable
	 * and the cause throwable. A <code>null</code> throwable will return a list of size zero.
	 * </p>
	 * <p>
	 * This method handles recursive cause structures that might otherwise cause infinite loops. The
	 * cause chain is processed until the end is reached, or until the next item in the chain is
	 * already in the result set.
	 * </p>
	 * 
	 * @param throwable the throwable to inspect, may be null
	 * @return the list of throwables, never null
	 * @since Commons Lang 2.2
	 */
	public static List getThrowableList(Throwable throwable) {
		List list = new ArrayList();
		while (throwable != null && list.contains(throwable) == false) {
			list.add(throwable);
			throwable = ExceptionUtils.getCause(throwable);
		}
		return list;
	}

	// -----------------------------------------------------------------------
	/**
	 * <p>
	 * Gets the stack trace from a Throwable as a String.
	 * </p>
	 * <p>
	 * The result of this method vary by JDK version as this method uses
	 * {@link Throwable#printStackTrace(java.io.PrintWriter)}. On JDK1.3 and earlier, the cause
	 * exception will not be shown unless the specified throwable alters printStackTrace.
	 * </p>
	 * 
	 * @param throwable the <code>Throwable</code> to be examined
	 * @return the stack trace as generated by the exception's
	 *         <code>printStackTrace(PrintWriter)</code> method
	 */
	public static String getStackTrace(Throwable throwable) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		throwable.printStackTrace(pw);
		return sw.getBuffer().toString();
	}

	/**
	 * <p>
	 * Captures the stack trace associated with the specified <code>Throwable</code> object,
	 * decomposing it into a list of stack frames.
	 * </p>
	 * <p>
	 * The result of this method vary by JDK version as this method uses
	 * {@link Throwable#printStackTrace(java.io.PrintWriter)}. On JDK1.3 and earlier, the cause
	 * exception will not be shown unless the specified throwable alters printStackTrace.
	 * </p>
	 * 
	 * @param throwable the <code>Throwable</code> to examine, may be null
	 * @return an array of strings describing each stack frame, never null
	 */
	public static String[] getStackFrames(Throwable throwable) {
		if (throwable == null) {
			return EMPTY_STRING_ARRAY;
		}
		return getStackFrames(getStackTrace(throwable));
	}

	// -----------------------------------------------------------------------
	/**
	 * <p>
	 * Returns an array where each element is a line from the argument.
	 * </p>
	 * <p>
	 * The end of line is determined by the value of {@link SystemUtils#LINE_SEPARATOR}.
	 * </p>
	 * <p>
	 * Functionality shared between the <code>getStackFrames(Throwable)</code> methods of this and
	 * the {@link org.apache.commons.lang.exception.NestableDelegate} classes.
	 * </p>
	 * 
	 * @param stackTrace a stack trace String
	 * @return an array where each element is a line from the argument
	 */
	static String[] getStackFrames(String stackTrace) {
		String linebreak = LINE_SEPARATOR;
		StringTokenizer frames = new StringTokenizer(stackTrace, linebreak);
		List list = new ArrayList();
		while (frames.hasMoreTokens()) {
			list.add(frames.nextToken());
		}
		return toArray(list);
	}

	/**
	 * <p>
	 * Produces a <code>List</code> of stack frames - the message is not included. Only the trace of
	 * the specified exception is returned, any caused by trace is stripped.
	 * </p>
	 * <p>
	 * This works in most cases - it will only fail if the exception message contains a line that
	 * starts with: <code>&quot;&nbsp;&nbsp;&nbsp;at&quot;.</code>
	 * </p>
	 * 
	 * @param t is any throwable
	 * @return List of stack frames
	 */
	static List getStackFrameList(Throwable t) {
		String stackTrace = getStackTrace(t);
		String linebreak = LINE_SEPARATOR;
		StringTokenizer frames = new StringTokenizer(stackTrace, linebreak);
		List list = new ArrayList();
		boolean traceStarted = false;
		while (frames.hasMoreTokens()) {
			String token = frames.nextToken();
			// Determine if the line starts with <whitespace>at
			int at = token.indexOf("at");
			if (at != -1 && token.substring(0, at).trim().length() == 0) {
				traceStarted = true;
				list.add(token);
			} else if (traceStarted) {
				break;
			}
		}
		return list;
	}
}
