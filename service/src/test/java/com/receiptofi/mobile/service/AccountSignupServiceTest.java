package com.receiptofi.mobile.service;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.receiptofi.domain.EmailValidateEntity;
import com.receiptofi.domain.UserAccountEntity;
import com.receiptofi.domain.UserAuthenticationEntity;
import com.receiptofi.service.AccountService;
import com.receiptofi.service.EmailValidateService;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicStatusLine;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith (MockitoJUnitRunner.class)
public class AccountSignupServiceTest {

    @Mock private WebConnectorService webConnectorService;
    @Mock private EmailValidateService emailValidateService;
    @Mock private AccountService accountService;

    private AccountSignupService accountSignupService;
    private HttpPost httpPost;
    private HttpClient httpClient;
    private HttpResponse httpResponse;
    private BasicStatusLine basicStatusLine;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        httpPost = mock(HttpPost.class);
        httpClient = mock(HttpClient.class);
        httpResponse = mock(HttpResponse.class);
        basicStatusLine = mock(BasicStatusLine.class);
        accountSignupService = new AccountSignupService(
                webConnectorService,
                emailValidateService,
                accountService
        );
    }

    @Test (expected = RuntimeException.class)
    public void testSignupWhenUserAccountIsNull() {
        when(accountService.createNewAccount(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(null);
        accountSignupService.signup("", "", "", "", "");
    }

    @Test (expected = RuntimeException.class)
    public void testSignupWhenUserAccountThrowsException() throws Exception {
        doThrow(new RuntimeException())
                .when(accountService)
                .createNewAccount(anyString(), anyString(), anyString(), anyString(), anyString());
        accountSignupService.signup("", "", "", "", "");
    }

    @Test (expected = IllegalArgumentException.class)
    public void testSignupWhenEmailValidateIsNull() {
        when(accountService.createNewAccount(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(UserAccountEntity.newInstance("", "", "", "", null));

        when(emailValidateService.saveAccountValidate("", "")).thenReturn(null);
        assertEquals("", accountSignupService.signup("", "", "", "", ""));
    }

    @Test
    public void tesSignupSuccess() {
        UserAuthenticationEntity userAuthentication = UserAuthenticationEntity.newInstance("", "authKey");
        UserAccountEntity userAccount = UserAccountEntity.newInstance("", "", "", "", userAuthentication);
        when(accountService.createNewAccount(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(userAccount);

        EmailValidateEntity emailValidate = EmailValidateEntity.newInstance("", "", "email-validation-auth-key");
        when(emailValidateService.saveAccountValidate("", "")).thenReturn(emailValidate);

        assertEquals("authKey", accountSignupService.signup("", "", "", "", ""));
    }

    @Test
    public void sendMailDuringSignupHttpPostNull() {
        when(webConnectorService.getHttpPost("", HttpClientBuilder.create().build())).thenReturn(null);
        assertNotNull(null, accountSignupService.sendMailDuringSignup("", "", "", httpClient));
    }

    @Test
    public void sendMailDuringSignupHttpResponseNull() throws IOException {
        when(webConnectorService.getHttpPost(anyString(), any(HttpClient.class))).thenReturn(httpPost);
        when(httpClient.execute(httpPost)).thenReturn(null);

        assertNotNull(null, accountSignupService.sendMailDuringSignup("", "", "", httpClient));
    }

    @Test
    public void sendMailDuringSignupStatus501() throws IOException {
        when(webConnectorService.getHttpPost(anyString(), any(HttpClient.class))).thenReturn(httpPost);
        when(httpClient.execute(httpPost)).thenReturn(httpResponse);
        when(httpResponse.getStatusLine()).thenReturn(basicStatusLine);
        when(basicStatusLine.getStatusCode()).thenReturn(WebConnectorServiceTest.HTTP_CODE_ERROR);

        assertFalse(accountSignupService.sendMailDuringSignup("", "", "", httpClient));
    }

    @Test
    public void sendMailDuringSignupSuccess() throws IOException {
        when(webConnectorService.getHttpPost(anyString(), any(HttpClient.class))).thenReturn(httpPost);
        when(httpClient.execute(httpPost)).thenReturn(httpResponse);
        when(httpResponse.getStatusLine()).thenReturn(basicStatusLine);
        when(basicStatusLine.getStatusCode()).thenReturn(WebConnectorServiceTest.HTTP_CODE_SUCCESS);

        assertTrue(accountSignupService.sendMailDuringSignup("", "", "", httpClient));
    }
}