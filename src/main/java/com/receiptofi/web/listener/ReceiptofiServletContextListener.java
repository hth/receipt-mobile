package com.receiptofi.web.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Properties;

/**
 * User: hitender
 * Date: 9/21/13 8:15 PM
 */
public class ReceiptofiServletContextListener implements ServletContextListener {
    private static final Logger log = LoggerFactory.getLogger(ReceiptofiServletContextListener.class);

    private Properties config = new Properties();

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        //TODO make clean shutdown for quartz. This prevent now from tomcat shutdown
        log.info("Receiptofi context destroyed");
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        log.info("Receiptofi context initialized");

        try {
            config.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("conf/config.properties"));
        } catch (IOException e) {
            log.error("could not load config properties file reason={}",  e.getLocalizedMessage(), e);
        }

        try {
            if(hasAccessToFileSystem()) {
                log.info("Found and has access to directory={}", config.get("expensofiReportLocation"));
            }
        } catch (IOException e) {
            log.error("Failure in creating new files reason={}", e.getLocalizedMessage(), e);
        }
    }

    private boolean hasAccessToFileSystem() throws IOException {
        File directory = new File((String) config.get("expensofiReportLocation"));
        if(directory.exists() && directory.isDirectory()) {
            File file = new File(config.get("expensofiReportLocation") + File.separator + "receiptofi-expensofi.temp.delete.me");
            if(!file.createNewFile() && !file.canWrite() && !file.canRead()) {
                throw new AccessDeniedException("Cannot create, read or write to location: " + config.get("expensofiReportLocation"));
            }
            if(!file.delete()) {
                throw new AccessDeniedException("Could not delete file from location: " + config.get("expensofiReportLocation"));
            }
        } else {
            throw new AccessDeniedException("File system directory does not exists: " + config.get("expensofiReportLocation"));
        }
        return true;
    }
}
