package com.receiptofi.mobile.web.controller.api;

import com.receiptofi.domain.ItemEntity;
import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.mobile.domain.mapping.ReceiptItem;
import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.service.ItemService;
import com.receiptofi.service.ReceiptService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

/**
 * User: hitender
 * Date: 9/9/14 11:37 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@RestController
@RequestMapping (value = "/api")
public class ReceiptItemsController {
    private static final Logger LOG = LoggerFactory.getLogger(ReceiptItemsController.class);
    private AuthenticateService authenticateService;
    private ItemService itemService;
    private ReceiptService receiptService;

    @Autowired
    public ReceiptItemsController(
            AuthenticateService authenticateService,
            ItemService itemService,
            ReceiptService receiptService
    ) {
        this.authenticateService = authenticateService;
        this.itemService = itemService;
        this.receiptService = receiptService;
    }

    /**
     * Gets detailed receipt.
     *
     * @param mail
     * @param auth
     * @param receiptId
     * @param response
     * @return
     * @throws java.io.IOException
     */
    @RequestMapping (
            method = RequestMethod.GET,
            value = "/receiptDetail/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public List<ReceiptItem> getDetailedReceipt(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            @PathVariable (value = "id")
            String receiptId,

            HttpServletResponse response
    ) throws IOException {
        LOG.debug("mail={}, auth={}", mail, UtilityController.AUTH_KEY_HIDDEN);
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (null == rid) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
            return Collections.emptyList();
        }

        List<ReceiptItem> receiptItems = new LinkedList<>();
        try {
            ReceiptEntity receipt = receiptService.findReceipt(receiptId, rid);
            if (null != receipt && receipt.getId().equals(receiptId)) {
                List<ItemEntity> items = itemService.getAllItemsOfReceipt(receiptId);
                for (ItemEntity item : items) {
                    receiptItems.add(ReceiptItem.newInstance(item));
                }
                return receiptItems;
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "NotFound");
                return Collections.emptyList();
            }
        } catch (Exception e) {
            LOG.error("reason={}", e.getLocalizedMessage(), e);
        }
        return receiptItems;
    }
}
