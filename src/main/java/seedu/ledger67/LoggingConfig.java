package seedu.ledger67;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Configuration for enhanced logging in Ledger67.
 * Provides structured logging with different levels and output formats.
 */
public class LoggingConfig {
    
    private static final String LOG_FILE_PATH = "logs/ledger67.log";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * Sets up the logging configuration for the application.
     * Should be called at application startup.
     */
    public static void setup() {
        try {
            // Create logs directory if it doesn't exist
            java.nio.file.Files.createDirectories(
                java.nio.file.Paths.get("logs"));
            
            // Get the root logger
            Logger rootLogger = Logger.getLogger("");
            
            // Remove default console handlers
            for (Handler handler : rootLogger.getHandlers()) {
                rootLogger.removeHandler(handler);
            }
            
            // Create file handler with rotation (5MB max, 5 files)
            FileHandler fileHandler = new FileHandler(
                LOG_FILE_PATH, 
                5 * 1024 * 1024, // 5MB
                5,               // 5 backup files
                true             // append
            );
            
            // Use JSON formatter for structured logging
            fileHandler.setFormatter(new JsonFormatter());
            fileHandler.setLevel(Level.ALL);
            
            // Create console handler with simple format
            Handler consoleHandler = new java.util.logging.ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            consoleHandler.setLevel(Level.INFO);
            
            // Add handlers to root logger
            rootLogger.addHandler(fileHandler);
            rootLogger.addHandler(consoleHandler);
            
            // Set root logger level
            rootLogger.setLevel(Level.ALL);
            
            // Log startup
            Logger.getLogger(LoggingConfig.class.getName())
                .info(() -> String.format("Logging system initialized - logFile: %s, timestamp: %s",
                    LOG_FILE_PATH,
                    LocalDateTime.now().format(TIMESTAMP_FORMATTER)));
            
        } catch (IOException e) {
            System.err.println("Failed to initialize logging: " + e.getMessage());
            // Fallback to basic logging
            Logger.getLogger(LoggingConfig.class.getName())
                .warning("Using fallback logging configuration");
        }
    }
    
    /**
     * JSON formatter for structured logging.
     * Outputs logs in JSON format for easier parsing and analysis.
     */
    private static class JsonFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            Map<String, Object> logEntry = new HashMap<>();
            
            logEntry.put("timestamp", 
                LocalDateTime.now().format(TIMESTAMP_FORMATTER));
            logEntry.put("level", record.getLevel().getName());
            logEntry.put("logger", record.getLoggerName());
            logEntry.put("message", record.getMessage());
            logEntry.put("thread", Thread.currentThread().getName());
            
            // Add method and class information
            if (record.getSourceClassName() != null) {
                logEntry.put("class", record.getSourceClassName());
            }
            if (record.getSourceMethodName() != null) {
                logEntry.put("method", record.getSourceMethodName());
            }
            
            // Add parameters if present
            if (record.getParameters() != null && record.getParameters().length > 0) {
                logEntry.put("parameters", record.getParameters());
            }
            
            // Add exception if present
            if (record.getThrown() != null) {
                Map<String, Object> exceptionInfo = new HashMap<>();
                exceptionInfo.put("type", record.getThrown().getClass().getName());
                exceptionInfo.put("message", record.getThrown().getMessage());
                
                // Include stack trace (first 5 lines)
                StackTraceElement[] stackTrace = record.getThrown().getStackTrace();
                if (stackTrace.length > 0) {
                    String[] traceLines = new String[Math.min(5, stackTrace.length)];
                    for (int i = 0; i < traceLines.length; i++) {
                        traceLines[i] = stackTrace[i].toString();
                    }
                    exceptionInfo.put("stackTrace", traceLines);
                }
                
                logEntry.put("exception", exceptionInfo);
            }
            
