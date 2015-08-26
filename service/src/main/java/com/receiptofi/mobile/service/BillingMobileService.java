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
import com.receiptofi.domain.types.BilledStatusEnum;
import com.receiptofi.domain.types.BillingPlanEnum;
import com.receiptofi.domain.types.PaymentGatewayEnum;
import com.receiptofi.domain.types.TransactionStatusEnum;
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

        Assert.hasLength(merchantAccountId, "Merchant account id is empty");
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
        BillingAccountEntity billingAccount = billingAccountManager.getLatestBillingAccount(rid);
        List<BillingHistoryEntity> billings = billingHistoryManager.getHistory(rid);

        availableAccountUpdates.setJsonBilling(new JsonBilling(billingAccount, billings));
    }

    /**
     * Billing history since last checked time.
     */
    public void getBilling(String rid, Date since, AvailableAccountUpdates availableAccountUpdates) {
        BillingAccountEntity billingAccount = billingAccountManager.getLatestBillingAccount(rid);
        List<BillingHistoryEntity> billings = billingHistoryManager.getHistory(rid, since);

        availableAccountUpdates.setJsonBilling(new JsonBilling(billingAccount, billings));
    }

    /**
     * Get all plans under all payment provider.
     */
    public List<ReceiptofiPlan> getAllPlans() {
        LOG.info("Getting all plans");
        List<ReceiptofiPlan> receiptofiPlans = planCache.getIfPresent(PLANS);
        if (null == receiptofiPlans) {
            receiptofiPlans = new ArrayList<>();

            for (PaymentGatewayEnum billingProvider : PaymentGatewayEnum.values()) {
                if (PaymentGatewayEnum.BT == billingProvider) {
                    receiptofiPlans.addAll(getAllBraintreePlans(billingProvider));
                } else {
                    LOG.error("Reached invalid PaymentGatewayEnum");
                    throw new IllegalStateException("Reached invalid payment gateway");
                }
            }

            planCache.put(PLANS, receiptofiPlans);
        }
        LOG.info("Total plans fetched size={}", receiptofiPlans.size());
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
            LOG.info("No plans found");
            receiptofiPlans = new ArrayList<>();

            try {
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
            } catch (Exception e) {
                LOG.error("Error getting all the plans reason={}", e.getLocalizedMessage(), e);
            }

        }
        LOG.info("Populated plans size={}", receiptofiPlans.size());
        return receiptofiPlans;
    }

    /**
     * Get payment gateway token to initialize SDK.
     */
    public Token getBrianTreeClientToken(String rid) {
        BraintreeToken braintreeToken;

        BillingAccountEntity billingAccount = billingAccountManager.getLatestBillingAccount(rid);
        if (StringUtils.isBlank(billingAccount.getCustomerId())) {
            braintreeToken = new BraintreeToken(paymentGatewayService.getGateway().clientToken().generate());
        } else {
            if (PaymentGatewayEnum.BT == billingAccount.getPaymentGateway()) {
                braintreeToken = getBraintreeToken(billingAccount);
            } else {
                LOG.error("Reached invalid PaymentGatewayEnum {}", billingAccount.getPaymentGateway());
                throw new IllegalStateException("Reached invalid payment gateway");
            }
        }
        return braintreeToken;
    }

    /**
     * Populate token with user information.
     */
    private BraintreeToken getBraintreeToken(BillingAccountEntity billingAccount) {
        ClientTokenRequest clientTokenRequest = new ClientTokenRequest().customerId(billingAccount.getCustomerId());
        /** Token from gateway can be null and should be sent to phone as null. */
        BraintreeToken braintreeToken = new BraintreeToken(paymentGatewayService.getGateway().clientToken().generate(clientTokenRequest));
        if (StringUtils.isBlank(braintreeToken.getToken())) {
            LOG.warn("Token not initialized rid={}", billingAccount.getRid());
        }
        braintreeToken.setHasCustomerInfo(true);
        braintreeToken.setFirstName(billingAccount.getFirstName());
        braintreeToken.setLastName(billingAccount.getLastName());
        braintreeToken.setPostalCode(billingAccount.getPostalCode());
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
        BillingAccountEntity billingAccount = billingAccountManager.getLatestBillingAccount(rid);

        ReceiptofiPlan receiptofiPlan = getPlan(planId);
        Assert.notNull(receiptofiPlan, "Could not find a plan for id=" + planId);

        BillingHistoryEntity billingHistory = billingHistoryManager.getHistory(rid, YYYY_MM.format(new Date()));
        if (null != billingHistory && BillingPlanEnum.P == billingHistory.getBillingPlan()) {
            LOG.info("User under promotional plan. Ignore this transaction and notify user.");
            return new TransactionDetailSubscription(
                    firstName,
                    lastName,
                    postal,
                    planId,
                    new StringBuilder().append("We appreciate your patronage to join ")
                            .append(receiptofiPlan.getName())
                            .append(" subscription plan, but you are currently enrolled for free promotional plan. ")
                            .append("Please sign up on a later date after promotional plan has ended. ")
                            .append("Billing History, lists the months you have been signed up for promotional plans.")
                            .toString());

        } else if (null != billingHistory && StringUtils.isBlank(billingHistory.getTransactionId())) {
            /**
             * This happens when user is not signed up for any subscription.
             * And nor signed up for promotional plan either.
             */
            TransactionDetail transaction = newPayment(
                    rid,
                    planId,
                    firstName,
                    lastName,
                    company,
                    postal,
                    receiptofiPlan,
                    paymentMethodNonce);


            if (transaction.isSuccess()) {
                /**
                 * After new billing history is created. Delete the existing billingHistory with no transaction id.
                 * Do not mark any history with valid transaction id as inactive.
                 */
                billingHistoryManager.deleteHard(billingHistory);

                /** When new billing account is created with new subscription id. Mark existing billing account inactive. */
                billingAccount.inActive();
                billingAccountManager.save(billingAccount);
            } else {
                transaction = new TransactionDetailSubscription(
                        firstName,
                        lastName,
                        postal,
                        planId,
                        ""
                );
            }

            return transaction;
        } else if (null != billingHistory) {
            LOG.info("Subscribing to planId={} and cancel transactionId={} and un-subscribe from planId={} where subscriptionId={} for rid={}",
                    planId, billingHistory.getTransactionId(), billingAccount.getBillingPlan().name(), billingAccount.getSubscriptionId(), rid);

            TransactionDetail transaction = null;
            StringBuilder message = new StringBuilder();

            if (cancelPayment(billingHistory)) {

                boolean cancelSubscriptionStatus;
                if (StringUtils.isNotBlank(billingAccount.getSubscriptionId())) {
                    cancelSubscriptionStatus = cancelSubscription(rid, billingAccount).isSuccess();
                    if (!cancelSubscriptionStatus) {

                        /** When we confirm subscription was cancelled successfully. */
                        message.append("Cancelled previous transaction successfully but failed to cancel subscription. ")
                                .append("Please un-subscribe and then try again to subscribe to new plan ")
                                .append(receiptofiPlan.getBillingPlan().getDescription())
                                .append(".");

                        return new TransactionDetailSubscription(
                                firstName,
                                lastName,
                                postal,
                                planId,
                                message.toString());
                    }
                }

                transaction = updateBilledPayment(
                        rid,
                        planId,
                        firstName,
                        lastName,
                        company,
                        postal,
                        receiptofiPlan,
                        paymentMethodNonce,
                        billingAccount);

                if (!transaction.isSuccess()) {
                    message.append("Cancelled previous transaction and subscription successfully. ")
                            .append("But failed to join to subscription plan ")
                            .append(receiptofiPlan.getBillingPlan().getDescription())
                            .append(". Please try later to subscribe to your selected plan.");

                    transaction = null;
                } else {
                    /** Mark the account as inactive since the new account is active. */
                    billingAccount.inActive();
                    billingAccountManager.save(billingAccount);
                }

            } else {
                LOG.error("Failed to cancel previous transaction rid={} transactionId={}", rid, billingHistory.getTransactionId());
                message.append("Failed to cancel previous transaction. Engineers are looking into this. Please try again later.");
            }

            if (transaction == null) {
                transaction = new TransactionDetailSubscription(
                        firstName,
                        lastName,
                        postal,
                        planId,
                        message.toString());
            }

            return transaction;
        } else {
            LOG.error("Failed to find any billing for month rid={} planId={}", rid, planId);
            return new TransactionDetailSubscription(
                    firstName,
                    lastName,
                    postal,
                    planId,
                    "Failed to find any billing for month");
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
                transaction.getProcessorResponseCode(),
                transaction.getProcessorResponseText());

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

            BillingAccountEntity billingAccount = new BillingAccountEntity(rid);
            billingAccount.setPaymentGateway(PaymentGatewayEnum.BT);
            billingAccount.setCustomerId(transaction.getCustomer().getId());
            billingAccount.setFirstName(firstName);
            billingAccount.setLastName(lastName);
            billingAccount.setCompany(company);
            billingAccount.setAddressId(transaction.getBillingAddress().getId());
            billingAccount.setPostalCode(postal);
            billingAccount.setBillingPlan(receiptofiPlan.getBillingPlan());
            /** Save early as adding user to subscription plan can fail. We would not like to lose this information. */
            billingAccountManager.save(billingAccount);

            BillingHistoryEntity billingHistory = createBillingHistory(rid, receiptofiPlan, PaymentGatewayEnum.BT, transaction.getId());
            billingHistoryManager.save(billingHistory);

            String subscriptionId = subscribe(receiptofiPlan, result.getTarget().getCreditCard().getToken());
            billingAccount.setSubscriptionId(subscriptionId);

            /** Adding subscription id to billing account. */
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
            String paymentMethodNonce,
            BillingAccountEntity billingAccount) {

        /** Updates existing billing account customer information. */
        updateCustomer(firstName, lastName, company, billingAccount);

        /** Updates existing billing account customer address information. */
        updateBillingAddress(postal, billingAccount);

        TransactionRequest request = new TransactionRequest();
        request.customerId(billingAccount.getCustomerId());
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

            BillingAccountEntity newBillingAccount = new BillingAccountEntity(rid);
            newBillingAccount.setPaymentGateway(PaymentGatewayEnum.BT);
            newBillingAccount.setCustomerId(transaction.getCustomer().getId());
            newBillingAccount.setFirstName(firstName);
            newBillingAccount.setLastName(lastName);
            newBillingAccount.setCompany(company);
            newBillingAccount.setAddressId(transaction.getBillingAddress().getId());
            newBillingAccount.setPostalCode(postal);
            newBillingAccount.setBillingPlan(receiptofiPlan.getBillingPlan());
            /** Save early as adding user to subscription plan can fail. We would not like to lose this information. */
            billingAccountManager.save(newBillingAccount);

            BillingHistoryEntity billingHistory = createBillingHistory(rid, receiptofiPlan, PaymentGatewayEnum.BT, transaction.getId());
            billingHistoryManager.save(billingHistory);

            String subscriptionId = subscribe(receiptofiPlan, result.getTarget().getCreditCard().getToken());
            newBillingAccount.setSubscriptionId(subscriptionId);

            /** Adding subscription id to billing account. */
            billingAccountManager.save(newBillingAccount);
        }

        LOG.debug("at exit {}", transactionDetail);
        return transactionDetail;
    }

    /**
     * Void or Refund existing transaction and then cancels subscription before signing up with new plan.
     */
    private boolean cancelPayment(BillingHistoryEntity billingHistory) {

        /** Highly unlikely to happen that refund will fail. Hence condition of 'OR' to complete un-subscribe. */
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
        } else {
            LOG.error("Failed to refund payment transactionId={} rid={}", billingHistory.getTransactionId(), billingHistory.getRid());
        }
        return transactionStatus != null;
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
     * Cancels last subscription.
     */
    public TransactionDetail cancelLastSubscription(String rid) {
        /** Existing billingAccount with be marked as inActive and instead new BillingAccount would be created. */
        BillingAccountEntity billingAccount = billingAccountManager.getLatestBillingAccount(rid);
        TransactionDetail transactionDetail = cancelSubscription(rid, billingAccount);

        BillingAccountEntity newBillingAccount = new BillingAccountEntity(rid);
        newBillingAccount.setPaymentGateway(PaymentGatewayEnum.BT);
        newBillingAccount.setCustomerId(billingAccount.getCustomerId());
        newBillingAccount.setFirstName(billingAccount.getFirstName());
        newBillingAccount.setLastName(billingAccount.getLastName());
        newBillingAccount.setCompany(billingAccount.getCompany());
        newBillingAccount.setAddressId(billingAccount.getAddressId());
        newBillingAccount.setPostalCode(billingAccount.getPostalCode());
        newBillingAccount.setBillingPlan(BillingPlanEnum.NB);
        billingAccountManager.save(newBillingAccount);

        return transactionDetail;
    }

    /**
     * Cancels an existing subscription.
     */
    private TransactionDetail cancelSubscription(String rid, BillingAccountEntity billingAccount) {
        TransactionDetail transactionDetail = null;
        String message = null;

        if (StringUtils.isNotBlank(billingAccount.getSubscriptionId())) {
            try {
                Result<Subscription> result = paymentGatewayService.getGateway().subscription().cancel(billingAccount.getSubscriptionId());
                Subscription subscription = result.getTarget();
                if (result.isSuccess() &&
                        StringUtils.isNotBlank(subscription.getId()) &&
                        billingAccount.getSubscriptionId().equals(subscription.getId())) {

                    LOG.info("Success canceled subscription subscriptionId={} rid={} status={} resultId={}",
                            billingAccount.getSubscriptionId(), rid, subscription.getStatus(), result.getTarget().getId());

                    billingAccount.inActive();
                    billingAccountManager.save(billingAccount);

                    transactionDetail = new TransactionDetailSubscription(
                            result.isSuccess(),
                            subscription.getStatus().name(),
                            subscription.getPlanId(),
                            billingAccount.getFirstName(),
                            billingAccount.getLastName(),
                            billingAccount.getPostalCode(),
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
            LOG.error("Subscription not found subscriptionId={} rid={}", billingAccount.getSubscriptionId(), rid);
            message = "Subscription could not be found.";
        }

        if (null == transactionDetail) {
            Assert.hasLength(message, "Message for TransactionDetailPayment is null");
            transactionDetail = new TransactionDetailSubscription(
                    billingAccount.getFirstName(),
                    billingAccount.getLastName(),
                    billingAccount.getPostalCode(),
                    billingAccount.getBillingPlan().getName(),
                    message
            );
        }

        LOG.debug("at exit {}", transactionDetail);
        return transactionDetail;
    }

    /**
     * Create new when BillingHistory for a new transaction Id.
     */
    private BillingHistoryEntity createBillingHistory(
            String rid,
            ReceiptofiPlan receiptofiPlan,
            PaymentGatewayEnum paymentGatewayEnum,
            String transactionId
    ) {
        BillingHistoryEntity billingHistory = new BillingHistoryEntity(rid, new Date());
        billingHistory.setBilledStatus(BilledStatusEnum.B);
        billingHistory.setBillingPlan(receiptofiPlan.getBillingPlan());
        billingHistory.setPaymentGateway(paymentGatewayEnum);
        billingHistory.setTransactionId(transactionId);
        billingHistory.setTransactionStatus(TransactionStatusEnum.S);
        return billingHistory;
    }

    private void updateCustomer(
            String firstName,
            String lastName,
            String company,
            BillingAccountEntity billingAccount
    ) {
        boolean modified = false;

        if (!firstName.equals(billingAccount.getFirstName())) {
            billingAccount.setFirstName(firstName);
            modified = true;
        }
        if (!lastName.equals(billingAccount.getLastName())) {
            billingAccount.setLastName(lastName);
            modified = true;
        }

        if (null != company && null != billingAccount.getCompany() && !company.equals(billingAccount.getCompany())) {
            billingAccount.setCompany(company);
            modified = true;
        }

        if (modified) {
            billingAccountManager.save(billingAccount);
            CustomerRequest customerRequest = new CustomerRequest();
            customerRequest
                    .firstName(firstName)
                    .lastName(lastName)
                    .company(company);
            paymentGatewayService.getGateway().customer().update(billingAccount.getCustomerId(), customerRequest);
        }
    }

    private void updateBillingAddress(
            String postal,
            BillingAccountEntity billingAccount
    ) {
        boolean modified = false;

        if (!postal.equals(billingAccount.getPostalCode())) {
            billingAccount.setPostalCode(postal);
            modified = true;
        }

        if (modified) {
            billingAccountManager.save(billingAccount);
            AddressRequest addressRequest = new AddressRequest();
            addressRequest
                    .postalCode(postal);
            paymentGatewayService.getGateway().address().update(billingAccount.getCustomerId(), billingAccount.getAddressId(), addressRequest);
        }
    }
}
