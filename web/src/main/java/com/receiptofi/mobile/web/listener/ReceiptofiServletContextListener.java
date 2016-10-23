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

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        LOG.info("Receiptofi mobile context destroyed");
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        LOG.info("Receiptofi context initialized");

        Properties messages = new Properties();
        Properties environment = new Properties();

        try {
            messages.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("messages.properties"));

            if (StringUtils.equals(messages.getProperty("build.env"), "prod")) {
                environment.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("conf/prod.properties"));
            } else if (StringUtils.equals(messages.getProperty("build.env"), "sandbox")) {
                environment.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("conf/sandbox.properties"));
            } else {
                environment.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("conf/dev.properties"));
            }

        } catch (IOException e) {
            LOG.error("could not load config properties file reason={}", e.getLocalizedMessage(), e);
        }

        checkEnvironment(messages, environment);
    }

    private void checkEnvironment(Properties messages, Properties environment) {
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            String buildEnvironment = (String) messages.get("build.env");
            String hostname = environment.getProperty("hostname.starts.with");

            LOG.info("Deploying on environment={} and host={}", buildEnvironment, hostName);
            if (StringUtils.equals(buildEnvironment, "prod") && !hostName.startsWith(hostname)) {
                LOG.error("Mismatch environment. Found env={} on host={}", buildEnvironment, hostName);
                throw new RuntimeException("Mismatch environment. Found env=" + buildEnvironment + " on host=" + hostName);
            } else if (StringUtils.equals(buildEnvironment, "sandbox") && !hostName.startsWith(hostname)) {
                LOG.error("Mismatch environment. Found env={} on host={}", buildEnvironment, hostName);
                throw new RuntimeException("Mismatch environment. Found env=" + buildEnvironment + " on host=" + hostName);
            }
        } catch (UnknownHostException e) {
            LOG.error("Could not get hostname reason={}", e.getLocalizedMessage(), e);
        }
    }
}
