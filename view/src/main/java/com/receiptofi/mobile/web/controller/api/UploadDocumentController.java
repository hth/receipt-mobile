package com.receiptofi.mobile.web.controller.api;

import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.DOCUMENT_UPLOAD;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.MOBILE_UPGRADE;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.USER_INPUT;

import com.receiptofi.domain.DocumentEntity;
import com.receiptofi.domain.shared.UploadDocumentImage;
import com.receiptofi.domain.types.DeviceTypeEnum;
import com.receiptofi.domain.types.DocumentOfTypeEnum;
import com.receiptofi.domain.types.DocumentRejectReasonEnum;
import com.receiptofi.domain.types.FileTypeEnum;
import com.receiptofi.mobile.domain.DocumentUpload;
import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.mobile.types.LowestSupportedAppEnum;
import com.receiptofi.mobile.util.ErrorEncounteredJson;
import com.receiptofi.service.DocumentUpdateService;
import com.receiptofi.service.FileSystemService;
import com.receiptofi.service.LandingService;
import com.receiptofi.service.MessageDocumentService;
import com.receiptofi.utils.ScrubbedInput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
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
    private FileSystemService fileSystemService;
    private DocumentUpdateService documentUpdateService;
    private MessageDocumentService messageDocumentService;

    @Value ("${duplicate.document.reject.user}")
    private String documentRejectUserId;

    @Value ("${duplicate.document.reject.rid}")
    private String documentRejectRid;

    @Autowired
    public UploadDocumentController(
            LandingService landingService,
            AuthenticateService authenticateService,
            FileSystemService fileSystemService,
            DocumentUpdateService documentUpdateService,
            MessageDocumentService messageDocumentService) {
        this.landingService = landingService;
        this.authenticateService = authenticateService;
        this.fileSystemService = fileSystemService;
        this.documentUpdateService = documentUpdateService;
        this.messageDocumentService = messageDocumentService;
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
            ScrubbedInput mail,

            @RequestHeader ("X-R-AUTH")
            ScrubbedInput auth,

            @RequestHeader (value = "X-R-DT", required = false, defaultValue = "I")
            ScrubbedInput deviceType,

            @RequestHeader (value = "X-R-VR", required = false, defaultValue = "150")
            ScrubbedInput version,

            @RequestPart ("qqfile")
            MultipartFile file,

            HttpServletResponse response
    ) throws IOException {
        LOG.debug("mail={}, auth={}", mail, UtilityController.AUTH_KEY_HIDDEN);
        String rid = authenticateService.getReceiptUserId(mail.getText(), auth.getText());
        if (rid == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
            return null;
        }

        DeviceTypeEnum deviceTypeEnum;
        try {
            deviceTypeEnum = DeviceTypeEnum.valueOf(deviceType.getText());
            if (deviceTypeEnum == DeviceTypeEnum.I) {
                LOG.info("Check if API version is supported for {} version={} rid={}", deviceTypeEnum.getDescription(), version.getText(), rid);
                try {
                    int versionNumber = Integer.valueOf(version.getText());
                    if (LowestSupportedAppEnum.isLessThanLowestSupportedVersion(deviceTypeEnum, versionNumber)) {
                        LOG.warn("Sent warning to upgrade rid={} versionNumber={}", rid, versionNumber);
                        return DeviceController.getErrorReason("To continue, please upgrade to latest version", MOBILE_UPGRADE);
                    }
                } catch (NumberFormatException e) {
                    LOG.error("Failed parsing API version, reason={}", e.getLocalizedMessage(), e);
                    return DeviceController.getErrorReason("Failed to read API version type.", USER_INPUT);
                } catch (Exception e) {
                    LOG.error("Failed parsing API version, reason={}", e.getLocalizedMessage(), e);
                    return DeviceController.getErrorReason("Incorrect API version type.", USER_INPUT);
                }
            }
        } catch (Exception e) {
            LOG.error("Failed parsing deviceType, reason={}", e.getLocalizedMessage(), e);
            return DeviceController.getErrorReason("Incorrect device type.", USER_INPUT);
        }

        LOG.info("upload document begins rid={}", rid);

        if (file.isEmpty()) {
            LOG.error("qqfile name missing in request or no file uploaded");
            return ErrorEncounteredJson.toJson("File qqfile missing in request or no file uploaded.", DOCUMENT_UPLOAD);
        }

        try {
            UploadDocumentImage uploadDocumentImage = UploadDocumentImage.newInstance(FileTypeEnum.R)
                    .setFileData(file)
                    .setRid(rid);

            /* Find duplicate if the similar file exists in the queue. */
            boolean duplicateFile = fileSystemService.fileWithSimilarNameDoesNotExists(rid, uploadDocumentImage.getRealFileName());
            DocumentEntity document = landingService.uploadDocument(uploadDocumentImage);

            /* Even if we know its a duplicate we need to add the document to list in rejection count. */
            if (!duplicateFile) {
                messageDocumentService.lockMessageWhenDuplicate(
                            document.getId(),
                            documentRejectUserId,
                            documentRejectRid);

                documentUpdateService.processDocumentForReject(
                        documentRejectRid,
                        document.getId(),
                        DocumentOfTypeEnum.RECEIPT,
                        DocumentRejectReasonEnum.D);
            }

            DocumentUpload documentUpload = DocumentUpload.newInstance(
                    uploadDocumentImage.getRealFileName(),
                    uploadDocumentImage.getBlobId(),
                    landingService.pendingReceipt(rid)
            );

            LOG.info("upload document successfully complete for rid={}", rid);
            return documentUpload.asJson();
        } catch (Exception exce) {
            LOG.error("upload document failed reason={} rid={}", exce.getLocalizedMessage(), rid, exce);

            Map<String, String> errors = new HashMap<>();
            errors.put(ErrorEncounteredJson.REASON, "Failed to upload file.");
            errors.put("document", file.getOriginalFilename());
            errors.put(ErrorEncounteredJson.SYSTEM_ERROR, DOCUMENT_UPLOAD.name());
            errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, DOCUMENT_UPLOAD.getCode());

            return ErrorEncounteredJson.toJson(errors);
        }
    }
}
