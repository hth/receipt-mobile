package com.receiptofi.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.ui.freemarker.FreeMarkerTemplateUtils.processTemplateIntoString;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;

/**
 * User: hitender
 * Date: 9/4/13 1:19 PM
 */
@Service
public final class ReportService {
    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    @Autowired LandingService landingService;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired private FreeMarkerConfigurationFactoryBean freemarkerConfiguration;

    @Value("${http}")
    private String http;

    @Value("${https}")
    private String https;

    @Value("${host}")
    private String host;

    @Value("${port}")
    private String port;

    @Value("${secure.port}")
    private String securePort;

    @Value("${app.name}")
    private String appName;

    public String monthlyReport(File file) {
        try {
            Map rootMap = new HashMap();
            rootMap.put("doc", freemarker.ext.dom.NodeModel.parse(file));

            rootMap.put("protocol", https);
            rootMap.put("host", host);
            if(rootMap.get("protocol").equals(https)) {
                rootMap.put("port", securePort);
            } else {
                rootMap.put("port", port);
            }
            rootMap.put("appname", appName);

            return freemarkerDo(rootMap);
        } catch (SAXException | ParserConfigurationException | IOException | TemplateException e) {
            log.error("Error while processing reporting template: " + e.getLocalizedMessage());
        }
        return null;
    }


    private String freemarkerDo(Map rootMap) throws IOException, TemplateException {
        Configuration cfg = freemarkerConfiguration.createConfiguration();
        Template template = cfg.getTemplate("monthly-report.ftl");
        return processTemplateIntoString(template, rootMap);
    }

    /**
     * Stream to console
     *
     * @param template
     * @param rootMap
     * @throws IOException
     * @throws TemplateException
     */
    private void processTemplateToSystemOut(Template template, Map rootMap) throws IOException, TemplateException {
        OutputStreamWriter output = new OutputStreamWriter(System.out);
        template.process(rootMap, output);
    }
}
