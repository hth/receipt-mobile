package com.receiptofi.web.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * User: hitender
 * Date: 4/14/13
 * Time: 6:51 PM
 * http://technotes.tostaky.biz/2012/08/jaxb-annotation-summary-crash-course.html
 * http://blog.bdoughan.com/2010/11/jaxb-and-inheritance-using-substitution.html
 * http://docs.oracle.com/javaee/5/tutorial/doc/bnbcv.html
 * http://docs.oracle.com/cd/E13222_01/wls/docs103/webserv/data_types.html
 */
@XmlRootElement(namespace="http://receiptofi.com/schema/receipt/v1", name="receipt_root")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "header" })
@XmlSeeAlso({Header.class})
public class Base implements Serializable {

    protected Header header;

    public Base() {}

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public void setStatus(Header.RESULT status) {
        header.setStatus(status);
    }

    public void setMessage(String message) {
        header.setMessage(message);
    }
}
