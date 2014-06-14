package com.receiptofi.service;

import com.receiptofi.domain.EmailValidateEntity;
import com.receiptofi.domain.ForgotRecoverEntity;
import com.receiptofi.domain.InviteEntity;
import com.receiptofi.domain.UserAccountEntity;
import com.receiptofi.domain.UserAuthenticationEntity;
import com.receiptofi.domain.UserPreferenceEntity;
import com.receiptofi.domain.UserProfileEntity;
import com.receiptofi.repository.InviteManager;
import com.receiptofi.repository.UserAccountManager;
import com.receiptofi.repository.UserAuthenticationManager;
import com.receiptofi.repository.UserPreferenceManager;
import com.receiptofi.repository.UserProfileManager;
import com.receiptofi.utils.HashText;
import com.receiptofi.utils.RandomString;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.ui.freemarker.FreeMarkerTemplateUtils.processTemplateIntoString;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;
import org.springframework.util.Assert;

/**
 * User: hitender
 * Date: 6/9/13
 * Time: 10:20 AM
 */
@Service
public final class MailService {
    private static Logger log = LoggerFactory.getLogger(MailService.class);

    @Autowired private AccountService accountService;

    @Autowired private InviteService inviteService;

    @Autowired private JavaMailSenderImpl mailSender;

    @Autowired private LoginService loginService;

    @Autowired private InviteManager inviteManager;

    @Autowired private UserProfileManager userProfileManager;

    @Autowired private UserAuthenticationManager userAuthenticationManager;

    @Autowired private UserPreferenceManager userPreferenceManager;

    @Autowired private UserAccountManager userAccountManager;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private FreeMarkerConfigurationFactoryBean freemarkerConfiguration;

    @Value("${do.not.reply.email}")
    private String doNotReplyEmail;

    @Value("${dev.sent.to}")
    private String devSentTo;

    @Value("${invitee.email}")
    private String inviteeEmail;

    @Value("${email.address.name}")
    private String emailAddressName;

    @Value("${domain}")
    private String domain;

    @Value("${https}")
    private String https;

    @Value("${mail.invite.subject}")
    private String mailInviteSubject;

    @Value("${mail.recover.subject}")
    private String mailRecoverSubject;

    @Value("${mail.validate.subject}")
    private String mailValidateSubject;

    public boolean accountValidationEmail(UserAccountEntity userAccount, EmailValidateEntity accountValidate) {
        Assert.notNull(userAccount);
        Map<String, String> rootMap = new HashMap<>();
        rootMap.put("to", userAccount.getName());
        rootMap.put("contact_email", userAccount.getUserId());
        rootMap.put("link", accountValidate.getAuthenticationKey());
        rootMap.put("domain", domain);
        rootMap.put("https", https);

        try {
            MimeMessage message = mailSender.createMimeMessage();

            // use the true flag to indicate you need a multipart message
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(new InternetAddress(doNotReplyEmail, emailAddressName));

            String sentTo = StringUtils.isEmpty(devSentTo) ? userAccount.getUserId() : devSentTo;
            if(!sentTo.equalsIgnoreCase(devSentTo)) {
                helper.setTo(new InternetAddress(userAccount.getUserId(), userAccount.getName()));
            } else {
                helper.setTo(new InternetAddress(devSentTo, emailAddressName));
            }
            log.info("Account validation sent to={}", !StringUtils.isEmpty(devSentTo) ? devSentTo : userAccount.getUserId());
            sendMail(
                    userAccount.getName() + ": " + mailValidateSubject,
                    freemarkerToString("mail/self-signup.ftl", rootMap),
                    message,
                    helper
            );
        } catch (IOException | TemplateException | MessagingException exception) {
            log.error("Validation failure email for={}", userAccount.getUserId(), exception);
            return false;
        }
        return true;
    }

