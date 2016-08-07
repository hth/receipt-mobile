package com.receiptofi.mobile.service;

import static com.receiptofi.domain.json.JsonReceipt.ISO8601_FMT;

import com.receiptofi.domain.CouponEntity;
import com.receiptofi.domain.FileSystemEntity;
import com.receiptofi.domain.json.JsonCoupon;
import com.receiptofi.domain.shared.UploadDocumentImage;
import com.receiptofi.domain.types.CouponTypeEnum;
import com.receiptofi.domain.types.CouponUploadStatusEnum;
import com.receiptofi.domain.types.FileTypeEnum;
import com.receiptofi.mobile.domain.AvailableAccountUpdates;
import com.receiptofi.mobile.repository.CouponManagerMobile;
import com.receiptofi.mobile.util.Util;
import com.receiptofi.repository.CouponManager;
import com.receiptofi.service.CampaignService;
import com.receiptofi.service.FileSystemService;
import com.receiptofi.service.ImageSplitService;
import com.receiptofi.utils.ParseJsonStringToMap;
import com.receiptofi.utils.ScrubbedInput;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
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
    private CouponManagerMobile couponManagerMobile;
    private ImageSplitService imageSplitService;
    private CampaignService campaignService;
    private FileSystemService fileSystemService;
    private FriendMobileService friendMobileService;

    @Value("${couponExpireAfterMonths:3}")
    private int couponExpireAfterMonths;

    @Autowired
    public CouponMobileService(
            CouponManager couponManager,
            CouponManagerMobile couponManagerMobile,
            ImageSplitService imageSplitService,
            CampaignService campaignService,
            FileSystemService fileSystemService,
            FriendMobileService friendMobileService) {
        this.couponManager = couponManager;
        this.couponManagerMobile = couponManagerMobile;
        this.imageSplitService = imageSplitService;
        this.campaignService = campaignService;
        this.fileSystemService = fileSystemService;
        this.friendMobileService = friendMobileService;
    }

    public void save(CouponEntity coupon) {
        couponManager.save(coupon);

        if (!coupon.isActive()) {
            switch (coupon.getCouponType()) {
                case B:
                    /** Ignore user delete as this is deleted when campaign is deleted. */
                    break;
                case I:
                    /** For individual, OriginId is blank for unshared coupons or the original owner of the coupon. */
                    if (StringUtils.isBlank(coupon.getOriginId()) && null != coupon.getFileSystemEntities()) {
                        LOG.info("FileSystem for coupon marked for soft delete id={} rid={}", coupon.getId(), coupon.getRid());
                        fileSystemService.deleteSoft(coupon.getFileSystemEntities(), FileTypeEnum.C);
                    }
                    break;
                default:
                    LOG.error("Reached unsupported condition={}", coupon.getCouponType());
                    throw new UnsupportedOperationException("Reached unsupported condition " + coupon.getCouponType());
            }
        }
    }

    public void shareCoupon(CouponEntity coupon) {
        List<String> rids = coupon.getSharedWithRids();
        for (String rid : rids) {
            /** When users are connected as a friend, then share coupon. */
            if (friendMobileService.isConnected(coupon.getRid(), rid)) {
                CouponEntity sharedCoupon = couponManagerMobile.findSharedCoupon(
                        rid,
                        StringUtils.isBlank(coupon.getOriginId()) ? coupon.getId() : coupon.getOriginId());

                if (null == sharedCoupon) {
                    sharedCoupon = new CouponEntity()
                            .setRid(rid)
                            .setBusinessName(coupon.getBusinessName())
                            .setFreeText(coupon.getFreeText())
                            .setAvailable(coupon.getAvailable())
                            .setExpire(coupon.getExpire())
                            .setCouponType(coupon.getCouponType())
                            .setImagePath(coupon.getImagePath())
                            .setSharedWithRids(Arrays.asList(coupon.getRid()))
                            .setOriginId(coupon.getId())
                            .setInitiatedFromId(coupon.getInitiatedFromId())
                            .setFileSystemEntities(coupon.getFileSystemEntities())
                            .setCouponUploadStatus(CouponUploadStatusEnum.S);

                    try {
                        couponManager.save(sharedCoupon);
                    } catch (Exception e) {
                        /** Can happen when saving duplicate coupon with same RID and INITIATED_FROM. */
                        LOG.warn("Failed to save coupon rid={} initiatedFrom={} reason={}",
                                rid, coupon.getInitiatedFromId(), e.getLocalizedMessage(), e);
                    }
                } else {
                    LOG.info("Found shared coupon with rid={} originId={}", rid, coupon.getId());
                }
            } else {
                LOG.info("Cannot share coupon as friends not connected rid={} fid={} id={}", coupon.getRid(), rid, coupon.getId());
            }
        }
    }

    public CouponEntity findOne(String couponId) {
        return couponManager.findOne(couponId);
    }

    public CouponEntity findOne(String couponId, String rid) {
        return couponManagerMobile.findOne(couponId, rid);
    }

    void getAll(String rid, AvailableAccountUpdates availableAccountUpdates) {
        populateAvailableAccountUpdate(availableAccountUpdates, couponManagerMobile.findAll(rid));
    }

    void getCouponUpdateSince(String rid, Date since, AvailableAccountUpdates availableAccountUpdates) {
        populateAvailableAccountUpdate(availableAccountUpdates, couponManagerMobile.getCouponUpdateSince(rid, since));
    }

    public CouponEntity parseCoupon(String couponJson) throws IOException, ParseException {
        try {
            Map<String, ScrubbedInput> map = ParseJsonStringToMap.jsonStringToMap(couponJson);
            CouponEntity coupon = new CouponEntity();
            if (BooleanUtils.toBoolean(Integer.parseInt(map.get("a").toString()))) {
                coupon.active();
            } else {
                coupon.inActive();
            }

            switch (CouponTypeEnum.valueOf(map.get("ct").toString())) {
                case B:
                    coupon.setRid(map.get("rid").toString())
                            .setId(map.get("id").toString());

                    coupon.setUpdated();
                    break;
                case I:
                    if (StringUtils.isBlank(map.get("id").toString())) {
                        /** Do not set the rid when id is blank as its a new Coupon. */
                        coupon.setCreateAndUpdate(DateUtils.parseDate(map.get("c").toString(), ISO8601_FMT));
                    } else {
                        coupon.setRid(map.get("rid").toString())
                                .setId(map.get("id").toString());
                    }

                    Date expire = DateUtils.addMonths(new Date(), couponExpireAfterMonths);
                    if (StringUtils.isNotBlank(map.get("ex").toString())) {
                        Date localExpireDate = DateUtils.parseDate(map.get("ex").toString(), ISO8601_FMT);
                        expire = localExpireDate.after(expire) ? expire : localExpireDate;
                    }

                    coupon.setAvailable(DateUtils.parseDate(map.get("av").toString(), ISO8601_FMT))
                            .setExpire(expire)
                            .setCouponType(CouponTypeEnum.I)
                            .setLocalId(map.get("lid").toString());
                    break;
                default:
                    LOG.error("Reached unsupported condition={}", CouponTypeEnum.valueOf(map.get("ct").toString()));
                    throw new UnsupportedOperationException("Reached unsupported condition " + CouponTypeEnum.valueOf(map.get("ct").toString()));
            }


            coupon.setBusinessName(map.get("bn").toString())
                    .setFreeText(map.get("ft").toString())
                    .setReminder(BooleanUtils.toBoolean(Integer.parseInt(map.get("rm").toString())))
                    .setImagePath(map.get("ip").toString())
                    .setSharedWithRids(Util.convertCommaSeparatedStringToList(map.get("sh").toString()))
                    .setOriginId(map.get("oi").toString())
                    .setUsedCoupon(BooleanUtils.toBoolean(Integer.parseInt(map.get("uc").toString())));

            return coupon;
        } catch (ParseException e) {
            LOG.error("Exception {}", e.getLocalizedMessage(), e);
            throw e;
        }
    }

    private void populateAvailableAccountUpdate(AvailableAccountUpdates availableAccountUpdates, List<CouponEntity> coupons) {
        for (CouponEntity coupon : coupons) {
            JsonCoupon jsonCoupon = JsonCoupon.newInstance(coupon);
            availableAccountUpdates.addJsonCoupons(jsonCoupon);
        }
    }

    public void uploadCoupon(MultipartFile multipartFile, String rid, CouponEntity coupon) throws IOException {
        UploadDocumentImage image = UploadDocumentImage.newInstance(FileTypeEnum.C)
                .setFileData(multipartFile)
                .setRid(rid);

        BufferedImage bufferedImage = imageSplitService.bufferedImage(image.getFileData().getInputStream());
        Collection<FileSystemEntity> fileSystems = campaignService.deleteAndCreateNewImage(
                bufferedImage,
                image,
                coupon.getFileSystemEntities());

        coupon.setFileSystemEntities(fileSystems);
        save(coupon);
    }
}
