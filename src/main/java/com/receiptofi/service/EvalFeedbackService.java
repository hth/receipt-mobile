package com.receiptofi.service;

import com.receiptofi.domain.EvalFeedbackEntity;
import com.receiptofi.domain.types.FileTypeEnum;
import com.receiptofi.repository.EvalFeedbackManager;
import com.receiptofi.web.form.UploadReceiptImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

/**
 * User: hitender
 * Date: 7/20/13
 * Time: 5:32 PM
 */
@Service
public final class EvalFeedbackService {
    private static final Logger log = LoggerFactory.getLogger(EvalFeedbackService.class);

    @Autowired EvalFeedbackManager evalFeedbackManager;
    @Autowired FileDBService fileDBService;

    public void addFeedback(String comment, int rating, CommonsMultipartFile fileData, String receiptUserId) {
        String blobId = StringUtils.EMPTY;
        try {
            if(fileData.getSize() > 0) {
                UploadReceiptImage uploadReceiptImage = UploadReceiptImage.newInstance();
                uploadReceiptImage.setFileData(fileData);
                uploadReceiptImage.setUserProfileId(receiptUserId);
                uploadReceiptImage.setFileType(FileTypeEnum.FEEDBACK);

                blobId = fileDBService.saveFile(uploadReceiptImage);
            }

            EvalFeedbackEntity evalFeedbackEntity = EvalFeedbackEntity.newInstance(comment, rating, receiptUserId);
            if(!StringUtils.isEmpty(blobId)) {
                evalFeedbackEntity.setAttachmentBlobId(blobId);
            }
            evalFeedbackManager.save(evalFeedbackEntity);
        } catch (Exception exce) {
            log.error(exce.getLocalizedMessage());
        }
    }
}
