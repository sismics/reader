package com.sismics.util.log4j;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Memory appender for Log4J.
 *
 * @author jtremeaux
 */
public class MemoryAppender extends AppenderSkeleton {

    /**
     * Maximum size of the queue.
     */
    private int size;
    
    /**
     * Queue of log entries.
     */
    private final Queue<LogEntry> logList = new ConcurrentLinkedQueue<LogEntry>();

    @Override
    public boolean requiresLayout() {
        return false;
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
    }

    @Override
    public synchronized void append(LoggingEvent event) {
        while (logList.size() > size) {
            logList.remove();
        }
        if (closed) {
            LogLog.warn("This appender is already closed, cannot append event.");
            return;
        }
        
        String loggerName = getLoggerName(event);

        LogEntry logEntry = new LogEntry(System.currentTimeMillis(), event.getLevel().toString(), loggerName, event.getMessage().toString());
        logList.add(logEntry);
    }

    /**
     * Extracts the class name of the logger, without the package name.
     * 
     * @param event Event
     * @return Class name
     */
    private String getLoggerName(LoggingEvent event) {
        int index = event.getLoggerName().lastIndexOf('.');

        return (index > -1) ?
            event.getLoggerName().substring(index + 1) :
            event.getLoggerName();
    }

    /**
     * Getter of logList.
     *
     * @return logList
     */
    public Queue<LogEntry> getLogList() {
        return logList;
    }

    /**
     * Setter of size.
     *
     * @param size size
     */
    public void setSize(int size) {
        this.size = size;
    }
}