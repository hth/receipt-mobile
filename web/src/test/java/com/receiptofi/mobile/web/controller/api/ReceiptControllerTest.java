package com.receiptofi.mobile.web.controller.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.receiptofi.domain.BizNameEntity;
import com.receiptofi.domain.BizStoreEntity;
import com.receiptofi.domain.CommentEntity;
import com.receiptofi.domain.FileSystemEntity;
import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.json.JsonReceipt;
import com.receiptofi.domain.types.BilledStatusEnum;
import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.mobile.service.ReceiptMobileService;
import com.receiptofi.service.LandingService;

import org.joda.time.DateTime;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
public class ReceiptControllerTest {

    @Mock private LandingService landingService;
    @Mock private AuthenticateService authenticateService;
    @Mock private HttpServletResponse httpServletResponse;
    @Mock private ReceiptEntity receiptEntity;
    @Mock private BizNameEntity bizName;
    @Mock private BizStoreEntity bizStore;
    @Mock private CommentEntity comment;
    @Mock private FileSystemEntity fileSystem;
    @Mock private ReceiptMobileService receiptMobileService;

    private ReceiptController receiptController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        receiptController = new ReceiptController(landingService, authenticateService, receiptMobileService);

        when(receiptEntity.getBizName()).thenReturn(bizName);
        when(receiptEntity.getBizStore()).thenReturn(bizStore);
        when(receiptEntity.getNotes()).thenReturn(comment);
        when(receiptEntity.getFileSystemEntities()).thenReturn(Collections.singletonList(fileSystem));
        when(receiptEntity.getReceiptDate()).thenReturn(new Date());
        when(receiptEntity.getReceiptUserId()).thenReturn("rid");
        when(receiptEntity.getBilledStatus()).thenReturn(BilledStatusEnum.P);
    }

    @Test
    public void testYtdReceiptsWhenUserIsNotPresent() throws Exception {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn(null);
        verify(landingService, never()).getAllReceiptsForTheYear(anyString(), any(DateTime.class));
        assertTrue(receiptController.ytdReceipts("mail@mail.com", "", httpServletResponse).isEmpty());
    }

    @Test
    public void testYtdReceiptsEmpty() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn("rid");
        when(landingService.getAllReceiptsForTheYear(anyString(), any(DateTime.class))).thenReturn(new ArrayList<ReceiptEntity>());
        assertTrue(receiptController.ytdReceipts("mail@mail.com", "", httpServletResponse).isEmpty());
    }

    @Test
    public void testYtdReceiptsException() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn("rid");
        doThrow(new RuntimeException()).when(landingService).getAllReceiptsForTheYear(anyString(), any(DateTime.class));
        assertTrue(receiptController.ytdReceipts("mail@mail.com", "", httpServletResponse).isEmpty());
    }

    @Test
    public void testYtdReceipts() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn("rid");
        when(landingService.getAllReceiptsForTheYear(anyString(), any(DateTime.class))).thenReturn(Collections.singletonList(receiptEntity));
        List<JsonReceipt> jsonReceipts = receiptController.ytdReceipts("mail@mail.com", "", httpServletResponse);
        assertEquals(receiptEntity.getReceiptUserId(), jsonReceipts.get(0).getReceiptUserId());
    }

    @Test
    public void testAllReceiptsWhenUserIsNotPresent() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn(null);
        verify(landingService, never()).getAllReceipts(anyString());
        assertTrue(receiptController.allReceipts("mail@mail.com", "", httpServletResponse).isEmpty());
    }

    @Test
    public void testAllReceiptsEmpty() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn("rid");
        when(landingService.getAllReceipts(anyString())).thenReturn(new ArrayList<>());
        assertTrue(receiptController.allReceipts("mail@mail.com", "", httpServletResponse).isEmpty());
    }

    @Test
    public void testAllReceiptsException() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn("rid");
        doThrow(new RuntimeException()).when(landingService).getAllReceipts(anyString());
        assertTrue(receiptController.allReceipts("mail@mail.com", "", httpServletResponse).isEmpty());
    }

    @Test
    public void testAllReceipts() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn("rid");
        when(landingService.getAllReceipts(anyString())).thenReturn(Collections.singletonList(receiptEntity));
        List<JsonReceipt> jsonReceipts = receiptController.allReceipts("mail@mail.com", "", httpServletResponse);
        assertEquals(receiptEntity.getReceiptUserId(), jsonReceipts.get(0).getReceiptUserId());
    }

    @Test
    public void testThisMonthReceiptsWhenUserIsNotPresent() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn(null);
        verify(landingService, never()).getAllReceiptsForThisMonth(anyString(), any(DateTime.class));
        assertTrue(receiptController.thisMonthReceipts("mail@mail.com", "", httpServletResponse).isEmpty());
    }

    @Test
    public void testThisMonthReceiptsIsEmpty() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn("rid");
        when(landingService.getAllReceiptsForThisMonth(anyString(), any(DateTime.class))).thenReturn(new ArrayList<>());
        assertTrue(receiptController.thisMonthReceipts("mail@mail.com", "", httpServletResponse).isEmpty());
    }

    @Test
    public void testThisMonthReceiptsException() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn("rid");
        doThrow(new RuntimeException()).when(landingService).getAllReceiptsForThisMonth(anyString(), any(DateTime.class));
        assertTrue(receiptController.thisMonthReceipts("mail@mail.com", "", httpServletResponse).isEmpty());
    }

    @Test
    public void testThisMonthReceipts() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn("rid");
        when(landingService.getAllReceiptsForThisMonth(anyString(), any(DateTime.class))).thenReturn(Arrays.asList(receiptEntity));
        List<JsonReceipt> jsonReceipts = receiptController.thisMonthReceipts("mail@mail.com", "", httpServletResponse);
        assertEquals(receiptEntity.getReceiptUserId(), jsonReceipts.get(0).getReceiptUserId());
    }
}
