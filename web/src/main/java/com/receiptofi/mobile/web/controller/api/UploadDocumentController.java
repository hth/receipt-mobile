package com.receiptofi.mobile.web.controller.api;

import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.DOCUMENT_UPLOAD;

import com.receiptofi.domain.shared.UploadDocumentImage;
import com.receiptofi.domain.types.FileTypeEnum;
import com.receiptofi.mobile.domain.DocumentUpload;
import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.mobile.util.ErrorEncounteredJson;
import com.receiptofi.service.LandingService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * User: hitender
 * Date: 7/13/14 4:35 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@RestController
@RequestMapping (value = "/api")
public class UploadDocumentController {
    private static final Logger LOG = LoggerFactory.getLogger(UploadDocumentController.class);

    private AuthenticateService authenticateService;
    private LandingService landingService;

    @Autowired
    public UploadDocumentController(LandingService landingService, AuthenticateService authenticateService) {
        this.landingService = landingService;
        this.authenticateService = authenticateService;
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
            value = "/upload",
            produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8"
    )
    public String upload(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            @RequestParam ("qqfile")
            MultipartFile file,

            HttpServletResponse response
    ) throws IOException {
        LOG.debug("mail={}, auth={}", mail, UtilityController.AUTH_KEY_HIDDEN);
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (rid == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
            return null;
        }
        LOG.info("upload document begins rid={}", rid);

        if (file.isEmpty()) {
            LOG.error("qqfile name missing in request or no file uploaded");
            return ErrorEncounteredJson.toJson("File qqfile missing in request or no file uploaded.", DOCUMENT_UPLOAD);
        }

        try {
            UploadDocumentImage uploadDocumentImage = UploadDocumentImage.newInstance();
            uploadDocumentImage.setFileData(file);
            uploadDocumentImage.setRid(rid);
            uploadDocumentImage.setFileType(FileTypeEnum.RECEIPT);
            landingService.uploadDocument(uploadDocumentImage);

            return DocumentUpload.newInstance(
                    file.getOriginalFilename(),
                    uploadDocumentImage.getBlobId(),
                    landingService.pendingReceipt(rid)
            ).asJson();
        } catch (Exception exce) {
            LOG.error("upload document failed reason={} rid={}", exce.getLocalizedMessage(), rid, exce);

            Map<String, String> errors = new HashMap<>();
            errors.put(ErrorEncounteredJson.REASON, "Failed to upload file.");
            errors.put("document", file.getOriginalFilename());
            errors.put(ErrorEncounteredJson.SYSTEM_ERROR, DOCUMENT_UPLOAD.name());
            errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, DOCUMENT_UPLOAD.getCode());

            return ErrorEncounteredJson.toJson(errors);
        } finally {
            LOG.info("upload document successfully complete for rid={}", rid);
        }
    }
}
