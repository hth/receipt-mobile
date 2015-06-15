package com.receiptofi.mobile.service;

import static com.receiptofi.domain.BillingHistoryEntity.YYYY_MM;

import com.braintreegateway.AddressRequest;
import com.braintreegateway.ClientTokenRequest;
import com.braintreegateway.CustomerRequest;
import com.braintreegateway.Plan;
import com.braintreegateway.Result;
import com.braintreegateway.Subscription;
import com.braintreegateway.SubscriptionRequest;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;
import com.braintreegateway.exceptions.NotFoundException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import com.receiptofi.domain.BillingAccountEntity;
import com.receiptofi.domain.BillingHistoryEntity;
import com.receiptofi.domain.json.JsonBilling;
import com.receiptofi.domain.types.BillingPlanEnum;
import com.receiptofi.domain.types.BilledStatusEnum;
import com.receiptofi.domain.types.PaymentGatewayEnum;
import com.receiptofi.domain.types.TransactionStatusEnum;
import com.receiptofi.domain.value.PaymentGatewayUser;
import com.receiptofi.mobile.domain.AvailableAccountUpdates;
import com.receiptofi.mobile.domain.BraintreeToken;
import com.receiptofi.mobile.domain.ReceiptofiPlan;
import com.receiptofi.mobile.domain.Token;
import com.receiptofi.mobile.domain.TransactionDetail;
import com.receiptofi.mobile.domain.TransactionDetailPayment;
import com.receiptofi.mobile.domain.TransactionDetailSubscription;
import com.receiptofi.mobile.repository.BillingAccountManagerMobile;
import com.receiptofi.mobile.repository.BillingHistoryManagerMobile;
import com.receiptofi.service.BillingService;
import com.receiptofi.service.PaymentGatewayService;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * User: hitender
 * Date: 4/19/15 3:58 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@Component
public class BillingMobileService {
    private static final Logger LOG = LoggerFactory.getLogger(BillingMobileService.class);
    private static final String PLANS = "PLANS";
    private static final int SIZE_20 = 20;
    private static final int SIZE_1 = 1;

    private BillingAccountManagerMobile billingAccountManager;
    private BillingHistoryManagerMobile billingHistoryManager;
    private BillingService billingService;
    private PaymentGatewayService paymentGatewayService;

    /** Cache plans. */
    private final Cache<String, List<ReceiptofiPlan>> planCache;
    private final Cache<PaymentGatewayEnum, List<ReceiptofiPlan>> planProviderCache;
    private final Cache<String, ReceiptofiPlan> plansMap;
    private String merchantAccountId;

    @Autowired
    public BillingMobileService(
            @Value ("${braintree.environment}")
            String brainTreeEnvironment,

            @Value ("${braintree.merchant_account_id}")
            String merchantAccountId,

            @Value ("${plan.cache.minutes}")
            int planCacheMinutes,

            BillingAccountManagerMobile billingAccountManager,
            BillingHistoryManagerMobile billingHistoryManager,
            BillingService billingService,
            PaymentGatewayService paymentGatewayService
    ) {
        if ("PRODUCTION".equals(brainTreeEnvironment)) {
            planCache = CacheBuilder.newBuilder()
                    .maximumSize(SIZE_1)
                    .expireAfterWrite(planCacheMinutes, TimeUnit.MINUTES)
                    .build();

            planProviderCache = CacheBuilder.newBuilder()
                    .maximumSize(SIZE_20)
                    .expireAfterWrite(planCacheMinutes, TimeUnit.MINUTES)
                    .build();

            plansMap = CacheBuilder.newBuilder()
                    .maximumSize(SIZE_20)
                    .expireAfterWrite(planCacheMinutes, TimeUnit.MINUTES)
                    .build();
        } else {
            planCache = CacheBuilder.newBuilder()
                    .maximumSize(SIZE_1)
                    .expireAfterWrite(planCacheMinutes, TimeUnit.MINUTES)
                    .build();

            planProviderCache = CacheBuilder.newBuilder()
                    .maximumSize(SIZE_20)
                    .expireAfterWrite(planCacheMinutes, TimeUnit.MINUTES)
                    .build();

            plansMap = CacheBuilder.newBuilder()
                    .maximumSize(SIZE_20)
                    .expireAfterWrite(planCacheMinutes, TimeUnit.MINUTES)
                    .build();
        }

        this.merchantAccountId = merchantAccountId;
        this.billingAccountManager = billingAccountManager;
        this.billingHistoryManager = billingHistoryManager;
        this.billingService = billingService;
        this.paymentGatewayService = paymentGatewayService;
    }

