package com.receiptofi.web.rest;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Header is part of all the REST messages
 *
 * User: hitender
 * Date: 4/14/13
 * Time: 6:53 PM
 */
@XmlRootElement(namespace="http://receiptofi.com/schema/receipt/v1", name="header")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public final class Header extends Base {
    public static enum RESULT {SUCCESS, FAILURE, INVALID_REQUEST, INVALID_DATA, AUTH_FAILURE}

    @XmlElement(name = "authId", type = String.class, required = true)
    protected String authId;

    @XmlElement(name = "status", required = true)
    protected RESULT status = RESULT.FAILURE;

    @XmlElement(name = "message", type = String.class, required = true, nillable = true)
    protected String message;

    public Header() {}

    private Header(String authId) {
        this.authId = authId;
    }

    /**
     * This is used in case there is a failure in Authorization
     * @return
     */
    public static Header newInstanceFailure() {
        return new Header();
    }

    /**
     * Auth code is required for any REST CRUD activity
     * @param authId
     * @return
     */
    public static Header newInstance(String authId) {
        return new Header(authId);
    }

    /** Required for JSON */
    @SuppressWarnings("unused")
    public String getAuthId() {
        return authId;
    }

    public void setStatus(RESULT status) {
        this.status = status;
    }

    /** Required for JSON */
    @SuppressWarnings("unused")
    public RESULT getStatus() {
        return status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /** Required for JSON */
    @SuppressWarnings("unused")
    public String getMessage() {
        return message;
    }
}
