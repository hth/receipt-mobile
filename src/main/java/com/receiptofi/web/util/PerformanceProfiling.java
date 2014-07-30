package com.receiptofi.web.util;

import com.receiptofi.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;

import org.joda.time.DateTime;

/**
 * Helps in profiling the duration it takes to complete a task
 *
 * User: hitender
 * Date: 4/7/13
 * Time: 11:39 AM
 */
public final class PerformanceProfiling {
    private static final Logger log = LoggerFactory.getLogger(PerformanceProfiling.class);
    private static final int QUARTER_SECOND = 250;
    private static final int HALF_SECOND = 500;
    private static boolean TIME_UNIT_MS = true;

    /**
     * Logs the start of the process
     *
     * @param type
     * @param message
     * @param <T>
     * @return
     */
    public static <T> Date log(Class<T> type, String... message) {
        Date time = DateUtil.nowTime();
        if(log.isDebugEnabled()) {
            log.debug(type.getName() + "  " + Arrays.asList(message).toString() + " " + time);
        }
        return time;
    }

    /**
     * Logs the completion of the process
     *
     * @param type
     * @param time
     * @param message
     * @param <T>
     */
    public static <T> void log(Class<T> type, DateTime time, String... message) {
        if(System.currentTimeMillis() - time.getMillis() > QUARTER_SECOND) {
            if(log.isWarnEnabled()) {
                log.warn("{}  {}, {}, duration={} milliseconds", type.getName(), Arrays.asList(message).toString(), time, computeDuration(time));
            }
        } else {
            if(log.isDebugEnabled()) {
                log.debug("{}  {}, {}, duration={} milliseconds", type.getName(), Arrays.asList(message).toString(), time, computeDuration(time));
            } else if(log.isInfoEnabled()) {
                log.info("{}  {}, {}, duration={} seconds", type.getName(), Arrays.asList(message).toString(), time, computeDuration(time));
            }
        }
    }

    private static String computeDuration(DateTime time) {
        if(TIME_UNIT_MS) {
            return String.valueOf(System.currentTimeMillis() - time.getMillis());
        } else {
            return String.valueOf(DateUtil.duration(time).getSeconds());
        }
    }

    /**
     * Shows if the log is for a success of the method execution or for a failure
     *
     * @param type
     * @param time
     * @param condition - boolean
     * @param <T>
     */
    public static <T> void log(Class<T> type, DateTime time, String methodName, boolean condition) {
        String message = condition ? "Success" : "Failure";
        log(type, time, methodName, message);
    }
}
