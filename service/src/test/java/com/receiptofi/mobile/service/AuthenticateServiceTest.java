package com.receiptofi.mobile.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import com.receiptofi.domain.UserAccountEntity;
import com.receiptofi.domain.UserAuthenticationEntity;
import com.receiptofi.repository.UserAccountManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        authenticateService = new AuthenticateService(userAccountManager);
    }

    @Test
    public void testHasAccessTrue() {
        when(userAccountManager.findByUserId(anyString())).thenReturn(userAccountEntity);
        when(userAccountEntity.getUserAuthentication()).thenReturn(userAuthenticationEntity);
        when(userAuthenticationEntity.getAuthenticationKey()).thenReturn("auth");
        assertTrue(authenticateService.hasAccess(anyString(), "auth"));
    }

    @Test
    public void testHasAccessFalse() {
        when(userAccountManager.findByUserId(anyString())).thenReturn(null);
        assertFalse(authenticateService.hasAccess(anyString(), "auth"));
    }

    @Test
    public void testFindUserAccountNull() {
        when(userAccountManager.findByUserId(anyString())).thenReturn(null);
        assertEquals(null, authenticateService.findUserAccount("userId", anyString()));
    }

    @Test
    public void testFindUserAccountWhenNotMatchingAuthenticationKey() {
        when(userAccountManager.findByUserId(anyString())).thenReturn(userAccountEntity);
        when(userAccountEntity.getUserAuthentication()).thenReturn(userAuthenticationEntity);
        when(userAuthenticationEntity.getAuthenticationKey()).thenReturn("auth");
        assertEquals(null, authenticateService.findUserAccount(anyString(), "auth1"));
    }

    @Test
    public void testFindUserAccount() {
        when(userAccountManager.findByUserId(anyString())).thenReturn(userAccountEntity);
        when(userAccountEntity.getUserAuthentication()).thenReturn(userAuthenticationEntity);
        when(userAuthenticationEntity.getAuthenticationKey()).thenReturn("auth");
        assertEquals(userAccountEntity, authenticateService.findUserAccount(anyString(), "auth"));
    }

    @Test
    public void testGetReceiptUserIdNull() {
        when(userAccountManager.findByUserId(anyString())).thenReturn(null);
        when(userAccountEntity.getUserAuthentication()).thenReturn(userAuthenticationEntity);
        when(userAuthenticationEntity.getAuthenticationKey()).thenReturn("auth");
        assertEquals(null, authenticateService.getReceiptUserId(anyString(), "auth"));
    }

    @Test
    public void testGetReceiptUserId() {
        when(userAccountManager.findByUserId(anyString())).thenReturn(userAccountEntity);
        when(userAccountEntity.getUserAuthentication()).thenReturn(userAuthenticationEntity);
        when(userAuthenticationEntity.getAuthenticationKey()).thenReturn("auth");
        when(userAccountEntity.getReceiptUserId()).thenReturn("userId");
        assertEquals("userId", authenticateService.getReceiptUserId(anyString(), "auth"));
    }
}