    /**
     * Send recover email to user of provided email id
     * http://bharatonjava.wordpress.com/2012/08/27/sending-email-using-java-mail-api/
     *
     * @param emailId
     */
    public boolean mailRecoverLink(String emailId) {
        UserAccountEntity userAccount = accountService.findByUserId(emailId);
        if(userAccount != null && userAccount.isAccountValidated()) {
            ForgotRecoverEntity forgotRecoverEntity = accountService.initiateAccountRecovery(userAccount.getReceiptUserId());

            Map<String, String> rootMap = new HashMap<>();
            rootMap.put("to", userAccount.getName());
            rootMap.put("link", forgotRecoverEntity.getAuthenticationKey());
            rootMap.put("domain", domain);
            rootMap.put("https", https);

            try {
                MimeMessage message = mailSender.createMimeMessage();

                // use the true flag to indicate you need a multipart message
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(new InternetAddress(doNotReplyEmail, emailAddressName));

                String sentTo = StringUtils.isEmpty(devSentTo) ? emailId : devSentTo;
                if(!sentTo.equalsIgnoreCase(devSentTo)) {
                    helper.setTo(new InternetAddress(emailId, userAccount.getName()));
                } else {
                    helper.setTo(new InternetAddress(devSentTo, emailAddressName));
                }
                log.info("Mail recovery send to : " + (!StringUtils.isEmpty(devSentTo) ? devSentTo : emailId));
                sendMail(
                        userAccount.getName() + ": " + mailRecoverSubject,
                        freemarkerToString("mail/account-recover.ftl", rootMap),
                        message,
                        helper
                );
                return true;
            } catch (IOException | TemplateException | MessagingException exception) {
                log.error("Recovery email={}", exception.getLocalizedMessage(), exception);
                return false;
            }
        }
        //TODO make sure not validate email should not get link to recover password; they should be re-send email
        return false;
    }

    /**
     * Used in sending the invitation for the first time
     *
     * @param invitedUserEmail  Invited users email address
     * @param invitedByRid Existing users email address
     * @return
     */
    public boolean sendInvitation(String invitedUserEmail, String invitedByRid) {
        UserAccountEntity invitedBy = accountService.findByReceiptUserId(invitedByRid);
        if(invitedBy != null) {
            InviteEntity inviteEntity = null;
            try {
                inviteEntity = inviteService.initiateInvite(invitedUserEmail, invitedBy);
                formulateInvitationEmail(invitedUserEmail, invitedBy, inviteEntity);
                return true;
            } catch (RuntimeException exception) {
                if(inviteEntity != null) {
                    deleteInvite(inviteEntity);
                    log.info("Due to failure in sending the invitation email. Deleting Invite={}, for={}", inviteEntity.getId(), inviteEntity.getEmail());
                }
                log.error("Exception occurred during persisting InviteEntity, message={}", exception.getLocalizedMessage(), exception);
            }
        }
        return false;
    }

    /**
     * Helps in re-sending the invitation or to send new invitation to existing (pending) invitation by a new user.
     *
     * @param emailId            Invited users email address
     * @param invitedByRid Existing users email address
     * @return
     */
    public boolean reSendInvitation(String emailId, String invitedByRid) {
        UserAccountEntity invitedBy = accountService.findByReceiptUserId(invitedByRid);
        if(invitedBy != null) {
            try {
                InviteEntity inviteEntity = inviteService.reInviteActiveInvite(emailId, invitedBy);
                boolean isNewInvite = false;
                if(inviteEntity == null) {
                    //Means invite exist by another user. Better to create a new invite for the requesting user
                    inviteEntity = reCreateAnotherInvite(emailId, invitedBy);
                    isNewInvite = true;
                }
                formulateInvitationEmail(emailId, invitedBy, inviteEntity);
                if(!isNewInvite) {
                    inviteManager.save(inviteEntity);
                }
            } catch (Exception exception) {
                log.error("Exception occurred during persisting InviteEntity, reason={}", exception.getLocalizedMessage(), exception);
                return false;
            }
        }
        return true;
    }