    /**
     * All billing history.
     */
    public void getBilling(String rid, AvailableAccountUpdates availableAccountUpdates) {
        BillingAccountEntity billingAccount = billingAccountManager.getBillingAccount(rid);
        List<BillingHistoryEntity> billings = billingHistoryManager.getHistory(rid);

        availableAccountUpdates.setJsonBilling(new JsonBilling(billingAccount, billings));
    }

    /**
     * Billing history since last checked time.
     */
    public void getBilling(String rid, Date since, AvailableAccountUpdates availableAccountUpdates) {
        BillingAccountEntity billingAccount = billingAccountManager.getBillingAccount(rid);
        List<BillingHistoryEntity> billings = billingHistoryManager.getHistory(rid, since);

        availableAccountUpdates.setJsonBilling(new JsonBilling(billingAccount, billings));
    }

    /**
     * Get all plans under all payment provider.
     */
    public List<ReceiptofiPlan> getAllPlans() {
        List<ReceiptofiPlan> receiptofiPlans = planCache.getIfPresent(PLANS);
        if (null == receiptofiPlans) {
            receiptofiPlans = new ArrayList<>();

            for (PaymentGatewayEnum billingProvider : PaymentGatewayEnum.values()) {
                if (PaymentGatewayEnum.BT == billingProvider) {
                    receiptofiPlans.addAll(getAllBraintreePlans(billingProvider));
                } else {
                    LOG.error("Reached unreachable condition for Billing Provider");
                    throw new IllegalStateException("Reached unreachable condition for Billing Provider");
                }
            }

            planCache.put(PLANS, receiptofiPlans);
        }
        return receiptofiPlans;
    }

    /**
     * Get plan from planId.
     */
    private ReceiptofiPlan getPlan(String planId) {
        ReceiptofiPlan receiptofiPlan = plansMap.getIfPresent(planId);
        if (receiptofiPlan == null) {
            getAllPlans();
            receiptofiPlan = plansMap.getIfPresent(planId);
        }

        LOG.info("planId={} receiptPlan={}", planId, receiptofiPlan.getId());
        return receiptofiPlan;
    }

    /**
     * Get from cache or reload from provider all available plans under Braintree.
     */
    private List<ReceiptofiPlan> getAllBraintreePlans(PaymentGatewayEnum paymentGateway) {
        List<ReceiptofiPlan> receiptofiPlans = planProviderCache.getIfPresent(paymentGateway);
        if (receiptofiPlans == null) {
            receiptofiPlans = new ArrayList<>();

            List<Plan> plans = paymentGatewayService.getGateway().plan().all();
            for (Plan plan : plans) {
                ReceiptofiPlan receiptofiPlan = new ReceiptofiPlan();
                receiptofiPlan.setId(plan.getId());
                receiptofiPlan.setName(plan.getName());
                receiptofiPlan.setDescription(plan.getDescription());
                receiptofiPlan.setPrice(plan.getPrice());
                receiptofiPlan.setBillingFrequency(plan.getBillingFrequency());
                receiptofiPlan.setBillingDayOfMonth(plan.getBillingDayOfMonth());
                receiptofiPlan.setPaymentGateway(paymentGateway);
                Assert.notNull(receiptofiPlan.getBillingPlan(), "Undefined plan " + plan.getId());

                receiptofiPlans.add(receiptofiPlan);
                plansMap.put(plan.getId(), receiptofiPlan);
            }
            planProviderCache.put(paymentGateway, receiptofiPlans);
        }
        return receiptofiPlans;
    }

