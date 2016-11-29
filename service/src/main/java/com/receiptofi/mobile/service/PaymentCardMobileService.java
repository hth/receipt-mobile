package com.receiptofi.mobile.service;

import com.receiptofi.domain.PaymentCardEntity;
import com.receiptofi.domain.types.CardNetworkEnum;
import com.receiptofi.mobile.domain.AvailableAccountUpdates;
import com.receiptofi.mobile.repository.PaymentCardManagerMobile;
import com.receiptofi.repository.PaymentCardManager;
import com.receiptofi.utils.Validate;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * User: hitender
 * Date: 9/23/16 5:11 AM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@Service
public class PaymentCardMobileService {
    private static final Logger LOG = LoggerFactory.getLogger(PaymentCardMobileService.class);

    private PaymentCardManager paymentCardManager;
    private PaymentCardManagerMobile paymentCardManagerMobile;

    @Autowired
    public PaymentCardMobileService(PaymentCardManager paymentCardManager, PaymentCardManagerMobile paymentCardManagerMobile) {
        this.paymentCardManager = paymentCardManager;
        this.paymentCardManagerMobile = paymentCardManagerMobile;
    }

    /**
     * Do not use this open end query.
     *
     * @param rid
     * @return
     */
    void getAll(String rid, AvailableAccountUpdates availableAccountUpdates) {
        populateAvailableAccountUpdate(availableAccountUpdates, paymentCardManager.getPaymentCards(rid));
    }

    private List<PaymentCardEntity> getUpdatedSince(String rid, Date since) {
        return paymentCardManagerMobile.getUpdatedSince(rid, since);
    }

    void getPaymentCardUpdatedSince(String rid, Date since, AvailableAccountUpdates availableAccountUpdates) {
        populateAvailableAccountUpdate(availableAccountUpdates, getUpdatedSince(rid, since));
    }

    private void populateAvailableAccountUpdate(AvailableAccountUpdates availableAccountUpdates, List<PaymentCardEntity> paymentCards) {
        availableAccountUpdates.addJsonPaymentCards(paymentCards);
    }

    public PaymentCardEntity populateCard(String id, String cardName, CardNetworkEnum cardNetwork, String cardDigit, boolean active, String rid) {
        PaymentCardEntity paymentCard = null;

        if (StringUtils.isNotBlank(id) && Validate.isValidObjectId(id)) {
            paymentCard = paymentCardManager.findOne(id, rid);
        }

        if (null == paymentCard) {
            paymentCard = PaymentCardEntity.newInstance(rid, cardName, cardNetwork, cardDigit);
        } else {
            paymentCard.setCardName(cardName);
            paymentCard.setCardNetwork(cardNetwork);
            paymentCard.setCardDigit(cardDigit);
        }

        if (active) {
            paymentCard.active();
        } else {
            paymentCard.inActive();
        }

        return paymentCard;
    }

    public void save(PaymentCardEntity paymentCard) {
        paymentCardManager.save(paymentCard);
    }
}