    /**
     * Invitation is created by the new user
     *
     * @param email
     * @param invitedBy
     * @return
     */
    public InviteEntity reCreateAnotherInvite(String email, UserAccountEntity invitedBy) {
        InviteEntity inviteEntity = inviteService.find(email);
        try {
            String auth = HashText.computeBCrypt(RandomString.newInstance().nextString());
            inviteEntity = InviteEntity.newInstance(email, auth, inviteEntity.getInvited(), invitedBy);
            inviteManager.save(inviteEntity);
            return inviteEntity;
        } catch (Exception exception) {
            log.error("Error occurred during creation of invited user={}", email, exception.getLocalizedMessage(), exception);
            return null;
        }
    }

    private void formulateInvitationEmail(String email, UserAccountEntity invitedBy, InviteEntity inviteEntity) {
        Map<String, String> rootMap = new HashMap<>();
        rootMap.put("from", invitedBy.getName());
        rootMap.put("fromEmail", invitedBy.getUserId());
        rootMap.put("to", email);
        rootMap.put("link", inviteEntity.getAuthenticationKey());
        rootMap.put("domain", domain);
        rootMap.put("https", https);

        try {
            MimeMessage message = mailSender.createMimeMessage();

            // use the true flag to indicate you need a multipart message
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(new InternetAddress(inviteeEmail, emailAddressName));
            helper.setTo(StringUtils.isEmpty(devSentTo) ? email : devSentTo);
            log.info("Invitation send to={}", (StringUtils.isEmpty(devSentTo) ? email : devSentTo));
            sendMail(
                    mailInviteSubject + " - " + invitedBy.getName(),
                    freemarkerToString("mail/invite.ftl", rootMap),
                    message,
                    helper
            );
        } catch (TemplateException | IOException | MessagingException exception) {
            log.error("Invitation failure email inviteId={}, for={}, exception={}", inviteEntity.getId(), inviteEntity.getEmail(), exception);
            throw new RuntimeException(exception);
        }
    }

    private void sendMail(String subject, String text, MimeMessage message, MimeMessageHelper helper) throws MessagingException {
        // use the true flag to indicate the text included is HTML
        helper.setText(text, true);
        helper.setSubject(subject);

        //Attach image always at the end
        URL url = this.getClass().getClassLoader().getResource("../jsp/images/receipt-o-fi.logo.jpg");
        Assert.notNull(url);
        FileSystemResource res = new FileSystemResource(url.getPath());
        helper.addInline("receiptofi.logo", res);

        try {
            mailSender.send(message);
        } catch(MailSendException mailSendException) {
            log.error("Mail send exception={}", mailSendException.getLocalizedMessage());
            throw new MessagingException(mailSendException.getLocalizedMessage(), mailSendException);
        }
    }

    private String freemarkerToString(String ftl, Map<String, String> rootMap) throws IOException, TemplateException {
        Configuration cfg = freemarkerConfiguration.createConfiguration();
        Template template = cfg.getTemplate(ftl);
        return processTemplateIntoString(template, rootMap);
    }

    /**
     * When invitation fails remove all the reference to the Invitation and the new user
     *
     * @param inviteEntity
     */
    private void deleteInvite(InviteEntity inviteEntity) {
        log.info("Deleting: Profile, Auth, Preferences, Invite as the invitation message failed to sent");
        UserProfileEntity userProfile = accountService.doesUserExists(inviteEntity.getEmail());
        UserAccountEntity userAccount = loginService.findByReceiptUserId(userProfile.getReceiptUserId());
        UserAuthenticationEntity userAuthenticationEntity = userAccount.getUserAuthentication();
        UserPreferenceEntity userPreferenceEntity = accountService.getPreference(userProfile);

        userPreferenceManager.deleteHard(userPreferenceEntity);
        userAuthenticationManager.deleteHard(userAuthenticationEntity);
        userAccountManager.deleteHard(userAccount);
        userProfileManager.deleteHard(userProfile);
        inviteManager.deleteHard(inviteEntity);
    }
}
