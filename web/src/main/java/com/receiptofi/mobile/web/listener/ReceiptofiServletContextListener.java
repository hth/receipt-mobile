package com.receiptofi.mobile.web.listener;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * User: hitender
 * Date: 8/13/15 8:19 AM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
public class ReceiptofiServletContextListener implements ServletContextListener {
    private static final Logger LOG = LoggerFactory.getLogger(ReceiptofiServletContextListener.class);

    private Properties messages = new Properties();

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        LOG.info("Receiptofi mobile context destroyed");
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        LOG.info("Receiptofi context initialized");

        try {
            messages.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("messages.properties"));
        } catch (IOException e) {
            LOG.error("could not load config properties file reason={}", e.getLocalizedMessage(), e);
        }

        checkEnvironment();
    }

    private void checkEnvironment() {
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            String buildEnvironment = (String) messages.get("build.env");

            LOG.info("Deploying on environment={} and host={}", buildEnvironment, hostName);
            if (StringUtils.equals(buildEnvironment, "prod") && (hostName.equals("t1") || hostName.equals("t2") || hostName.equals("t3") || hostName.equals("t4"))) {
                LOG.error("Mismatch environment. Found env={} on host={}", buildEnvironment, hostName);
                throw new RuntimeException("Mismatch environment. Found env=" + buildEnvironment + " on host=" + hostName);
            } else if (StringUtils.equals(buildEnvironment, "test") && !hostName.equals("receiptofi.com")) {
                LOG.error("Mismatch environment. Found env={} on host={}", buildEnvironment, hostName);
                throw new RuntimeException("Mismatch environment. Found env=" + buildEnvironment + " on host=" + hostName);
            }
        } catch (UnknownHostException e) {
            LOG.error("Could not get hostname reason={}", e.getLocalizedMessage(), e);
        }
    }
}
