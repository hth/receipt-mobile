package com.receiptofi.mobile.web.controller.api;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.service.FileDBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.mongodb.gridfs.GridFSDBFile;

/**
 * User: hitender
 * Date: 8/18/14 10:11 PM
 */
@Controller
@RequestMapping (value = "/api")
public final class FileDownloadController {
    private static final Logger log = LoggerFactory.getLogger(FileDownloadController.class);

    @Autowired private FileDBService fileDBService;
    @Autowired private AuthenticateService authenticateService;

    @Value ("${imageNotFoundPlaceHolder:/static/images/no_image.gif}")
    private String imageNotFound;

    /**
     * Serve images. There is no authentication here other than loading an image for a valid user.
     * TODO Should image belonging to specific user be allowed to load? Or no Auth required
     *
     * @param mail
     * @param auth
     * @param imageId
     * @param request
     * @param response
     */
    @RequestMapping (method = RequestMethod.GET, value = "/image/{imageId}")
    public void getDocumentImage(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            @PathVariable
            String imageId,

            HttpServletRequest request,

            HttpServletResponse response
    ) throws IOException {
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (rid != null) {
            try {
                GridFSDBFile gridFSDBFile = fileDBService.getFile(imageId);

                if (gridFSDBFile == null) {
                    log.warn("GridFSDBFile failed to find image={}", imageId);
                    File file = FileUtils.getFile(request.getServletContext().getRealPath(File.separator) + imageNotFound);
                    BufferedImage bi = ImageIO.read(file);
                    setContentType(file, response);
                    OutputStream out = response.getOutputStream();
                    ImageIO.write(bi, getFormatForFile(file), out);
                    out.close();
                } else {
                    log.debug("Length={} MetaData={}", gridFSDBFile.getLength(), gridFSDBFile.getMetaData());
                    response.setContentType(gridFSDBFile.getContentType());
                    gridFSDBFile.writeTo(response.getOutputStream());
                }
            } catch (IOException e) {
                log.error("Image retrieval error occurred for imageId={} rid={} reason={}", imageId, rid, e.getLocalizedMessage(), e);
            }
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }
    }

    private void setContentType(File file, HttpServletResponse response) {
        String extension = FilenameUtils.getExtension(file.getName());
        if (extension.endsWith("jpg") || extension.endsWith("jpeg")) {
            response.setContentType("image/jpeg");
            return;
        }
        if (extension.endsWith("gif")) {
            response.setContentType("image/gif");
            return;
        }
        response.setContentType("image/png");
    }

    private String getFormatForFile(File file) {
        String extension = FilenameUtils.getExtension(file.getName());
        if (extension.endsWith("jpg") || extension.endsWith("jpeg")) {
            return "jpg";
        }
        if (extension.endsWith("gif")) {
            return "gif";
        }
        return "png";
    }
}