            // Convert to JSON (simple implementation)
            return toJson(logEntry) + System.lineSeparator();
        }
        
        private String toJson(Map<String, Object> map) {
            StringBuilder json = new StringBuilder("{");
            boolean first = true;
            
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (!first) {
                    json.append(",");
                }
                first = false;
                
                json.append("\"").append(entry.getKey()).append("\":");
                
                Object value = entry.getValue();
                if (value == null) {
                    json.append("null");
                } else if (value instanceof String) {
                    json.append("\"").append(escapeJson((String) value)).append("\"");
                } else if (value instanceof Number || value instanceof Boolean) {
                    json.append(value);
                } else if (value instanceof Object[]) {
                    json.append(arrayToJson((Object[]) value));
                } else {
                    json.append("\"").append(escapeJson(value.toString())).append("\"");
                }
            }
            
            json.append("}");
            return json.toString();
        }
        
        private String arrayToJson(Object[] array) {
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < array.length; i++) {
                if (i > 0) {
                    json.append(",");
                }
                if (array[i] instanceof String) {
                    json.append("\"").append(escapeJson((String) array[i])).append("\"");
                } else {
                    json.append(array[i]);
                }
            }
            json.append("]");
            return json.toString();
        }
        
        private String escapeJson(String str) {
            return str.replace("\\", "\\\\")
                     .replace("\"", "\\\"")
                     .replace("\n", "\\n")
                     .replace("\r", "\\r")
                     .replace("\t", "\\t");
        }
    }
    
    /**
     * Logs a structured info message with additional context.
     * 
     * @param logger the logger instance
     * @param message the log message
     * @param context additional context as key-value pairs
     */
    public static void info(Logger logger, String message, Map<String, Object> context) {
        logger.log(Level.INFO, message, new Object[]{context});
    }
    
    /**
     * Logs a structured warning message with additional context.
     * 
     * @param logger the logger instance
     * @param message the log message
     * @param context additional context as key-value pairs
     */
    public static void warning(Logger logger, String message, Map<String, Object> context) {
        logger.log(Level.WARNING, message, new Object[]{context});
    }
    
    /**
     * Logs a structured error message with additional context.
     * 
     * @param logger the logger instance
     * @param message the log message
     * @param context additional context as key-value pairs
     */
    public static void error(Logger logger, String message, Map<String, Object> context) {
        logger.log(Level.SEVERE, message, new Object[]{context});
    }
    
    /**
     * Logs a user action with structured context.
     * 
     * @param logger the logger instance
     * @param action the user action (e.g., "add_transaction")
     * @param userId the user identifier (can be null for anonymous)
     * @param details action details
     */
    public static void userAction(Logger logger, String action, String userId, Map<String, Object> details) {
        Map<String, Object> context = new HashMap<>(details);
        context.put("action", action);
        if (userId != null) {
            context.put("userId", userId);
        }
        context.put("type", "user_action");
        
        info(logger, "User action: " + action, context);
    }
    
    /**
     * Logs a performance metric.
     * 
     * @param logger the logger instance
     * @param operation the operation being measured
     * @param durationMs the duration in milliseconds
     * @param additionalInfo additional performance information
     */
    public static void performance(Logger logger, String operation, long durationMs, 
                                  Map<String, Object> additionalInfo) {
        Map<String, Object> context = new HashMap<>(additionalInfo);
        context.put("operation", operation);
        context.put("durationMs", durationMs);
        context.put("type", "performance");
        
        info(logger, "Performance: " + operation + " took " + durationMs + "ms", context);
    }
    
    /**
     * Logs an audit event (for security/compliance).
     * 
     * @param logger the logger instance
     * @param event the audit event
     * @param severity the severity level (INFO, WARNING, SEVERE)
     * @param details event details
     */
    public static void audit(Logger logger, String event, Level severity, 
                            Map<String, Object> details) {
        Map<String, Object> context = new HashMap<>(details);
        context.put("event", event);
        context.put("type", "audit");
        
        logger.log(severity, "Audit: " + event, new Object[]{context});
    }
    
    // Private constructor to prevent instantiation
    private LoggingConfig() {
        throw new AssertionError("LoggingConfig class should not be instantiated.");
    }
}