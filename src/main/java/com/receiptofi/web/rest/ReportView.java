package com.receiptofi.web.rest;

import com.receiptofi.domain.ReceiptEntity;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * User: hitender
 * Date: 9/6/13 9:27 PM
 */
@XmlRootElement(name="reportView")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType(name="home", propOrder = { "userId", "emailId", "receipts" })
public final class ReportView extends Base {

    @XmlElement(name = "userId", type = String.class, required = true)
    private String userId;

    @XmlElement(name = "emailId", type = String.class, required = true)
    private String emailId;

    @XmlElementWrapper(name = "receipts")
    @XmlElement(name = "receipt")
    protected List<ReceiptEntity> receipts;

    public ReportView() { }

    private ReportView(String userId, String emailId, Header header) {
        super.setHeader(header);
        this.userId = userId;
        this.emailId = emailId;
    }

    public static ReportView newInstance(String userId, String emailId, Header header) {
        return new ReportView(userId, emailId, header);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public List<ReceiptEntity> getReceipts() {
        return receipts;
    }

    public void setReceipts(List<ReceiptEntity> receipts) {
        this.receipts = receipts;
    }
}
