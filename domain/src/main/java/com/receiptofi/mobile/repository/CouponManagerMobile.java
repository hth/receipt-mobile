package com.receiptofi.mobile.repository;

import com.receiptofi.domain.CouponEntity;

/**
 * User: hitender
 * Date: 5/9/16 10:37 AM
 */
public interface CouponManagerMobile extends RepositoryManager<CouponEntity> {
    void save(CouponEntity coupon);
}
