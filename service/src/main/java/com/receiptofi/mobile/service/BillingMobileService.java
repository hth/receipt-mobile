package com.receiptofi.mobile.service;

import static com.receiptofi.domain.BillingHistoryEntity.YYYY_MM;

import com.braintreegateway.AddressRequest;
import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.ClientTokenRequest;
import com.braintreegateway.CustomerRequest;
import com.braintreegateway.Environment;
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
import com.receiptofi.domain.types.AccountBillingTypeEnum;
import com.receiptofi.domain.types.BilledStatusEnum;
import com.receiptofi.domain.types.PaymentGatewayEnum;
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

    private BraintreeGateway gateway;
    private BillingAccountManagerMobile billingAccountManager;
    private BillingHistoryManagerMobile billingHistoryManager;

    /** Cache plans. */
    private final Cache<String, List<ReceiptofiPlan>> planCache;
    private final Cache<PaymentGatewayEnum, List<ReceiptofiPlan>> planProviderCache;
    private final Cache<String, ReceiptofiPlan> plansMap;
    private String merchantAccountId;

    @Autowired
    public BillingMobileService(
            @Value ("${braintree.environment}")
            String brainTreeEnvironment,

            @Value ("${braintree.merchant_id}")
            String brainTreeMerchantId,

            @Value ("${braintree.public_key}")
            String brainTreePublicKey,

            @Value ("${braintree.private_key}")
            String brainTreePrivateKey,

            @Value ("${braintree.merchant_account_id}")
            String merchantAccountId,

            @Value ("${plan.cache.minutes}")
            int planCacheMinutes,

            BillingAccountManagerMobile billingAccountManager,
            BillingHistoryManagerMobile billingHistoryManager
    ) {
        if ("PRODUCTION".equals(brainTreeEnvironment)) {
            gateway = new BraintreeGateway(
                    Environment.PRODUCTION,
                    brainTreeMerchantId,
                    brainTreePublicKey,
                    brainTreePrivateKey
            );

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
            gateway = new BraintreeGateway(
                    Environment.SANDBOX,
                    brainTreeMerchantId,
                    brainTreePublicKey,
                    brainTreePrivateKey
            );

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

        this.billingAccountManager = billingAccountManager;
        this.billingHistoryManager = billingHistoryManager;
        this.merchantAccountId = merchantAccountId;
    }

    public void getBilling(String rid, AvailableAccountUpdates availableAccountUpdates) {
        BillingAccountEntity billingAccount = billingAccountManager.getBillingAccount(rid);
        List<BillingHistoryEntity> billings = billingHistoryManager.getHistory(rid);

        availableAccountUpdates.setJsonBilling(new JsonBilling(billingAccount, billings));
    }

    public void getBilling(String rid, Date since, AvailableAccountUpdates availableAccountUpdates) {
        BillingAccountEntity billingAccount = billingAccountManager.getBillingAccount(rid);
        List<BillingHistoryEntity> billings = billingHistoryManager.getHistory(rid, since);

        availableAccountUpdates.setJsonBilling(new JsonBilling(billingAccount, billings));
    }

    /**
     * Get all plans under all payment provider.
     *
     * @return
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
     *
     * @param planId
     * @return
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
     *
     * @return
     */
    private List<ReceiptofiPlan> getAllBraintreePlans(PaymentGatewayEnum paymentGateway) {
        List<ReceiptofiPlan> receiptofiPlans = planProviderCache.getIfPresent(paymentGateway);
        if (receiptofiPlans == null) {
            receiptofiPlans = new ArrayList<>();

            List<Plan> plans = gateway.plan().all();
            for (Plan plan : plans) {
                ReceiptofiPlan receiptofiPlan = new ReceiptofiPlan();
                receiptofiPlan.setId(plan.getId());
                receiptofiPlan.setName(plan.getName());
                receiptofiPlan.setDescription(plan.getDescription());
                receiptofiPlan.setPrice(plan.getPrice());
                receiptofiPlan.setBillingFrequency(plan.getBillingFrequency());
                receiptofiPlan.setBillingDayOfMonth(plan.getBillingDayOfMonth());
                receiptofiPlan.setPaymentGateway(paymentGateway);
                Assert.notNull(receiptofiPlan.getAccountBillingType(), "Undefined plan " + plan.getId());

                receiptofiPlans.add(receiptofiPlan);
                plansMap.put(plan.getId(), receiptofiPlan);
            }
            planProviderCache.put(paymentGateway, receiptofiPlans);
        }
        return receiptofiPlans;
    }

    public Token getBrianTreeClientToken(String rid) {
        BraintreeToken braintreeToken;

        BillingAccountEntity billingAccount = billingAccountManager.getBillingAccount(rid);
        if (billingAccount.getPaymentGateway().isEmpty()) {
            braintreeToken = new BraintreeToken(gateway.clientToken().generate());
        } else {
            PaymentGatewayUser paymentGatewayUser = billingAccount.getPaymentGateway().getLast();
            if (PaymentGatewayEnum.BT == paymentGatewayUser.getPaymentGateway()) {
                braintreeToken = getBraintreeToken(billingAccount, paymentGatewayUser);
            } else {
                LOG.error("Reached unreachable condition ", billingAccount.getPaymentGateway());
                throw new IllegalStateException("Reached unreachable condition for payment gateway");
            }
        }
        return braintreeToken;
    }

    private BraintreeToken getBraintreeToken(BillingAccountEntity billingAccount, PaymentGatewayUser paymentGatewayUser) {
        ClientTokenRequest clientTokenRequest = new ClientTokenRequest().customerId(paymentGatewayUser.getCustomerId());
        /** Token from gateway can be null and should be sent to phone as null. */
        BraintreeToken braintreeToken = new BraintreeToken(gateway.clientToken().generate(clientTokenRequest));
        if (StringUtils.isBlank(braintreeToken.getToken())) {
            LOG.warn("Token not initialized rid={}", billingAccount.getRid());
        }
        braintreeToken.setHasCustomerInfo(true);
        braintreeToken.setFirstName(paymentGatewayUser.getFirstName());
        braintreeToken.setLastName(paymentGatewayUser.getLastName());
        braintreeToken.setPostalCode(paymentGatewayUser.getPostalCode());
        braintreeToken.setPlanId(billingAccount.getAccountBillingType().name());
        return braintreeToken;
    }

    //https://developers.braintreepayments.com/ios+java/reference/general/testing

    /**
     * Create new payment or update payment.
     *
     * @param rid
     * @param planId
     * @param firstName
     * @param lastName
     * @param company
     * @param postal
     * @param paymentMethodNonce
     * @return
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

            if (billingHistory.getBilledStatus() == BilledStatusEnum.B) {
                return updateBilledPayment(
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
            } else {
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
    }

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

        Result<Transaction> result = gateway.transaction().sale(request);
        Transaction transaction = result.getTarget();
        LOG.info("Processor responseCode={} responseText={} authorizationCode={} settlementResponseCode={} settlementResponseText={}",
                transaction.getProcessorResponseCode(),
                transaction.getProcessorResponseText(),
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
            billingAccount.markAccountBilled();
            billingAccountManager.save(billingAccount);
            upsertBillingHistory(rid, receiptofiPlan, transaction, paymentGatewayUser);

            String subscriptionId = subscribe(receiptofiPlan, result.getTarget().getCreditCard().getToken());
            paymentGatewayUser.setSubscriptionId(subscriptionId);
            billingAccount.setAccountBillingType(receiptofiPlan.getAccountBillingType());
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
            String paymentMethodNonce,
            BillingHistoryEntity billingHistory
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

        Result<Transaction> result = gateway.transaction().sale(request);
        Transaction transaction = result.getTarget();
        LOG.info("Processor responseCode={} responseText={} authorizationCode={} settlementResponseCode={} settlementResponseText={}",
                transaction.getProcessorResponseCode(),
                transaction.getProcessorResponseText(),
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
            upsertBillingHistory(rid, receiptofiPlan, transaction, paymentGatewayUser);

            String subscriptionId = subscribe(receiptofiPlan, transaction.getCreditCard().getToken());
            paymentGatewayUser.setSubscriptionId(subscriptionId);
            paymentGatewayUser.setUpdated(new Date());
            billingAccount.setAccountBillingType(receiptofiPlan.getAccountBillingType());
            billingAccountManager.save(billingAccount);
        }

        LOG.debug("at exit {}", transactionDetail);
        return transactionDetail;
    }

    /**
     * Void or Refund existing transaction and then cancels subscription before signing up with new plan.
     *
     * @param rid
     * @param planId
     * @param firstName
     * @param lastName
     * @param company
     * @param postal
     * @param receiptofiPlan
     * @param billingAccount
     * @param paymentMethodNonce
     * @param billingHistory
     * @return
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

            boolean vorSuccess = voidOrRefund(billingHistory, rid);
            TransactionDetail subscriptionCancelDetail = cancelSubscription(rid, billingAccount, paymentGatewayUser);
            if (subscriptionCancelDetail.isSuccess()) {
                LOG.info("success cancelled subscriptionId={} rid={}", paymentGatewayUser.getSubscriptionId(), rid);
                transactionDetail = updateBilledPayment(
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
            } else {
                if (vorSuccess) {
                    message = "Payment refunded. " + subscriptionCancelDetail.getMessage();
                    LOG.warn("refund success message={}", message);
                } else {
                    LOG.error("Failed to refund payment and cancel subscription transactionId={} rid={}", billingHistory.getTransactionId(), rid);
                    message = "Failed to refund payment and cancel subscription.";
                    LOG.warn("message={}", message);
                }
            }
        } else {
            LOG.error("Either transactionId or subscriptionId is empty transactionId={} subscriptionId={} rid={}",
                    billingHistory.getTransactionId(), paymentGatewayUser.getSubscriptionId(), rid);

            message = "We failed to understand the request.";
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
     * Create new or update existing billing history.
     *
     * @param rid
     * @param receiptofiPlan
     * @param transaction
     * @param paymentGatewayUser
     */
    private void upsertBillingHistory(String rid, ReceiptofiPlan receiptofiPlan, Transaction transaction, PaymentGatewayUser paymentGatewayUser) {
        BillingHistoryEntity billingHistory = billingHistoryManager.getHistory(rid, YYYY_MM.format(new Date()));
        if (null == billingHistory || BilledStatusEnum.B == billingHistory.getBilledStatus()) {
            billingHistory = createBillingHistory(
                    rid,
                    receiptofiPlan,
                    paymentGatewayUser,
                    transaction.getId());
        } else {
            /** Update BillingHistory when bill status is either BilledStatusEnum.NB or BilledStatusEnum.P. */
            updateBillingHistory(receiptofiPlan, paymentGatewayUser, transaction.getId(), billingHistory);
        }
        billingHistoryManager.save(billingHistory);
    }

    /**
     * Subscribe the plan.
     *
     * @param receiptofiPlan
     * @param token
     * @return
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
        Result<Subscription> subscriptionResult = gateway.subscription().create(subscriptionRequest);
        if (subscriptionResult.isSuccess()) {
            LOG.info("Added to subscription");
            subscriptionId = subscriptionResult.getTarget().getId();
        }
        return subscriptionId;
    }

    /**
     * Cancels all subscription.
     *
     * @param rid
     * @return
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
     *
     * @param rid
     * @return
     */
    public TransactionDetail cancelLastSubscription(String rid) {
        BillingAccountEntity billingAccount = billingAccountManager.getBillingAccount(rid);
        PaymentGatewayUser paymentGatewayUser = billingAccount.getPaymentGateway().getLast();
        return cancelSubscription(rid, billingAccount, paymentGatewayUser);
    }

    /**
     * Cancels an existing subscription.
     *
     * @param rid
     * @param billingAccount
     * @param paymentGatewayUser
     * @return
     */
    private TransactionDetail cancelSubscription(
            String rid,
            BillingAccountEntity billingAccount,
            PaymentGatewayUser paymentGatewayUser
    ) {
        TransactionDetail transactionDetail;
        if (StringUtils.isNotBlank(paymentGatewayUser.getSubscriptionId())) {
            try {
                Result<Subscription> result = gateway.subscription().cancel(paymentGatewayUser.getSubscriptionId());
                Subscription subscription = result.getTarget();
                if (result.isSuccess() &&
                        StringUtils.isNotBlank(subscription.getId()) &&
                        paymentGatewayUser.getSubscriptionId().equals(subscription.getId())) {

                    paymentGatewayUser.setSubscriptionId("");
                    paymentGatewayUser.setUpdated(new Date());
                    billingAccount.setAccountBillingType(AccountBillingTypeEnum.NB);
                    billingAccountManager.save(billingAccount);
                    LOG.info("Success canceled subscription subscriptionId={} rid={} status={} resultId={}",
                            paymentGatewayUser.getSubscriptionId(), rid, subscription.getStatus(), result.getTarget().getId());

                    transactionDetail = new TransactionDetailSubscription(
                            result.isSuccess(),
                            subscription.getStatus().name(),
                            subscription.getPlanId(),
                            paymentGatewayUser.getFirstName(),
                            paymentGatewayUser.getLastName(),
                            paymentGatewayUser.getPostalCode(),
                            billingAccount.getAccountBillingType().getName(),
                            subscription.getId()
                    );
                } else {
                    LOG.warn("Failed to cancel rid={} status={}", rid, result.getMessage());

                    transactionDetail = new TransactionDetailSubscription(
                            paymentGatewayUser.getFirstName(),
                            paymentGatewayUser.getLastName(),
                            paymentGatewayUser.getPostalCode(),
                            billingAccount.getAccountBillingType().getName(),
                            result.getMessage()
                    );
                }
            } catch (NotFoundException e) {
                LOG.error("Failed when un-subscribing reason={}", e.getLocalizedMessage(), e);

                transactionDetail = new TransactionDetailSubscription(
                        paymentGatewayUser.getFirstName(),
                        paymentGatewayUser.getLastName(),
                        paymentGatewayUser.getPostalCode(),
                        billingAccount.getAccountBillingType().getName(),
                        "Could not find users subscription."
                );
            }
        } else {
            LOG.error("Subscription not found subscriptionId={} rid={}", paymentGatewayUser.getSubscriptionId(), rid);
            transactionDetail = new TransactionDetailSubscription(
                    paymentGatewayUser.getFirstName(),
                    paymentGatewayUser.getLastName(),
                    paymentGatewayUser.getPostalCode(),
                    billingAccount.getAccountBillingType().getName(),
                    "Subscription could not be found."
            );
        }

        LOG.debug("at exit {}", transactionDetail);
        return transactionDetail;
    }

    private boolean voidOrRefund(BillingHistoryEntity billingHistory, String rid) {
        try {
            Result<Transaction> result = gateway.transaction().voidTransaction(billingHistory.getTransactionId());
            if (result.isSuccess()) {
                LOG.info("void success transactionId={} rid={} resultId={}", billingHistory.getTransactionId(), rid, result.getTarget().getId());
                billingHistory.setTransactionId("");
                billingHistory.setUpdated();
                billingHistoryManager.save(billingHistory);
                return result.isSuccess();
            } else {
                LOG.warn("void failed transactionId={} rid={} reason={}, trying refund", billingHistory.getTransactionId(), rid, result.getMessage());
                result = gateway.transaction().refund(billingHistory.getTransactionId());
                if (result.isSuccess()) {
                    LOG.info("refund success transactionId={} rid={}", billingHistory.getTransactionId(), rid);
                    billingHistory.setTransactionId("");
                    billingHistory.setUpdated();
                    billingHistoryManager.save(billingHistory);
                } else {
                    LOG.warn("refund failed transactionId={} rid={} reason={}", billingHistory.getTransactionId(), rid, result.getMessage());
                }
                return result.isSuccess();
            }
        } catch (NotFoundException e) {
            LOG.error("Failed when voidOrRefunding reason={}", e.getLocalizedMessage(), e);
            return false;
        }
    }

    /**
     * Update BillingHistory when bill status is either BilledStatusEnum.NB or BilledStatusEnum.P.
     *
     * @param receiptofiPlan
     * @param paymentGatewayUser
     * @param transactionId
     * @param billingHistory
     */
    private void updateBillingHistory(
            ReceiptofiPlan receiptofiPlan,
            PaymentGatewayUser paymentGatewayUser,
            String transactionId,
            BillingHistoryEntity billingHistory
    ) {
        billingHistory.setBilledStatus(BilledStatusEnum.S);
        billingHistory.setAccountBillingType(receiptofiPlan.getAccountBillingType());
        billingHistory.setPaymentGateway(paymentGatewayUser.getPaymentGateway());
        billingHistory.setTransactionId(transactionId);
        billingHistory.setUpdated();
    }

    /**
     * Create new when BillingHistory does not exists or previously billed status is BilledStatusEnum.B.
     *
     * @param rid
     * @param receiptofiPlan
     * @param paymentGatewayUser
     * @param transactionId
     * @return
     */
    private BillingHistoryEntity createBillingHistory(
            String rid,
            ReceiptofiPlan receiptofiPlan,
            PaymentGatewayUser paymentGatewayUser,
            String transactionId
    ) {
        BillingHistoryEntity billingHistory = new BillingHistoryEntity(rid, new Date());
        billingHistory.setBilledStatus(BilledStatusEnum.S);
        billingHistory.setAccountBillingType(receiptofiPlan.getAccountBillingType());
        billingHistory.setPaymentGateway(paymentGatewayUser.getPaymentGateway());
        billingHistory.setTransactionId(transactionId);
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
            gateway.customer().update(paymentGatewayUser.getCustomerId(), customerRequest);
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
            gateway.address().update(paymentGatewayUser.getCustomerId(), paymentGatewayUser.getAddressId(), addressRequest);
        }
    }
}
