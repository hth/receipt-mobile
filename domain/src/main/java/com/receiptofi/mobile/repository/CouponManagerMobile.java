package com.receiptofi.mobile.repository;

import com.receiptofi.domain.CouponEntity;

import java.util.Date;
import java.util.List;

/**
 * User: hitender
 * Date: 5/9/16 10:37 AM
 */
public interface CouponManagerMobile extends RepositoryManager<CouponEntity> {
    List<CouponEntity> findAll(String rid);

    List<CouponEntity> getCouponUpdateSince(String rid, Date since);

    CouponEntity findOne(String couponId, String rid);

    CouponEntity findSharedCoupon(String rid, String originId);
}
