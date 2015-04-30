package com.receiptofi.mobile.web.controller.api;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;

import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.service.FileDBService;

import org.apache.commons.io.FileUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
public class FileDownloadControllerTest {

    @Rule public TemporaryFolder folder = new TemporaryFolder();
    @Mock private FileDBService fileDBService;
    @Mock private AuthenticateService authenticateService;
    @Mock private HttpServletResponse httpServletResponse;
    @Mock private HttpServletRequest httpServletRequest;
    @Mock private GridFSDBFile gridFSDBFile;
    @Mock private ServletContext servletContext;
    @Mock private ServletOutputStream outputStream;
    @Mock private DBObject dbObject;

    private FileDownloadController fileDownloadController;
    private File createdFile;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        folder.newFolder("static", "images");
        createdFile = folder.newFile("/static/images/no_image.gif");
        FileUtils.copyFile(
                new File(this.getClass().getResource("/no_image.gif").getFile()),
                new File(createdFile.getAbsolutePath()));

        fileDownloadController = new FileDownloadController(
                createdFile.getAbsolutePath(),
                fileDBService,
                authenticateService);

        when(servletContext.getRealPath(anyString())).thenReturn("");
        when(httpServletResponse.getOutputStream()).thenReturn(outputStream);
    }

    @Test
    public void testHasUpdateFailsToFindUser() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn(null);
        fileDownloadController.getDocumentImage("", "", "", httpServletRequest, httpServletResponse);
        verify(fileDBService, never()).getFile(anyString());
    }

    @Test
    public void testGetDocumentImageWhenGridFSDBFileIsNull() throws Exception {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn("");
        when(fileDBService.getFile(anyString())).thenReturn(null);
        when(httpServletRequest.getServletContext()).thenReturn(servletContext);
        fileDownloadController.getDocumentImage("", "", "imageId", httpServletRequest, httpServletResponse);
        verify(outputStream, times(1)).close();
    }

    @Test
    public void testGetDocumentImage() throws Exception {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn("");
        when(fileDBService.getFile(anyString())).thenReturn(gridFSDBFile);
        when(gridFSDBFile.getFilename()).thenReturn("filename");
        fileDownloadController.getDocumentImage("", "", "ab", httpServletRequest, httpServletResponse);
        verify(gridFSDBFile, times(1)).writeTo(httpServletResponse.getOutputStream());
    }
}