    /**
     * Get payment gateway token to initialize SDK.
     */
    public Token getBrianTreeClientToken(String rid) {
        BraintreeToken braintreeToken;

        BillingAccountEntity billingAccount = billingAccountManager.getBillingAccount(rid);
        if (billingAccount.getPaymentGateway().isEmpty()) {
            braintreeToken = new BraintreeToken(paymentGatewayService.getGateway().clientToken().generate());
        } else {
            PaymentGatewayUser paymentGatewayUser = billingAccount.getPaymentGateway().getLast();
            if (PaymentGatewayEnum.BT == paymentGatewayUser.getPaymentGateway()) {
                braintreeToken = getBraintreeToken(billingAccount, paymentGatewayUser);
            } else {
                LOG.error("Reached unreachable condition {}", billingAccount.getPaymentGateway());
                throw new IllegalStateException("Reached unreachable condition for payment gateway");
            }
        }
        return braintreeToken;
    }

    /**
     * Populate token with user information.
     */
    private BraintreeToken getBraintreeToken(BillingAccountEntity billingAccount, PaymentGatewayUser paymentGatewayUser) {
        ClientTokenRequest clientTokenRequest = new ClientTokenRequest().customerId(paymentGatewayUser.getCustomerId());
        /** Token from gateway can be null and should be sent to phone as null. */
        BraintreeToken braintreeToken = new BraintreeToken(paymentGatewayService.getGateway().clientToken().generate(clientTokenRequest));
        if (StringUtils.isBlank(braintreeToken.getToken())) {
            LOG.warn("Token not initialized rid={}", billingAccount.getRid());
        }
        braintreeToken.setHasCustomerInfo(true);
        braintreeToken.setFirstName(paymentGatewayUser.getFirstName());
        braintreeToken.setLastName(paymentGatewayUser.getLastName());
        braintreeToken.setPostalCode(paymentGatewayUser.getPostalCode());
        braintreeToken.setPlanId(billingAccount.getBillingPlan().name());
        return braintreeToken;
    }

    //https://developers.braintreepayments.com/ios+java/reference/general/testing

    /**
     * Create new payment or update payment.
     */
    public TransactionDetail payment(
            String rid,
            String planId,
            String firstName,
            String lastName,
            String company,
            String postal,
            String paymentMethodNonce
    ) {
        BillingAccountEntity billingAccount = billingAccountManager.getBillingAccount(rid);
        ReceiptofiPlan receiptofiPlan = getPlan(planId);
        Assert.notNull(receiptofiPlan, "Could not find a plan for id=" + planId);
        if (billingAccount.getPaymentGateway().isEmpty()) {
            /** Means customer does not exists on payment gateway. */
            return newPayment(
                    rid,
                    planId,
                    firstName,
                    lastName,
                    company,
                    postal,
                    receiptofiPlan,
                    billingAccount,
                    paymentMethodNonce);
        } else {
            BillingHistoryEntity billingHistory = billingHistoryManager.getHistory(rid, YYYY_MM.format(new Date()));
            return cancelAndCreateNewPayment(
                    rid,
                    planId,
                    firstName,
                    lastName,
                    company,
                    postal,
                    receiptofiPlan,
                    billingAccount,
                    paymentMethodNonce,
                    billingHistory);
        }
    }

