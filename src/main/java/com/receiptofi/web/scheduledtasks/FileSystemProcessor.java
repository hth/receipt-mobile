package com.receiptofi.web.scheduledtasks;

import com.receiptofi.service.ReceiptService;
import com.receiptofi.utils.CreateTempFile;
import com.receiptofi.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AgeFileFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * User: hitender
 * Date: 12/7/13 2:18 PM
 */
@Component
public class FileSystemProcessor {
    private static final Logger log = LoggerFactory.getLogger(FileSystemProcessor.class);

    @Value("${expensofiReportLocation}")
    private String expensofiReportLocation;

    @Value("${deleteExcelFileAfterDay:7}")
    private int deleteExcelFileAfterDay;

    @Autowired private ReceiptService receiptService;

    //for every two second use */2 * * * * ? where as cron string blow run every day at 12:00 AM
    @Scheduled(cron="0 0 0 * * ?")
    public void removeExpiredExcelFiles() {
        log.info("FileSystemProcessor.removeExpiredExcelFiles begins");
        int count = 0, found = 0;
        try {
            AgeFileFilter cutoff = new AgeFileFilter(DateUtil.now().minusDays(deleteExcelFileAfterDay).toDate());
            File directory = new File(expensofiReportLocation);
            String[] files = directory.list(cutoff);
            found = files.length;
            for(String filename : files) {
                removeExpiredExcel(getExcelFile(filename));
                receiptService.removeExpensofiFilenameReference(filename);
                count++;
            }
        } finally {
            log.info("FileSystemProcessor.removeExpiredExcelFiles : deletedExcelFile={}, foundExcelFile={}", count, found);
        }

    }

    public File getExcelFile(String filename) {
        return new File(expensofiReportLocation + File.separator + filename);
    }

    public void removeExpiredExcel(File file) {
        FileUtils.deleteQuietly(file);
    }

    public void removeExpiredExcel(String filename) {
        removeExpiredExcel(getExcelFile(filename));
    }

    /**
     * Run this every morning at 9:00 AM
     *
     * @throws IOException
     */
    @Scheduled(cron="0 0 9 * * ?")
    public void removeTempFiles() throws IOException {
        File file = CreateTempFile.file("delete", ".xml");
        File directory = file.getParentFile();

        if(directory.exists()) {
            FilenameFilter textFilter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.startsWith(CreateTempFile.TEMP_FILE_START_WITH);
                }
            };

            int numberOfFiles = directory.listFiles(textFilter).length;
            for(File f : directory.listFiles(textFilter)) {
                log.debug("File={}{}{}", directory, File.separator, f.getName());
                FileUtils.deleteQuietly(f);
            }
            log.info("removed total temp files count={}", numberOfFiles);
        } else {
            log.info("{} directory doesn't exists", directory);
        }

        FileUtils.deleteQuietly(file);
    }
}
