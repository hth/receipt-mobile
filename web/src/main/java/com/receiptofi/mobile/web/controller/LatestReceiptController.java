package com.receiptofi.mobile.web.controller;

import com.receiptofi.domain.json.JsonReceipt;
import com.receiptofi.mobile.service.ReceiptMobileService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

/**
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

    @RequestMapping (
            value = "/receipt",
            method = RequestMethod.GET,
            headers = "Accept=" + MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public JsonReceipt getLatestReceipt() {
        return receiptMobileService.getRecentReceipts();
    }
}