    /**
     * Create customer. Charge the card and add to subscription.
     */
    private TransactionDetail newPayment(
            String rid,
            String planId,
            String firstName,
            String lastName,
            String company,
            String postal,
            ReceiptofiPlan receiptofiPlan,
            BillingAccountEntity billingAccount,
            String paymentMethodNonce
    ) {
        TransactionRequest request = new TransactionRequest();
        request.merchantAccountId(merchantAccountId);
        request.customer()
                .firstName(firstName)
                .lastName(lastName)
                .company(company);
        request.billingAddress()
                .firstName(firstName)
                .lastName(lastName)
                .postalCode(postal);
        request.amount(receiptofiPlan.getPrice())
                .paymentMethodNonce(paymentMethodNonce)
                .options()
                .submitForSettlement(true)
                .storeInVaultOnSuccess(true)
                .addBillingAddressToPaymentMethod(true)
                .done();
        request.recurring(true);

        Result<Transaction> result = paymentGatewayService.getGateway().transaction().sale(request);
        Transaction transaction = result.getTarget();
        LOG.info("Processor authorizationCode={} settlementResponseCode={} settlementResponseText={}",
                transaction.getProcessorAuthorizationCode(),
                transaction.getProcessorSettlementResponseCode(),
                transaction.getProcessorSettlementResponseText());

        TransactionDetail transactionDetail = new TransactionDetailPayment(
                result.isSuccess(),
                transaction.getStatus().name(),
                firstName,
                lastName,
                postal,
                planId,
                transaction.getId()
        );

        if (result.isSuccess()) {
            LOG.info("Paid for rid={} plan={} customerId={}",
                    rid, receiptofiPlan.getId(), transaction.getCustomer().getId());

            PaymentGatewayUser paymentGatewayUser = new PaymentGatewayUser(
                    PaymentGatewayEnum.BT,
                    transaction.getCustomer().getId(),
                    firstName,
                    lastName,
                    company,
                    transaction.getBillingAddress().getId(),
                    postal);

            billingAccount.addPaymentGateway(paymentGatewayUser);
            billingAccountManager.save(billingAccount);
            BillingHistoryEntity billingHistory = createBillingHistory(rid, receiptofiPlan, paymentGatewayUser, transaction.getId());
            billingHistoryManager.save(billingHistory);

            String subscriptionId = subscribe(receiptofiPlan, result.getTarget().getCreditCard().getToken());
            paymentGatewayUser.setSubscriptionId(subscriptionId);
            billingAccount.setBillingPlan(receiptofiPlan.getBillingPlan());
            billingAccountManager.save(billingAccount);
        }

        LOG.debug("at exit {}", transactionDetail);
        return transactionDetail;
    }

    private TransactionDetail updateBilledPayment(
            String rid,
            String planId,
            String firstName,
            String lastName,
            String company,
            String postal,
            ReceiptofiPlan receiptofiPlan,
            BillingAccountEntity billingAccount,
            String paymentMethodNonce
    ) {
        PaymentGatewayUser paymentGatewayUser = billingAccount.getPaymentGateway().getLast();

        updateCustomer(firstName, lastName, company, paymentGatewayUser, billingAccount);
        updateBillingAddress(postal, paymentGatewayUser, billingAccount);

        TransactionRequest request = new TransactionRequest();
        request.customerId(paymentGatewayUser.getCustomerId());
        request.amount(receiptofiPlan.getPrice())
                .paymentMethodNonce(paymentMethodNonce)
                .options()
                .submitForSettlement(true)
                .done();

        Result<Transaction> result = paymentGatewayService.getGateway().transaction().sale(request);
        Transaction transaction = result.getTarget();
        LOG.info("Processor authorizationCode={} settlementResponseCode={} settlementResponseText={}",
                transaction.getProcessorAuthorizationCode(),
                transaction.getProcessorSettlementResponseCode(),
                transaction.getProcessorSettlementResponseText());

        TransactionDetail transactionDetail = new TransactionDetailPayment(
                result.isSuccess(),
                transaction.getStatus().name(),
                firstName,
                lastName,
                postal,
                planId,
                transaction.getId()
        );

        if (result.isSuccess()) {
            LOG.info("Paid for rid={} plan={} customerId={}", rid, receiptofiPlan.getId(), transaction.getCustomer().getId());

            BillingHistoryEntity billingHistory = createBillingHistory(rid, receiptofiPlan, paymentGatewayUser, transaction.getId());
            billingHistoryManager.save(billingHistory);

            String subscriptionId = subscribe(receiptofiPlan, transaction.getCreditCard().getToken());
            paymentGatewayUser.setSubscriptionId(subscriptionId);
            paymentGatewayUser.setUpdated(new Date());
            billingAccount.setBillingPlan(receiptofiPlan.getBillingPlan());
            billingAccountManager.save(billingAccount);
        }

        LOG.debug("at exit {}", transactionDetail);
        return transactionDetail;
    }

