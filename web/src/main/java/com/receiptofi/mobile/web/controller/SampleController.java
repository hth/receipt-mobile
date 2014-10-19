package com.receiptofi.mobile.web.controller;

import com.mongodb.gridfs.GridFSDBFile;

import com.receiptofi.service.FileDBService;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
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
 * Date: 10/18/14 10:22 PM
 */
@Controller
public class SampleController {
    private static final Logger LOG = LoggerFactory.getLogger(SampleController.class);

    @Autowired private FileDBService fileDBService;
    @Value ("${imageNotFoundPlaceHolder:/static/images/no_image.gif}")
    private String imageNotFound;

    @RequestMapping (method = RequestMethod.GET, value = "/image1/{imageId}")
    public void getDocumentImage1(
            @PathVariable
            String imageId,

            HttpServletRequest request,

            HttpServletResponse response
    ) throws IOException {
        try {
            GridFSDBFile gridFSDBFile = fileDBService.getFile(imageId);

            if (gridFSDBFile == null) {
                LOG.warn("GridFSDBFile failed to find image={}", imageId);
                File file = FileUtils.getFile(request.getServletContext().getRealPath(File.separator) + imageNotFound);
                BufferedImage bi = ImageIO.read(file);
                setContentType(file.getName(), response);
                response.setHeader("Content-Length", String.valueOf(file.length()));
                response.setHeader("Content-Disposition", "inline; filename=" + file.getName());
                OutputStream out = response.getOutputStream();
                ImageIO.write(bi, getFormatForImageIO(file.getName()), out);
                out.close();
            } else {
                LOG.debug("Length={} MetaData={}", gridFSDBFile.getLength(), gridFSDBFile.getMetaData());
                setContentType(gridFSDBFile.getFilename(), response);
                response.setHeader("Content-Length", String.valueOf(gridFSDBFile.getLength()));
                response.setHeader("Content-Disposition", "inline; filename=" + imageId + "." + FilenameUtils.getExtension(gridFSDBFile.getFilename()));
                gridFSDBFile.writeTo(response.getOutputStream());
            }
        } catch (IOException e) {
            LOG.error("Image retrieval error occurred for imageId={} reason={}", imageId, e.getLocalizedMessage(), e);
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
        if (extension.endsWith("jpg") || extension.endsWith("jpeg")) {
            return "jpg";
        } else if (extension.endsWith("gif")) {
            return "gif";
        } else {
            return "png";
        }
    }
}
