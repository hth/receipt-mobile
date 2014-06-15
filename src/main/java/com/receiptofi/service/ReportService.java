package com.receiptofi.service;

import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.service.mobile.LandingViewService;
import com.receiptofi.utils.CreateTempFile;
import com.receiptofi.web.rest.Header;
import com.receiptofi.web.rest.ReportView;
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
import java.util.List;
import java.util.Map;

import static org.springframework.ui.freemarker.FreeMarkerTemplateUtils.processTemplateIntoString;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;

import org.joda.time.DateTime;

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
    @Autowired private LandingViewService landingViewService;

    public String monthlyReport(DateTime month, String profileId, String emailId, Header header) {
        List<ReceiptEntity> receipts = landingService.getAllReceiptsForThisMonth(profileId, month);

        ReportView reportView = ReportView.newInstance(profileId, emailId, header);
        reportView.setReceipts(receipts);
        reportView.setHeader(header);

        return monthlyReport(reportView);
    }

    private String monthlyReport(ReportView reportView) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ReportView.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            // output pretty printed
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);

            File file = CreateTempFile.file("XML-Report", ".xml");
            jaxbMarshaller.marshal(reportView, file);
            landingViewService.populateDataForFTL(file);
        } catch (JAXBException | SAXException | ParserConfigurationException | IOException | TemplateException e) {
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