    /**
     * Void or Refund existing transaction and then cancels subscription before signing up with new plan.
     */
    private TransactionDetail cancelAndCreateNewPayment(
            String rid,
            String planId,
            String firstName,
            String lastName,
            String company,
            String postal,
            ReceiptofiPlan receiptofiPlan,
            BillingAccountEntity billingAccount,
            String paymentMethodNonce,
            BillingHistoryEntity billingHistory
    ) {
        TransactionDetail transactionDetail = null;
        String message = null;
        PaymentGatewayUser paymentGatewayUser = billingAccount.getPaymentGateway().getLast();

        /** Highly unlikely to happen that refund will fail. Hence condition of 'OR' to complete un-subscribe. */
        if (StringUtils.isNotBlank(billingHistory.getTransactionId()) ||
                StringUtils.isNotBlank(paymentGatewayUser.getSubscriptionId())) {

            TransactionStatusEnum transactionStatus = billingService.voidTransaction(billingHistory);
            if (null != transactionStatus) {
                /** Should only support Void and Refund transaction status. */
                switch (transactionStatus) {
                    case R:
                        break;
                    case V:
                        break;
                    default:
                        LOG.error("Unknown transactionStatus={}", transactionStatus.getName());
                }

                billingHistory.setBilledStatus(BilledStatusEnum.R);
                billingHistory.setTransactionStatus(transactionStatus);
                billingHistoryManager.save(billingHistory);
            }

            if (StringUtils.isBlank(paymentGatewayUser.getSubscriptionId())) {
                LOG.info("User not subscribed to any plan. Hence billing and subscribing to a plan");
                transactionDetail = updateBilledPayment(
                        rid,
                        planId,
                        firstName,
                        lastName,
                        company,
                        postal,
                        receiptofiPlan,
                        billingAccount,
                        paymentMethodNonce);
            } else {
                TransactionDetail subscriptionCancelDetail = cancelSubscription(rid, billingAccount, paymentGatewayUser);
                if (subscriptionCancelDetail.isSuccess()) {
                    LOG.info("success cancelled subscriptionId={} rid={} and now re-subscribing to newer plan",
                            paymentGatewayUser.getSubscriptionId(), rid);

                    transactionDetail = updateBilledPayment(
                            rid,
                            planId,
                            firstName,
                            lastName,
                            company,
                            postal,
                            receiptofiPlan,
                            billingAccount,
                            paymentMethodNonce);
                } else {
                    if (null != transactionStatus) {
                        LOG.warn("refund success message={}", message);
                        message = "Payment refunded. " + subscriptionCancelDetail.getMessage();
                    } else {
                        LOG.error("Failed to refund payment and cancel subscription transactionId={} rid={}", billingHistory.getTransactionId(), rid);
                        message = "Failed to refund payment and cancel subscription.";
                    }
                }
            }
        } else {
            LOG.info("Subscribing to new plan since transactionId or subscriptionId is empty transactionId={} subscriptionId={} rid={}",
                    billingHistory.getTransactionId(), paymentGatewayUser.getSubscriptionId(), rid);

            transactionDetail = updateBilledPayment(
                    rid,
                    planId,
                    firstName,
                    lastName,
                    company,
                    postal,
                    receiptofiPlan,
                    billingAccount,
                    paymentMethodNonce);
        }

        if (null == transactionDetail) {
            Assert.hasLength(message, "Message for TransactionDetailPayment is null");
            transactionDetail = new TransactionDetailPayment(
                    firstName,
                    lastName,
                    postal,
                    planId,
                    message
            );
        }

        LOG.debug("at exit {}", transactionDetail);
        return transactionDetail;
    }

