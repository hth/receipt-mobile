package com.receiptofi.mobile.repository;

import com.receiptofi.domain.PaymentCardEntity;

import java.util.Date;
import java.util.List;

/**
 * User: hitender
 * Date: 9/23/16 5:24 AM
 */
public interface PaymentCardManagerMobile extends RepositoryManager<PaymentCardEntity> {

    PaymentCardEntity findOne(String id, String rid);
    List<PaymentCardEntity> getAllPaymentCards(String rid);
    List<PaymentCardEntity> getUpdatedSince(String rid, Date since);
}
