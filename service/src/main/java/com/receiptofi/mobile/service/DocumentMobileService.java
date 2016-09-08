package com.receiptofi.mobile.service;

import com.receiptofi.mobile.domain.AvailableAccountUpdates;
import com.receiptofi.repository.DocumentManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * User: hitender
 * Date: 4/7/15 11:55 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@Service
public class DocumentMobileService {

    private final DocumentManager documentManager;

    @Autowired
    public DocumentMobileService(DocumentManager documentManager) {
        this.documentManager = documentManager;
    }

    public void getUnprocessedDocuments(String rid, AvailableAccountUpdates availableAccountUpdates) {
        availableAccountUpdates.setUnprocessedDocuments(documentManager.numberOfPendingReceipts(rid));
    }
}
