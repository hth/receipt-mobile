package com.receiptofi.web.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Properties;

import org.springframework.util.Assert;

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

        if(hasAccessToFileSystem()) {
            log.info("Found and has access, to directory={}", config.get("expensofiReportLocation"));
        }
    }

    private boolean hasAccessToFileSystem() {
        Assert.notNull(config.get("expensofiReportLocation"));
        File directory = new File((String) config.get("expensofiReportLocation"));
        if(directory.exists() && directory.isDirectory()) {
            File file = new File(config.get("expensofiReportLocation") + File.separator + "receiptofi-expensofi.temp.delete.me");
            try {
                if(!file.createNewFile()) {
                    throw new AccessDeniedException("Cannot create, to location=" + config.get("expensofiReportLocation"));
                }
                if(!file.canWrite()) {
                    throw new AccessDeniedException("Cannot write, to location=" + config.get("expensofiReportLocation"));
                }
                if(!file.canRead()) {
                    throw new AccessDeniedException("Cannot read, to location=" + config.get("expensofiReportLocation"));
                }
                if(!file.delete()) {
                    throw new AccessDeniedException("Cannot delete, from location=" + config.get("expensofiReportLocation"));
                }
            } catch(IOException e) {
                log.error(
                        "Possible permission deny to location={}, reason={}",
                        config.get("expensofiReportLocation"),
                        e.getLocalizedMessage(),
                        e
                );
                stopServer();
            }
        } else {
            log.error("File system directory does not exists, location={}", config.get("expensofiReportLocation"));
            stopServer();
        }
        return true;
    }

    private void stopServer() {
        log.error("Stopping server now");
        System.exit(0);
    }
}
