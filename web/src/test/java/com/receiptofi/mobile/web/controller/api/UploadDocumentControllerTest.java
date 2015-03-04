package com.receiptofi.mobile.web.controller.api;

import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.DOCUMENT_UPLOAD;
import static com.receiptofi.mobile.web.controller.AccountControllerTest.ERROR;
import static com.receiptofi.mobile.web.controller.AccountControllerTest.REASON;
import static com.receiptofi.mobile.web.controller.AccountControllerTest.SYSTEM_ERROR;
import static com.receiptofi.mobile.web.controller.AccountControllerTest.SYSTEM_ERROR_CODE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.service.LandingService;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
public class UploadDocumentControllerTest {

    @Mock private AuthenticateService authenticateService;
    @Mock private LandingService landingService;
    @Mock private HttpServletResponse httpServletResponse;
    @Mock private ServletRequestWrapper servletRequestWrapper;
    @Mock private HttpServletRequest httpServletRequest;
    @Mock private MultipartHttpServletRequest multipartHttpServletRequest;
    @Mock private MultipartFile multipartFile;

    private UploadDocumentController uploadDocumentController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        uploadDocumentController = new UploadDocumentController(landingService, authenticateService);
        when(servletRequestWrapper.getRequest()).thenReturn(httpServletRequest);
    }

    @Test
    public void testUploadWhenUserIsNotPresent() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn(null);
        assertNull(uploadDocumentController.upload("mail@mail.com", "", multipartHttpServletRequest, httpServletResponse));
    }

    @Test
    public void testUploadWhenRequestTypeIsIncorrect() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn("");
        String responseJson = uploadDocumentController.upload("mail@mail.com", "", httpServletRequest, httpServletResponse);

        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertEquals(DOCUMENT_UPLOAD.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(DOCUMENT_UPLOAD.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("Failed to upload file.", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());
    }

    @Test
    public void testUploadWhenFilesEmpty() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn("rid");
        when(multipartHttpServletRequest.getContentType()).thenReturn("multipart/mixed;");
        when(multipartHttpServletRequest.getMethod()).thenReturn("POST");
        String responseJson = uploadDocumentController.upload("mail@mail.com", "", multipartHttpServletRequest, httpServletResponse);

        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertEquals(DOCUMENT_UPLOAD.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(DOCUMENT_UPLOAD.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("qqfile name missing in request or no file uploaded", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());
    }

    @Test
    public void testUploadFileSizeZero() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn("rid");
        when(multipartHttpServletRequest.getContentType()).thenReturn("multipart/mixed;");
        when(multipartHttpServletRequest.getMethod()).thenReturn("POST");
        when(multipartHttpServletRequest.getFiles(anyString())).thenReturn(Arrays.asList(multipartFile));
        when(multipartFile.getSize()).thenReturn(0L);
        when(multipartFile.getOriginalFilename()).thenReturn("filename");

        String responseJson = uploadDocumentController.upload("mail@mail.com", "", multipartHttpServletRequest, httpServletResponse);
        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertEquals(DOCUMENT_UPLOAD.getCode(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR_CODE).getAsString());
        assertEquals(DOCUMENT_UPLOAD.name(), jo.get(ERROR).getAsJsonObject().get(SYSTEM_ERROR).getAsString());
        assertEquals("Failed to upload file.", jo.get(ERROR).getAsJsonObject().get(REASON).getAsString());
        assertEquals("filename", jo.get(ERROR).getAsJsonObject().get("document").getAsString());
    }

    @Test
    public void testUploadFile() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn("rid");
        when(multipartHttpServletRequest.getContentType()).thenReturn("multipart/mixed;");
        when(multipartHttpServletRequest.getMethod()).thenReturn("POST");
        when(multipartHttpServletRequest.getFiles(anyString())).thenReturn(Arrays.asList(multipartFile));
        when(multipartFile.getSize()).thenReturn(1L);
        when(multipartFile.getOriginalFilename()).thenReturn("filename");
        when(landingService.pendingReceipt(anyString())).thenReturn(1L);
        String responseJson = uploadDocumentController.upload("mail@mail.com", "", multipartHttpServletRequest, httpServletResponse);
        responseJson = StringUtils.replace(responseJson, "null", "\"blobId\"");

        JsonObject jo = (JsonObject) new JsonParser().parse(responseJson);
        assertEquals("blobId", jo.getAsJsonObject().get("blobId").getAsString());
        assertEquals("filename", jo.getAsJsonObject().get("uploadedDocumentName").getAsString());
        assertEquals("1", jo.get("unprocessedDocuments").getAsJsonObject().get("unprocessedCount").getAsString());
    }
}