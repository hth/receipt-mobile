/**
 *
 */
package com.receiptofi.web.controller.access;

import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.site.ReceiptUser;
import com.receiptofi.service.FileDBService;
import com.receiptofi.service.ReceiptService;
import com.receiptofi.utils.DateUtil;
import com.receiptofi.utils.Formatter;
import com.receiptofi.utils.PerformanceProfiling;
import com.receiptofi.web.scheduledtasks.FileSystemProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.joda.time.DateTime;

import com.mongodb.gridfs.GridFSDBFile;

/**
 * @author hitender
 * @since Jan 6, 2013 8:21:54 PM
 *
 */
@Controller
@RequestMapping(value = "/access/filedownload")
public final class FileDownloadController {
	private static final Logger log = LoggerFactory.getLogger(FileDownloadController.class);

	@Autowired private FileDBService fileDBService;
    @Autowired private FileSystemProcessor fileSystemProcessor;
    @Autowired private ReceiptService receiptService;

    @Value("${imageNotFoundPlaceHolder:/static/images/no_image.gif}")
    private String imageNotFound;

	/**
	 * This is used only to serve images of Receipt
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/receiptimage/{imageId}")
	public void getReceipt(@PathVariable String imageId, HttpServletRequest request, HttpServletResponse response) {
        DateTime time = DateUtil.now();
        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		try {
			GridFSDBFile gridFSDBFile = fileDBService.getFile(imageId);

			if(gridFSDBFile == null) {
                log.warn("GridFSDBFile is null; failedToFindImage="+ imageId);
				String pathToWeb = request.getServletContext().getRealPath(File.separator);
    			File file = FileUtils.getFile(pathToWeb + imageNotFound);
//                File file = FileUtils.getFile(pathToWeb + "/static/images/no_image_found.jpg");
				BufferedImage bi = ImageIO.read(file);
                setContentType(file, response);
				OutputStream out = response.getOutputStream();
				ImageIO.write(bi, getFormatForFile(file), out);
				out.close();
			} else {
                log.debug("Length: " + gridFSDBFile.getLength() + ", MetaData: " + gridFSDBFile.getMetaData());
				gridFSDBFile.writeTo(response.getOutputStream());
				response.setContentType(gridFSDBFile.getContentType());
			}

            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(),  true);
		} catch (IOException e) {
			log.error("Exception occurred during image retrieval" + e.getLocalizedMessage());
			log.error("Image retrieval error occurred: " + imageId + " for user : " + receiptUser.getRid());
            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "error fetching receipt");
		}
	}

    @RequestMapping(method = RequestMethod.GET, value = "/expensofi/{receiptId}")
    public void getReport(@PathVariable String receiptId, HttpServletResponse response) {
        DateTime time = DateUtil.now();
        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            ReceiptEntity receiptEntity = receiptService.findReceipt(receiptId, receiptUser.getRid());
            setHeaderForExcel(receiptEntity, response);

            InputStream inputStream = new FileInputStream(fileSystemProcessor.getExcelFile(receiptEntity.getExpenseReportInFS()));
            IOUtils.copy(inputStream, response.getOutputStream());

            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(),  true);
        } catch (IOException e) {
            log.error("Excel retrieval error occurred Receipt={} for user={} reason={}", receiptId, receiptUser.getRid(), e.getLocalizedMessage(), e);
            PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName(), "error fetching receipt");
        }
    }

    private void setHeaderForExcel(ReceiptEntity receiptEntity, HttpServletResponse response) {
        response.addHeader("Content-Disposition", "inline; filename=" + receiptEntity.getBizName().getBusinessName() + "-" + Formatter.toSmallDate(receiptEntity.getReceiptDate()));
        response.setContentType("application/vnd.ms-excel");
    }

    private void setContentType(File file, HttpServletResponse response) {
        String extension = FilenameUtils.getExtension(file.getName());
        if(extension.endsWith("jpg") || extension.endsWith("jpeg")) {
            response.setContentType("image/jpeg");
            return;
        }
        if(extension.endsWith("gif")) {
            response.setContentType("image/gif");
            return;
        }
        response.setContentType("image/png");
    }

    private String getFormatForFile(File file) {
        String extension = FilenameUtils.getExtension(file.getName());
        if(extension.endsWith("jpg") || extension.endsWith("jpeg")) {
            return "jpg";
        }
        if(extension.endsWith("gif")) {
            return "gif";
        }
        return "png";
    }
}
