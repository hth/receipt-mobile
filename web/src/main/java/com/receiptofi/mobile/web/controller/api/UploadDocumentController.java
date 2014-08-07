package com.receiptofi.mobile.web.controller.api;

import com.receiptofi.domain.shared.UploadReceiptImage;
import com.receiptofi.domain.types.FileTypeEnum;
import com.receiptofi.mobile.domain.DocumentUpload;
import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.mobile.util.ErrorEncounteredJson;
import com.receiptofi.mobile.util.MobileSystemErrorCodeEnum;
import com.receiptofi.service.LandingService;
import com.receiptofi.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.servlet.ServletFileUpload;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.WebUtils;

import org.joda.time.DateTime;

/**
 * User: hitender
 * Date: 7/13/14 4:35 PM
 */
@Controller
@RequestMapping(value = "/api")
public class UploadDocumentController {
    private static final Logger log = LoggerFactory.getLogger(UploadDocumentController.class);

    @Autowired private AuthenticateService authenticateService;
    @Autowired private LandingService landingService;

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/upload",
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public @ResponseBody
    String upload(
            @RequestHeader("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            HttpServletRequest httpServletRequest,
            HttpServletResponse response
    ) throws IOException {
        DateTime time = DateUtil.now();
        log.debug("mail={}, auth={}", mail, "*********");
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if(rid != null) {
            log.info("uploading document");

            boolean isMultipart = ServletFileUpload.isMultipartContent(httpServletRequest);
            if(isMultipart) {
                MultipartHttpServletRequest multipartHttpRequest = WebUtils.getNativeRequest(httpServletRequest, MultipartHttpServletRequest.class);
                final List<MultipartFile> files = multipartHttpRequest.getFiles("qqfile");

                if(files.isEmpty()) {
                    return ErrorEncounteredJson.toJson("qqfile name missing in request or no file uploaded", MobileSystemErrorCodeEnum.DOCUMENT_UPLOAD);
                }

                for (MultipartFile multipartFile : files) {
                    UploadReceiptImage uploadReceiptImage = UploadReceiptImage.newInstance();
                    uploadReceiptImage.setFileData(multipartFile);
                    uploadReceiptImage.setUserProfileId(rid);
                    uploadReceiptImage.setFileType(FileTypeEnum.RECEIPT);
                    try {
                        landingService.uploadReceipt(rid, uploadReceiptImage);
                        return DocumentUpload.newInstance(multipartFile.getOriginalFilename(), landingService.pendingReceipt(rid)).asJson();
                    } catch (Exception exce) {
                        log.error("document upload failed reason={} rid={}", exce.getLocalizedMessage(), rid, exce);

                        Map<String, String> errors = new HashMap<>();
                        errors.put("reason", "failed document upload");
                        errors.put("document", multipartFile.getOriginalFilename());
                        errors.put("systemError", MobileSystemErrorCodeEnum.DOCUMENT_UPLOAD.name());
                        errors.put("systemErrorCode", MobileSystemErrorCodeEnum.DOCUMENT_UPLOAD.getCode());

                        return ErrorEncounteredJson.toJson(errors);
                    }
                }

            }
            return ErrorEncounteredJson.toJson("multipart failure for document upload", MobileSystemErrorCodeEnum.DOCUMENT_UPLOAD);
        }
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        return null;
    }
}