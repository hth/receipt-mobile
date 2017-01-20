package com.receiptofi.mobile.web.controller.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import com.receiptofi.mobile.service.AuthenticateService;
import com.receiptofi.service.LandingService;
import com.receiptofi.utils.ScrubbedInput;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

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
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        utilityController = new UtilityController(authenticateService, landingService);
    }

    @Test
    public void testHasAccessFalse() throws IOException {
        when(authenticateService.hasAccess(anyString(), anyString())).thenReturn(false);
        assertNull(utilityController.hasAccess(new ScrubbedInput(""), new ScrubbedInput(""), httpServletResponse));
    }

    @Test
    public void testHasAccess() throws IOException {
        when(authenticateService.hasAccess(anyString(), anyString())).thenReturn(true);
        assertEquals("granted", utilityController.hasAccess(new ScrubbedInput(""), new ScrubbedInput(""), httpServletResponse).getAccess());
    }

    @Test
    public void testUnprocessedDocumentsWhenUserNotFound() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn(null);
        assertNull(utilityController.unprocessedDocuments(new ScrubbedInput(""), new ScrubbedInput(""), httpServletResponse));
    }

    @Test
    public void testUnprocessedDocuments() throws IOException {
        when(authenticateService.getReceiptUserId(anyString(), anyString())).thenReturn("");
        when(landingService.pendingReceipt(anyString())).thenReturn(123L);
        assertEquals(123L, utilityController.unprocessedDocuments(new ScrubbedInput(""), new ScrubbedInput(""), httpServletResponse).getNumberOfUnprocessedFiles());
    }
}