    /**
     * Subscribe the plan.
     */
    private String subscribe(
            ReceiptofiPlan receiptofiPlan,
            String token
    ) {
        String subscriptionId = null;
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
        subscriptionRequest
                .paymentMethodToken(token)
                .planId(receiptofiPlan.getId());
        Result<Subscription> subscriptionResult = paymentGatewayService.getGateway().subscription().create(subscriptionRequest);
        if (subscriptionResult.isSuccess()) {
            LOG.info("Added to subscriptionId={}", subscriptionResult.getTarget().getId());
            subscriptionId = subscriptionResult.getTarget().getId();
        }
        return subscriptionId;
    }

    /**
     * Cancels all subscription.
     */
    public List<TransactionDetail> cancelAllSubscription(String rid) {
        int count = 0, success = 0, failure = 0;
        List<TransactionDetail> transactionDetails = new LinkedList<>();
        BillingAccountEntity billingAccount = billingAccountManager.getBillingAccount(rid);
        for (PaymentGatewayUser paymentGatewayUser : billingAccount.getPaymentGateway()) {
            count++;
            TransactionDetail transactionDetail = cancelSubscription(rid, billingAccount, paymentGatewayUser);
            if (transactionDetail.isSuccess()) {
                success++;
            } else {
                failure++;
            }
            transactionDetails.add(transactionDetail);
        }
        LOG.info("Cancelled subscriptions count={} success={} failure={}", count, success, failure);

        return transactionDetails;
    }

    /**
     * Cancels last subscription.
     */
    public TransactionDetail cancelLastSubscription(String rid) {
        BillingAccountEntity billingAccount = billingAccountManager.getBillingAccount(rid);
        PaymentGatewayUser paymentGatewayUser = billingAccount.getPaymentGateway().getLast();
        return cancelSubscription(rid, billingAccount, paymentGatewayUser);
    }

    /**
     * Cancels an existing subscription.
     */
    private TransactionDetail cancelSubscription(
            String rid,
            BillingAccountEntity billingAccount,
            PaymentGatewayUser paymentGatewayUser
    ) {
        TransactionDetail transactionDetail = null;
        String message = null;

        if (StringUtils.isNotBlank(paymentGatewayUser.getSubscriptionId())) {
            try {
                Result<Subscription> result = paymentGatewayService.getGateway().subscription().cancel(paymentGatewayUser.getSubscriptionId());
                Subscription subscription = result.getTarget();
                if (result.isSuccess() &&
                        StringUtils.isNotBlank(subscription.getId()) &&
                        paymentGatewayUser.getSubscriptionId().equals(subscription.getId())) {

                    LOG.info("Success canceled subscription subscriptionId={} rid={} status={} resultId={}",
                            paymentGatewayUser.getSubscriptionId(), rid, subscription.getStatus(), result.getTarget().getId());

                    /** Payment Gateway is updated with BillingAccount. */
                    paymentGatewayUser.setSubscriptionId("");
                    paymentGatewayUser.setUpdated(new Date());
                    billingAccount.setBillingPlan(BillingPlanEnum.NB);
                    billingAccountManager.save(billingAccount);

                    transactionDetail = new TransactionDetailSubscription(
                            result.isSuccess(),
                            subscription.getStatus().name(),
                            subscription.getPlanId(),
                            paymentGatewayUser.getFirstName(),
                            paymentGatewayUser.getLastName(),
                            paymentGatewayUser.getPostalCode(),
                            billingAccount.getBillingPlan().getName(),
                            subscription.getId()
                    );
                } else {
                    LOG.warn("Failed to cancel rid={} status={}", rid, result.getMessage());
                    message = result.getMessage();
                }
            } catch (NotFoundException e) {
                LOG.error("Failed when un-subscribing reason={}", e.getLocalizedMessage(), e);
                message = "Could not find users subscription.";
            }
        } else {
            LOG.error("Subscription not found subscriptionId={} rid={}", paymentGatewayUser.getSubscriptionId(), rid);
            message = "Subscription could not be found.";
        }

        if (null == transactionDetail) {
            Assert.hasLength(message, "Message for TransactionDetailPayment is null");
            transactionDetail = new TransactionDetailSubscription(
                    paymentGatewayUser.getFirstName(),
                    paymentGatewayUser.getLastName(),
                    paymentGatewayUser.getPostalCode(),
                    billingAccount.getBillingPlan().getName(),
                    message
            );
        }

        LOG.debug("at exit {}", transactionDetail);
        return transactionDetail;
    }

