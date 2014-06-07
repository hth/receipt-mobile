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
 * Date: 4/13/13
 * Time: 10:35 PM
 * http://glassfish.java.net/nonav/javaee5/api/javax/xml/bind/annotation/XmlElements.html
 */
//@XmlRootElement(namespace="http://receiptofi.com/schema/receipt/v1", name="landingView")
@XmlRootElement(name="landingView")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlType(name="home", propOrder = { "userId", "emailId", "pendingCount", "receipts" })
public final class LandingView extends Base {

    @XmlElement(name = "userId", type = String.class, required = true)
    private String userId;

    @XmlElement(name = "emailId", type = String.class, required = true)
    private String emailId;

    @XmlElement(name = "pendingCount", type = Long.class, required = false)
    private long pendingCount;

    @XmlElementWrapper(name = "receipts")
    @XmlElement(name = "receipt")
    protected List<ReceiptEntity> receipts;

    public LandingView() { }

    private LandingView(String userId, String emailId, Header header) {
        super.setHeader(header);
        this.userId = userId;
        this.emailId = emailId;
    }

    public static LandingView newInstance(String userId, String emailId, Header header) {
        return new LandingView(userId, emailId, header);
    }

    /** Required for JSON */
    @SuppressWarnings("unused")
    public String getUserId() {
        return userId;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setPendingCount(long pendingCount) {
        this.pendingCount = pendingCount;
    }

    /** Required for JSON */
    @SuppressWarnings("unused")
    public long getPendingCount() {
        return pendingCount;
    }

    public void setReceipts(List<ReceiptEntity> receipts) {
        this.receipts = receipts;
    }

    /** Required for JSON */
    @SuppressWarnings("unused")
    public List<ReceiptEntity> getReceipts() {
        return receipts;
    }
}
