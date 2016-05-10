package com.receiptofi.mobile.service;

import static com.receiptofi.domain.json.JsonReceipt.ISO8601_FMT;

import com.receiptofi.domain.CouponEntity;
import com.receiptofi.domain.types.CouponTypeEnum;
import com.receiptofi.mobile.util.Util;
import com.receiptofi.repository.CouponManager;
import com.receiptofi.utils.ParseJsonStringToMap;
import com.receiptofi.utils.ScrubbedInput;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

/**
 * User: hitender
 * Date: 5/9/16 9:32 AM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@Service
public class CouponMobileService {
    private static final Logger LOG = LoggerFactory.getLogger(CouponMobileService.class);

    private CouponManager couponManager;

    @Autowired
    public CouponMobileService(CouponManager couponManager) {
        this.couponManager = couponManager;
    }

    public void save(CouponEntity coupon) {
        couponManager.save(coupon);
    }

    public CouponEntity parseCoupon(String couponJson) throws IOException, ParseException {
        try {
            Map<String, ScrubbedInput> map = ParseJsonStringToMap.jsonStringToMap(couponJson);
            CouponEntity coupon = new CouponEntity();
            if (Boolean.valueOf(map.get("a").toString())) {
                coupon.active();
            } else {
                coupon.inActive();
            }

            switch (CouponTypeEnum.valueOf(map.get("ct").toString())) {
                case B:
                    coupon.setId(map.get("id").toString());
                    coupon.setUpdated();
                    break;
                case I:
                    if (StringUtils.isBlank(map.get("id").toString())) {
                        coupon.setCreateAndUpdate(DateUtils.parseDate(map.get("c").toString(), ISO8601_FMT));
                    } else {
                        coupon.setId(map.get("id").toString());
                    }

                    coupon.setAvailable(DateUtils.parseDate(map.get("av").toString(), ISO8601_FMT))
                            .setExpire(DateUtils.parseDate(map.get("ex").toString(), ISO8601_FMT))
                            .setCouponType(CouponTypeEnum.I)
                            .setLocalId(map.get("lid").toString());
                    break;
                default:
            }


            coupon.setRid(map.get("rid").toString())
                    .setBusinessName(map.get("bn").toString())
                    .setFreeText(map.get("ft").toString())
                    .setReminder(Boolean.valueOf(map.get("rm").toString()))
                    .setImagePath(map.get("ip").toString())
                    .setSharedWithRids(Util.convertCommaSeparatedStringToList(map.get("sh").toString()))
                    .setOriginId(map.get("oi").toString())
                    .setUsedCoupon(Boolean.valueOf(map.get("uc").toString()));

            return coupon;
        } catch (ParseException e) {
            LOG.error("Exception {}", e.getLocalizedMessage(), e);
            throw e;
        }
    }
}
