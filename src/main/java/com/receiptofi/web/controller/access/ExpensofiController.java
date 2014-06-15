package com.receiptofi.web.controller.access;

import com.receiptofi.domain.FileSystemEntity;
import com.receiptofi.domain.ItemEntity;
import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.site.ReceiptUser;
import com.receiptofi.domain.types.NotificationTypeEnum;
import com.receiptofi.service.FileDBService;
import com.receiptofi.service.ItemAnalyticService;
import com.receiptofi.service.NotificationService;
import com.receiptofi.service.ReceiptService;
import com.receiptofi.utils.CreateTempFile;
import com.receiptofi.utils.DateUtil;
import com.receiptofi.utils.PerformanceProfiling;
import com.receiptofi.web.helper.AnchorFileInExcel;
import com.receiptofi.web.helper.json.ExcelFileName;
import com.receiptofi.web.scheduledtasks.FileSystemProcessor;
import com.receiptofi.web.view.ExpensofiExcelView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import org.joda.time.DateTime;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.mongodb.gridfs.GridFSDBFile;

/**
 * User: hitender
 * Date: 11/30/13 2:45 AM
 */
@Controller
@RequestMapping(value = "/access/expensofi")
public final class ExpensofiController {
    private static final Logger log = LoggerFactory.getLogger(ExpensofiController.class);

    @Autowired private ReceiptService receiptService;
    @Autowired private NotificationService notificationService;
    @Autowired private FileDBService fileDBService;
    @Autowired private ItemAnalyticService itemAnalyticService;
    @Autowired private FileSystemProcessor fileSystemProcessor;

    @RequestMapping(value = "/items", method = RequestMethod.POST)
    public @ResponseBody
    String expensofi(@RequestBody String itemIds, Model model) throws IOException {
        DateTime time = DateUtil.now();
        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        JsonArray jsonItems = getJsonElements(itemIds);
        List<ItemEntity> items = getItemEntities(receiptUser.getRid(), jsonItems);

        if(!items.isEmpty()) {
            model.addAttribute("items", items);
            Assert.notNull(model.asMap().get("items"));

            ReceiptEntity receiptEntity = items.get(0).getReceipt();
            Collection<AnchorFileInExcel> anchorFileInExcels = new LinkedList<>();
            for(FileSystemEntity fileId : receiptEntity.getFileSystemEntities()) {
                GridFSDBFile gridFSDBFile = fileDBService.getFile(fileId.getBlobId());
                InputStream is = null;
                try {
                    is = gridFSDBFile.getInputStream();
                    AnchorFileInExcel anchorFileInExcel = new AnchorFileInExcel(IOUtils.toByteArray(is), gridFSDBFile.getContentType());
                    anchorFileInExcels.add(anchorFileInExcel);
                } catch (IOException exce) {
                    log.error("Failed to load receipt image: " + exce.getLocalizedMessage());
                } finally {
                    IOUtils.closeQuietly(is);
                }
            }
            model.addAttribute("to_be_anchored_files", anchorFileInExcels);

            try {
                String filename = CreateTempFile.createRandomFilename();
                model.addAttribute("file-name", filename);

                ExpensofiExcelView.newInstance().generateExcel(model.asMap(), new HSSFWorkbook());

                updateReceiptWithExcelFilename(receiptEntity, filename);
                notificationService.addNotification(receiptEntity.getBizName().getBusinessName() + " expense report created", NotificationTypeEnum.EXPENSE_REPORT, receiptEntity);

                PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());
                return new ExcelFileName(filename).asJson();
            } catch (IOException e) {
                log.error("Failure in creating and saving excel report to file system: " + e.getLocalizedMessage(), e);
            }
        }
        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());
        return new ExcelFileName(StringUtils.EMPTY).asJson();
    }

    private void updateReceiptWithExcelFilename(ReceiptEntity receiptEntity, String filename) {
        if(StringUtils.isNotEmpty(receiptEntity.getExpenseReportInFS())) {
            fileSystemProcessor.removeExpiredExcel(receiptEntity.getExpenseReportInFS());
        }
        receiptEntity.setExpenseReportInFS(filename);
        receiptService.updateReceiptWithExpReportFilename(receiptEntity);
    }

    private List<ItemEntity> getItemEntities(String receiptUserId, JsonArray jsonItems) {
        List<ItemEntity> items = new ArrayList<>();
        for(Object jsonItem : jsonItems) {
            ItemEntity ie = itemAnalyticService.findItemById(jsonItem.toString().substring(1, jsonItem.toString().length() - 1), receiptUserId);
            items.add(ie);
        }
        return items;
    }

    private JsonArray getJsonElements(String itemIds) throws UnsupportedEncodingException {
        String result = URLDecoder.decode(itemIds, "UTF-8");
        result = result.substring(0, result.length() - 1);

        JsonObject jsonObject = (JsonObject) new JsonParser().parse(result);
        return (JsonArray) jsonObject.get("items");
    }
}
