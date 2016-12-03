package com.receiptofi.mobile.web.controller.api;

import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.DOCUMENT_UPLOAD;

import com.receiptofi.domain.DocumentEntity;
import com.receiptofi.domain.shared.UploadDocumentImage;
import com.receiptofi.domain.types.DocumentOfTypeEnum;
import com.receiptofi.domain.types.DocumentRejectReasonEnum;
import com.receiptofi.domain.types.FileTypeEnum;
import com.receiptofi.mobile.domain.DocumentUpload;
import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.mobile.util.ErrorEncounteredJson;
import com.receiptofi.service.DocumentUpdateService;
import com.receiptofi.service.FileSystemService;
import com.receiptofi.service.LandingService;
import com.receiptofi.service.MessageDocumentService;

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
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            @RequestPart ("qqfile")
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
            UploadDocumentImage uploadDocumentImage = UploadDocumentImage.newInstance(FileTypeEnum.R)
                    .setFileData(file)
                    .setRid(rid);

            boolean duplicateFile = fileSystemService.fileWithSimilarNameDoesNotExists(rid, uploadDocumentImage.getOriginalFileName());
            DocumentEntity document = landingService.uploadDocument(uploadDocumentImage);

            /* Even if we know its a duplicate we need to add the document to list in rejection count. */
            if (!duplicateFile) {
                LOG.info("{} receipt found, delete, name={} rid={}",
                        DocumentRejectReasonEnum.D.getDescription(),
                        uploadDocumentImage.getOriginalFileName(),
                        rid);

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
                    file.getOriginalFilename(),
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
