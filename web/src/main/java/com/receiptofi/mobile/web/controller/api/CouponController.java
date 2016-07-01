package com.receiptofi.mobile.web.controller.api;

import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.DOCUMENT_UPLOAD;

import com.receiptofi.domain.CouponEntity;
import com.receiptofi.domain.json.JsonCoupon;
import com.receiptofi.mobile.domain.AvailableAccountUpdates;
import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.mobile.service.CouponMobileService;
import com.receiptofi.mobile.util.ErrorEncounteredJson;
import com.receiptofi.utils.ScrubbedInput;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

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

    private AuthenticateService authenticateService;
    private CouponMobileService couponMobileService;

    @Autowired
    public CouponController(
            AuthenticateService authenticateService,
            CouponMobileService couponMobileService) {
        this.authenticateService = authenticateService;
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
        LOG.debug("mail={}, auth={}", mail, UtilityController.AUTH_KEY_HIDDEN);
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (rid == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
            return null;
        } else {
            try {
                CouponEntity coupon = couponMobileService.parseCoupon(requestBodyJson);
                if (StringUtils.isNotBlank(coupon.getId())) {
                    CouponEntity couponEntity = couponMobileService.findOne(coupon.getId());
                    if (null != couponEntity) {
                        coupon.setFileSystemEntities(couponEntity.getFileSystemEntities())
                                .setVersion(couponEntity.getVersion());
                    } else {
                        LOG.error("Tried modifying coupon that did not exists rid={} id={}", coupon.getRid(), coupon.getId());
                        response.sendError(HttpServletResponse.SC_NOT_FOUND, "NotFound");
                        return null;
                    }
                } else {
                    coupon.setRid(rid);
                }
                couponMobileService.save(coupon);
                couponMobileService.shareCoupon(coupon);
                AvailableAccountUpdates availableAccountUpdates = AvailableAccountUpdates.newInstance();
                availableAccountUpdates.addJsonCoupons(JsonCoupon.newInstance(coupon));
                return availableAccountUpdates.asJson();
            } catch (Exception e) {
                LOG.error("Failure during coupon save rid={} reason={}", rid, e.getLocalizedMessage(), e);
                Map<String, String> errors = ExpenseTagController.getErrorSevere("Something went wrong. Engineers are looking into this.");
                return ErrorEncounteredJson.toJson(errors);
            }
        }
    }

    /**
     * TODO(hth) look into @RequestPart("meta-data") MetaData metadata, @RequestPart("file-data") MultipartFile file
     * http://docs.spring.io/spring/docs/current/spring-framework-reference/html/mvc.html
     *
     * @param mail
     * @param auth
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping (
            method = RequestMethod.POST,
            value = "/upload/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public String upload(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            @PathVariable ("id")
            ScrubbedInput id,

            @RequestPart ("qqfile")
            MultipartFile multipartFile,

            HttpServletResponse response
    ) throws IOException {
        LOG.info("mail={}, auth={}", mail, UtilityController.AUTH_KEY_HIDDEN);
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (rid == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
            return null;
        } else {
            try {
                LOG.info("Starting coupon upload couponId={}", id);
                CouponEntity coupon = couponMobileService.findOne(id.getText(), rid);
                if (null == coupon) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "NotFound");
                    return null;
                }

                if (multipartFile.isEmpty()) {
                    LOG.error("qqfile name missing in request or no file uploaded");
                    return ErrorEncounteredJson.toJson("File qqfile missing in request or no file uploaded.", DOCUMENT_UPLOAD);
                }

                couponMobileService.uploadCoupon(multipartFile, rid, coupon);

                AvailableAccountUpdates availableAccountUpdates = AvailableAccountUpdates.newInstance();
                availableAccountUpdates.addJsonCoupons(JsonCoupon.newInstance(coupon));
                return availableAccountUpdates.asJson();
            } catch (Exception e) {
                LOG.error("Failure during coupon save rid={} reason={}", rid, e.getLocalizedMessage(), e);
                Map<String, String> errors = ExpenseTagController.getErrorSevere("Something went wrong. Engineers are looking into this.");
                return ErrorEncounteredJson.toJson(errors);
            }
        }
    }
}