    /**
     * Update BillingHistory when bill status is either BilledStatusEnum.NB or BilledStatusEnum.P.
     */
    private void updateBillingHistory(
            ReceiptofiPlan receiptofiPlan,
            PaymentGatewayUser paymentGatewayUser,
            String transactionId,
            BillingHistoryEntity billingHistory
    ) {
        billingHistory.setBilledStatus(BilledStatusEnum.B);
        billingHistory.setBillingPlan(receiptofiPlan.getBillingPlan());
        billingHistory.setPaymentGateway(paymentGatewayUser.getPaymentGateway());
        billingHistory.setTransactionId(transactionId);
        billingHistory.setTransactionStatus(TransactionStatusEnum.S);
    }

    /**
     * Create new when BillingHistory for a new transaction Id.
     */
    private BillingHistoryEntity createBillingHistory(
            String rid,
            ReceiptofiPlan receiptofiPlan,
            PaymentGatewayUser paymentGatewayUser,
            String transactionId
    ) {
        BillingHistoryEntity billingHistory = new BillingHistoryEntity(rid, new Date());
        billingHistory.setBilledStatus(BilledStatusEnum.B);
        billingHistory.setBillingPlan(receiptofiPlan.getBillingPlan());
        billingHistory.setPaymentGateway(paymentGatewayUser.getPaymentGateway());
        billingHistory.setTransactionId(transactionId);
        billingHistory.setTransactionStatus(TransactionStatusEnum.S);
        return billingHistory;
    }

    private void updateCustomer(
            String firstName,
            String lastName,
            String company,
            PaymentGatewayUser paymentGatewayUser,
            BillingAccountEntity billingAccount
    ) {
        boolean modified = false;

        if (!firstName.equals(paymentGatewayUser.getFirstName())) {
            paymentGatewayUser.setFirstName(firstName);
            modified = true;
        }
        if (!lastName.equals(paymentGatewayUser.getLastName())) {
            paymentGatewayUser.setLastName(lastName);
            modified = true;
        }

        if (null != company && null != paymentGatewayUser.getCompany() && !company.equals(paymentGatewayUser.getCompany())) {
            paymentGatewayUser.setCompany(company);
            modified = true;
        }

        if (modified) {
            billingAccountManager.save(billingAccount);
            CustomerRequest customerRequest = new CustomerRequest();
            customerRequest
                    .firstName(firstName)
                    .lastName(lastName)
                    .company(company);
            paymentGatewayService.getGateway().customer().update(paymentGatewayUser.getCustomerId(), customerRequest);
        }
    }

    private void updateBillingAddress(
            String postal,
            PaymentGatewayUser paymentGatewayUser,
            BillingAccountEntity billingAccount
    ) {
        boolean modified = false;

        if (!postal.equals(paymentGatewayUser.getPostalCode())) {
            paymentGatewayUser.setPostalCode(postal);
            modified = true;
        }

        if (modified) {
            billingAccountManager.save(billingAccount);
            AddressRequest addressRequest = new AddressRequest();
            addressRequest
                    .postalCode(postal);
            paymentGatewayService.getGateway().address().update(paymentGatewayUser.getCustomerId(), paymentGatewayUser.getAddressId(), addressRequest);
        }
    }
}
