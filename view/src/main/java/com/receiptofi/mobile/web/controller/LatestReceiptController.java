package com.receiptofi.mobile.web.controller;

import com.receiptofi.mobile.service.ReceiptMobileService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Shows recent receipt sanitized to main web home screen.
 *
 * User: hitender
 * Date: 2/11/16 7:43 PM
 */
@RestController
@RequestMapping (value = "/latest")
public class LatestReceiptController {
    private static final Logger LOG = LoggerFactory.getLogger(LatestReceiptController.class);

    private ReceiptMobileService receiptMobileService;

    @Autowired
    public LatestReceiptController(ReceiptMobileService receiptMobileService) {
        this.receiptMobileService = receiptMobileService;
    }

    @CrossOrigin (origins = {"https://receiptofi.com", "https://www.receiptofi.com"})
    @RequestMapping (
            value = "/receipt",
            method = RequestMethod.GET,
            headers = "Accept=" + MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public String getLatestReceipt() {
        //return receiptMobileService.getRecentReceipts();
        return "{}";
    }
}
