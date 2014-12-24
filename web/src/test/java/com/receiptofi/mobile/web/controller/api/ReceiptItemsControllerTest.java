package com.receiptofi.mobile.web.controller.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.receiptofi.domain.ItemEntity;
import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.service.ItemService;
import com.receiptofi.service.ReceiptService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;

@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
public class ReceiptItemsControllerTest {

    @Mock private AuthenticateService authenticateService;
    @Mock private ItemService itemService;
    @Mock private ReceiptService receiptService;
    @Mock private HttpServletResponse httpServletResponse;
    @Mock private ReceiptEntity receiptEntity;
    @Mock private ItemEntity itemEntity;

    private ReceiptItemsController receiptItemsController;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        receiptItemsController = new ReceiptItemsController(authenticateService, itemService, receiptService);
    }

    @Test
    public void testGetDetailedReceiptWhenUserIsNotPresent() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn(null);
        verify(receiptService, never()).findReceipt(anyString(), anyString());
        assertTrue(receiptItemsController.getDetailedReceipt("mail@mail.com", "", "", httpServletResponse).isEmpty());
    }

    @Test
    public void testGetDetailedReceiptIsNull() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn("rid");
        when(receiptService.findReceipt(anyString(), anyString())).thenReturn(null);
        assertTrue(receiptItemsController.getDetailedReceipt("mail@mail.com", "", "", httpServletResponse).isEmpty());
    }

    @Test
    public void testGetDetailedReceiptWhenItemsAreEmpty() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn("rid");
        when(receiptService.findReceipt(anyString(), anyString())).thenReturn(receiptEntity);
        when(receiptEntity.getId()).thenReturn("id");
        when(itemService.getAllItemsOfReceipt("id")).thenReturn(new ArrayList<ItemEntity>());
        assertTrue(receiptItemsController.getDetailedReceipt("mail@mail.com", "", "", httpServletResponse).isEmpty());
    }

    @Test
    public void testGetDetailedReceiptWhenThrowsException() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn("rid");
        when(receiptService.findReceipt(anyString(), anyString())).thenReturn(receiptEntity);
        when(receiptEntity.getId()).thenReturn("id");
        doThrow(new RuntimeException()).when(itemService).getAllItemsOfReceipt("id");
        assertTrue(receiptItemsController.getDetailedReceipt("mail@mail.com", "", "id", httpServletResponse).isEmpty());
    }


    @Test
    public void testGetDetailedReceipt() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn("rid");
        when(receiptService.findReceipt(anyString(), anyString())).thenReturn(receiptEntity);
        when(receiptEntity.getId()).thenReturn("id");
        when(itemService.getAllItemsOfReceipt("id")).thenReturn(Arrays.asList(itemEntity));
        when(itemEntity.getId()).thenReturn("");
        when(itemEntity.getSequence()).thenReturn(0);
        when(itemEntity.getQuantity()).thenReturn(0.0);
        when(itemEntity.getPrice()).thenReturn(0.0);
        when(itemEntity.getTax()).thenReturn(0.0);
        when(itemEntity.getReceipt()).thenReturn(receiptEntity);

        assertFalse(receiptItemsController.getDetailedReceipt("mail@mail.com", "", "id", httpServletResponse).isEmpty());
    }
}
