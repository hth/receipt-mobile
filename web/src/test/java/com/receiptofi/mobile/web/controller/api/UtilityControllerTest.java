package com.receiptofi.mobile.web.controller.api;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.service.LandingService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletResponse;

@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
public class UtilityControllerTest {

    @Mock private AuthenticateService authenticateService;
    @Mock private LandingService landingService;
    @Mock private HttpServletResponse httpServletResponse;
    private UtilityController utilityController;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        utilityController = new UtilityController(authenticateService, landingService);
    }

    @Test
    public void testHasAccessFalse() throws Exception {
        when(authenticateService.hasAccess(anyString(), anyString())).thenReturn(false);
        assertNull(utilityController.hasAccess(anyString(), anyString(), httpServletResponse));
    }

    @Test
    public void testHasAccess() throws Exception {
        when(authenticateService.hasAccess(anyString(), anyString())).thenReturn(true);
        assertEquals("granted", utilityController.hasAccess(anyString(), anyString(), httpServletResponse).getAccess());
    }

    @Test
    public void testUnprocessedDocumentsWhenUserNotFound() throws Exception {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn(null);
        assertNull(utilityController.unprocessedDocuments(anyString(), anyString(), httpServletResponse));
    }

    @Test
    public void testUnprocessedDocuments() throws Exception {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn("");
        when(landingService.pendingReceipt(anyString())).thenReturn(123l);
        assertEquals(123l, utilityController.unprocessedDocuments(anyString(), anyString(), httpServletResponse).getNumberOfUnprocessedFiles());
    }
}