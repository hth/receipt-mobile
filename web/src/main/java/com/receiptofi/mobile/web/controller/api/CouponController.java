package com.receiptofi.mobile.web.controller.api;

import com.receiptofi.domain.CouponEntity;
import com.receiptofi.domain.json.JsonCoupon;
import com.receiptofi.mobile.domain.AvailableAccountUpdates;
import com.receiptofi.mobile.service.CouponMobileService;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.ParseException;

import javax.servlet.http.HttpServletResponse;

/**
 * User: hitender
 * Date: 4/27/16 9:31 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@RestController
@RequestMapping (
        value = "/api/coupon",
        produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class CouponController {
    private static final Logger LOG = LoggerFactory.getLogger(CouponController.class);

    private CouponMobileService couponMobileService;

    @Autowired
    public CouponController(CouponMobileService couponMobileService) {
        this.couponMobileService = couponMobileService;
    }

    /**
     * Create, Update, Delete coupons.
     *
     * @param mail
     * @param auth
     * @param requestBodyJson
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping (
            value = "/update.json",
            method = RequestMethod.POST,
            headers = "Accept=" + MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public String updateExpenseTag(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            @RequestBody
            String requestBodyJson,

            HttpServletResponse response
    ) throws IOException, ParseException {
        CouponEntity coupon = couponMobileService.parseCoupon(requestBodyJson);
        if (StringUtils.isNotBlank(coupon.getId())) {
            CouponEntity couponEntity = couponMobileService.findOne(coupon.getId());
            if (null != couponEntity) {
                coupon.setVersion(couponEntity.getVersion());
            }
        }
        couponMobileService.save(coupon);
        AvailableAccountUpdates availableAccountUpdates = AvailableAccountUpdates.newInstance();
        availableAccountUpdates.addJsonCoupons(JsonCoupon.newInstance(coupon));
        return availableAccountUpdates.asJson();
    }
}