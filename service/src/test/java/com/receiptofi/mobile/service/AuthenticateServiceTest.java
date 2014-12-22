package com.receiptofi.mobile.service;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import com.receiptofi.domain.UserAccountEntity;
import com.receiptofi.domain.UserAuthenticationEntity;
import com.receiptofi.repository.UserAccountManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URLEncoder;

@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
public class AuthenticateServiceTest {

    @Mock private UserAccountManager userAccountManager;
    @Mock private UserAccountEntity userAccountEntity;
    @Mock private UserAuthenticationEntity userAuthenticationEntity;

    private AuthenticateService authenticateService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        authenticateService = new AuthenticateService(userAccountManager);
    }

    @Test
    public void testHasAccessTrue() throws Exception {
        when(userAccountManager.findByUserId(anyString())).thenReturn(userAccountEntity);
        when(userAccountEntity.getUserAuthentication()).thenReturn(userAuthenticationEntity);
        when(userAuthenticationEntity.getAuthenticationKey()).thenReturn("auth");
        assertTrue(authenticateService.hasAccess(anyString(), "auth"));
    }

    @Test
    public void testHasAccessFalse() throws Exception {
        when(userAccountManager.findByUserId(anyString())).thenReturn(null);
        assertFalse(authenticateService.hasAccess(anyString(), "auth"));
    }

    @Test
    public void testFindUserAccountNull() throws Exception {
        when(userAccountManager.findByUserId(anyString())).thenReturn(null);
        assertEquals(null, authenticateService.findUserAccount("userId", anyString()));
    }

    @Test
    public void testFindUserAccountWhenNotMatchingAuthenticationKey() throws Exception {
        when(userAccountManager.findByUserId(anyString())).thenReturn(userAccountEntity);
        when(userAccountEntity.getUserAuthentication()).thenReturn(userAuthenticationEntity);
        when(userAuthenticationEntity.getAuthenticationKey()).thenReturn("auth");
        assertEquals(null, authenticateService.findUserAccount(anyString(), "auth1"));
    }

    @Test
    public void testFindUserAccount() throws Exception {
        when(userAccountManager.findByUserId(anyString())).thenReturn(userAccountEntity);
        when(userAccountEntity.getUserAuthentication()).thenReturn(userAuthenticationEntity);
        when(userAuthenticationEntity.getAuthenticationKey()).thenReturn("auth");
        assertEquals(userAccountEntity, authenticateService.findUserAccount(anyString(), "auth"));
    }

    @Test
    public void testGetReceiptUserIdNull() throws Exception {
        when(userAccountManager.findByUserId(anyString())).thenReturn(null);
        when(userAccountEntity.getUserAuthentication()).thenReturn(userAuthenticationEntity);
        when(userAuthenticationEntity.getAuthenticationKey()).thenReturn("auth");
        assertEquals(null, authenticateService.getReceiptUserId(anyString(), "auth"));
    }

    @Test
    public void testGetReceiptUserId() throws Exception {
        when(userAccountManager.findByUserId(anyString())).thenReturn(userAccountEntity);
        when(userAccountEntity.getUserAuthentication()).thenReturn(userAuthenticationEntity);
        when(userAuthenticationEntity.getAuthenticationKey()).thenReturn("auth");
        when(userAccountEntity.getReceiptUserId()).thenReturn("userId");
        assertEquals("userId", authenticateService.getReceiptUserId(anyString(), "auth"));
    }
}