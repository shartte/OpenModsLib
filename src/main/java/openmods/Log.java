package openmods;

import cpw.mods.fml.common.FMLLog;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

public final class Log {
	private Log() {}

  /**
   * @see openmods.OpenMods#preInit(cpw.mods.fml.common.event.FMLPreInitializationEvent)
   */
	private static Logger logger = FMLLog.getLogger();

	private static final Throwable stackInfo = new Throwable();

  public static void setLogger(Logger logger) {
    Log.logger = logger;
  }

	private static String getLogLocation(Throwable t) {
		// first element is always log function

		// maybe faster but definitely unsafe implementation:
		// JavaLangAccess access = SharedSecrets.getJavaLangAccess();
		// if (access.getStackTraceDepth(t) < 2) return "";
		// final StackTraceElement caller = access.getStackTraceElement(t, 1);

		final StackTraceElement[] stack = t.getStackTrace();
		if (stack.length < 2) return "";
		final StackTraceElement caller = stack[1];
		return caller.getClassName() + "." + caller.getMethodName() + "(" + caller.getFileName() + ":" + caller.getLineNumber() + "): ";
	}

	private static void logWithCaller(Throwable callerStack, Level level, String format, Object... data) {
		logger.log(level, getLogLocation(callerStack) + String.format(format, data));
	}

	public static void log(Level level, String format, Object... data) {
		logWithCaller(stackInfo.fillInStackTrace(), level, format, data);
	}

	public static void severe(String format, Object... data) {
		logWithCaller(stackInfo.fillInStackTrace(), Level.FATAL, format, data);
	}

	public static void warn(String format, Object... data) {
		logWithCaller(stackInfo.fillInStackTrace(), Level.WARN, format, data);
	}

	public static void info(String format, Object... data) {
		logWithCaller(stackInfo.fillInStackTrace(), Level.INFO, format, data);
	}

	public static void fine(String format, Object... data) {
		logWithCaller(stackInfo.fillInStackTrace(), Level.DEBUG, format, data);
	}

	public static void finer(String format, Object... data) {
		logWithCaller(stackInfo.fillInStackTrace(), Level.TRACE, format, data);
	}

	public static void log(Level level, Throwable ex, String format, Object... data) {
		logger.log(level, String.format(format, data), ex);
	}

	public static void severe(Throwable ex, String format, Object... data) {
		log(Level.FATAL, ex, format, data);
	}

	public static void warn(Throwable ex, String format, Object... data) {
		log(Level.WARN, ex, format, data);
	}
}
