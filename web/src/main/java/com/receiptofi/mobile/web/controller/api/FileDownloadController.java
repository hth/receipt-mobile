package com.receiptofi.mobile.web.controller.api;

import com.mongodb.gridfs.GridFSDBFile;

import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.service.FileDBService;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * User: hitender
 * Date: 8/18/14 10:11 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@Controller
@RequestMapping (value = "/api")
public final class FileDownloadController {
    private static final Logger LOG = LoggerFactory.getLogger(FileDownloadController.class);

    @Autowired private FileDBService fileDBService;
    @Autowired private AuthenticateService authenticateService;

    @Value ("${imageNotFoundPlaceHolder:/static/images/no_image.gif}")
    private String imageNotFound;

    /**
     * Serve images. There is no authentication here other than loading an image for a valid user.
     * TODO(hth) Should image belonging to specific user be allowed to load? Or no Auth required
     *
     * @param mail
     * @param auth
     * @param imageId
     * @param req
     * @param res
     */
    @RequestMapping (method = RequestMethod.GET, value = "/image/{imageId}")
    public void getDocumentImage(
            @RequestHeader ("X-R-MAIL")
            String mail,

            @RequestHeader ("X-R-AUTH")
            String auth,

            @PathVariable
            String imageId,

            HttpServletRequest req,

            HttpServletResponse res
    ) throws IOException {
        String rid = authenticateService.getReceiptUserId(mail, auth);
        if (rid == null) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, UtilityController.UNAUTHORIZED);
        }

        try {
            GridFSDBFile gridFSDBFile = fileDBService.getFile(imageId);

            if (gridFSDBFile == null) {
                LOG.warn("GridFSDBFile failed to find image={}", imageId);
                File file = FileUtils.getFile(req.getServletContext().getRealPath(File.separator) + imageNotFound);
                BufferedImage bi = ImageIO.read(file);
                setContentType(file.getName(), res);
                res.setHeader("Content-Length", String.valueOf(file.length()));
                res.setHeader("Content-Disposition", "inline; filename=" + file.getName());
                OutputStream out = res.getOutputStream();
                ImageIO.write(bi, getFormatForImageIO(file.getName()), out);
                out.close();
            } else {
                LOG.debug("Length={} MetaData={}", gridFSDBFile.getLength(), gridFSDBFile.getMetaData());
                setContentType(gridFSDBFile.getFilename(), res);
                res.setHeader("Content-Length", String.valueOf(gridFSDBFile.getLength()));
                res.setHeader(
                        "Content-Disposition",
                        "inline; filename=" + imageId + "." + FilenameUtils.getExtension(gridFSDBFile.getFilename())
                );
                gridFSDBFile.writeTo(res.getOutputStream());
            }
        } catch (IOException e) {
            LOG.error("Image retrieval error occurred for imageId={} rid={} reason={}",
                    imageId, rid, e.getLocalizedMessage(), e);
        }
    }

    private void setContentType(String filename, HttpServletResponse response) {
        String extension = FilenameUtils.getExtension(filename);
        if (extension.endsWith("jpg") || extension.endsWith("jpeg")) {
            response.setContentType("image/jpeg");
        } else if (extension.endsWith("gif")) {
            response.setContentType("image/gif");
        } else {
            response.setContentType("image/png");
        }
    }

    private String getFormatForImageIO(String filename) {
        String extension = FilenameUtils.getExtension(filename);
        if (extension.endsWith("jpeg")) {
            return "jpg";
        }
        return extension;
    }
}
