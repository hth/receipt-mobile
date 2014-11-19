package com.receiptofi.mobile.service;

import com.google.gson.Gson;

import com.receiptofi.domain.EmailValidateEntity;
import com.receiptofi.domain.UserAccountEntity;
import com.receiptofi.mobile.domain.SignupUserInfo;
import com.receiptofi.service.AccountService;
import com.receiptofi.service.EmailValidateService;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;

/**
 * User: hitender
 * Date: 11/8/14 2:00 AM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@Component
public class AccountSignupService {
    private static final Logger LOG = LoggerFactory.getLogger(AccountSignupService.class);

    @Value ("${accountValidation:/webapi/mobile/mail/accountValidation.htm}")
    private String accountValidationEndPoint;

    private WebConnectorService webConnectorService;
    private EmailValidateService emailValidateService;
    private AccountService accountService;

    @Autowired
    public AccountSignupService(
            WebConnectorService webConnectorService,
            EmailValidateService emailValidateService,
            AccountService accountService
    ) {
        this.webConnectorService = webConnectorService;
        this.emailValidateService = emailValidateService;
        this.accountService = accountService;
    }

    /**
     * Signup user and return authenticated key
     *
     * @param mail
     * @param firstName
     * @param lastName
     * @param password
     * @param birthday
     * @return
     */
    public String signup(String mail, String firstName, String lastName, String password, String birthday) {
        UserAccountEntity userAccount;
        try {
            userAccount = accountService.createNewAccount(mail, firstName, lastName, password, birthday);
            Assert.notNull(userAccount);
            LOG.info("Registered new user Id={}", userAccount.getReceiptUserId());
        } catch (RuntimeException exce) {
            LOG.error("failed creating new account for user={} reason={}", mail, exce.getLocalizedMessage(), exce);
            throw new RuntimeException("failed creating new account for user " + mail, exce);
        }

        EmailValidateEntity accountValidate = emailValidateService.saveAccountValidate(
                userAccount.getReceiptUserId(),
                userAccount.getUserId());
        Assert.notNull(accountValidate);

        //TODO(hth) mail sending can be done on background. Just store this as a task.
        boolean mailStatus = sendMailDuringSignup(
                userAccount.getUserId(),
                userAccount.getName(),
                accountValidate.getAuthenticationKey(),
                HttpClientBuilder.create().build());
        LOG.info("mail sent={} to user={}", mailStatus, mail);

        return userAccount.getUserAuthentication().getAuthenticationKey();
    }

    /**
     * Call this on terminal as below.
     * http localhost:9090/receipt-mobile/authenticate.json < ~/Downloads/pid.json
     *
     * @param userId
     * @param name
     * @param auth
     * @return
     */
    public boolean sendMailDuringSignup(String userId, String name, String auth, HttpClient httpClient) {
        LOG.debug("userId={} name={} webApiAccessToken={}", userId, name, "*******");
        HttpPost httpPost = webConnectorService.getHttpPost(accountValidationEndPoint, httpClient);
        if (httpPost == null) {
            LOG.warn("failed connecting, reason={}", webConnectorService.getNoResponseFromWebServer());
            return false;
        }

        populateEntity(userId, name, auth, httpPost);
        HttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            LOG.error("error occurred while executing request path={} reason={}",
                    httpPost.getURI(), e.getLocalizedMessage(), e);
        }

        if (response == null) {
            LOG.warn("failed response, reason={}", webConnectorService.getNoResponseFromWebServer());
            return false;
        }

        int status = response.getStatusLine().getStatusCode();
        LOG.debug("status={}", status);
        if (status >= WebConnectorService.HTTP_STATUS_200 && status < WebConnectorService.HTTP_STATUS_300) {
            return true;
        }

        LOG.error("server responded with response code={}", status);
        return false;
    }

    /**
     * Create Request Body.
     *
     * @param userId
     * @param name
     * @param auth
     * @param httpPost
     */
    private void populateEntity(String userId, String name, String auth, HttpPost httpPost) {
        httpPost.setEntity(
                new StringEntity(
                        new Gson().toJson(SignupUserInfo.newInstance(userId, name, auth)),
                        ContentType.create(MediaType.APPLICATION_JSON_VALUE, "UTF-8")
                )
        );
    }

    public enum REGISTRATION {
        FN, //Firstname
        LN, //Lastname
        EM, //Email
        BD, //Birthday
        PW  //Password
    }
}
