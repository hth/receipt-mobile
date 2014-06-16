package com.receiptofi.service.mobile;

import com.receiptofi.service.LandingService;
import com.receiptofi.utils.CreateTempFile;
import com.receiptofi.web.rest.LandingView;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
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
 * Date: 9/21/13 1:17 PM
 */
@Deprecated //TODO Find if this has to be removed. Since no more mobile is supported in web
@Service
public final class LandingViewService {
    private static final Logger log = LoggerFactory.getLogger(LandingViewService.class);

    @Autowired
    LandingService landingService;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired private FreeMarkerConfigurationFactoryBean freemarkerConfiguration;

    @Value("${http}")
    private String http;

    @Value("${https}")
    private String https;

    @Value("${mobile-host}")
    private String host;

    @Value("${mobile-port}")
    private String port;

    @Value("${secure.port}")
    private String securePort;

    @Value("${app.name}")
    private String appName;

    //XXX TODO Find if this has to be removed. Since no more mobile is supported in web
    public String landingViewHTMLString(LandingView landingView) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(LandingView.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            // output pretty printed
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);

            File file = CreateTempFile.file("XML-Landing", ".xml");
            jaxbMarshaller.marshal(landingView, file);
            jaxbMarshaller.marshal(landingView, System.out);

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
        } catch (JAXBException | SAXException | ParserConfigurationException | IOException | TemplateException e) {
            log.error("Error while processing reporting template: " + e.getLocalizedMessage());
        }
        return null;
    }


    private String freemarkerDo(Map rootMap) throws IOException, TemplateException {
        Configuration cfg = freemarkerConfiguration.createConfiguration();
        Template template = cfg.getTemplate("landingview-mobile.ftl");
        final String text = processTemplateIntoString(template, rootMap);
        log.debug(text);
        return text;
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
