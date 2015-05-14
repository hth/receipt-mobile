package com.receiptofi.mobile.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import com.receiptofi.domain.BillingAccountEntity;
import com.receiptofi.domain.BillingHistoryEntity;
import com.receiptofi.domain.json.JsonBilling;
import com.receiptofi.domain.types.PaymentGatewayEnum;
import com.receiptofi.domain.value.PaymentGatewayUser;
import com.receiptofi.mobile.domain.AvailableAccountUpdates;
import com.receiptofi.mobile.domain.ReceiptofiPlan;
import com.receiptofi.mobile.repository.BillingAccountManagerMobile;
import com.receiptofi.mobile.repository.BillingHistoryManagerMobile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.braintreegateway.AddressRequest;
import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.ClientTokenRequest;
import com.braintreegateway.CustomerRequest;
import com.braintreegateway.Environment;
import com.braintreegateway.Plan;
import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;

import java.math.BigDecimal;
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

    private BraintreeGateway gateway;
    private BillingAccountManagerMobile billingAccountManager;
    private BillingHistoryManagerMobile billingHistoryManager;

    /** Cache plans. */
    private final Cache<String, List<ReceiptofiPlan>> planCache;
    private final Cache<PaymentGatewayEnum, List<ReceiptofiPlan>> planProviderCache;

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

            @Value ("${plan.cache.minutes}")
            int planCacheMinutes,

            BillingAccountManagerMobile billingAccountManager,
            BillingHistoryManagerMobile billingHistoryManager
    ) {
        if (brainTreeEnvironment.equals("PRODUCTION")) {
            gateway = new BraintreeGateway(
                    Environment.PRODUCTION,
                    brainTreeMerchantId,
                    brainTreePublicKey,
                    brainTreePrivateKey
            );

            planCache = CacheBuilder.newBuilder()
                    .maximumSize(1)
                    .expireAfterWrite(planCacheMinutes, TimeUnit.MINUTES)
                    .build();

            planProviderCache = CacheBuilder.newBuilder()
                    .maximumSize(20)
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
                    .maximumSize(1)
                    .expireAfterWrite(planCacheMinutes, TimeUnit.MINUTES)
                    .build();

            planProviderCache = CacheBuilder.newBuilder()
                    .maximumSize(20)
                    .expireAfterWrite(planCacheMinutes, TimeUnit.MINUTES)
                    .build();
        }

        this.billingAccountManager = billingAccountManager;
        this.billingHistoryManager = billingHistoryManager;
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
        if (receiptofiPlans == null) {
            receiptofiPlans = new ArrayList<>();

            for (PaymentGatewayEnum billingProvider : PaymentGatewayEnum.values()) {
                switch (billingProvider) {
                    case BT:
                        receiptofiPlans.addAll(getAllBraintreePlans(billingProvider));
                        break;
                    default:
                        LOG.error("Reached unreachable condition for Billing Provider");
                        throw new IllegalStateException("Reached unreachable condition for Billing Provider");
                }
            }

            planCache.put(PLANS, receiptofiPlans);
        }
        return receiptofiPlans;
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

                receiptofiPlans.add(receiptofiPlan);
            }
            planProviderCache.put(paymentGateway, receiptofiPlans);
        }
        return receiptofiPlans;
    }

    public String getBrianTreeClientToken(String rid) {
        String clientToken;

        BillingAccountEntity billingAccount = billingAccountManager.getBillingAccount(rid);
        if (billingAccount.getPaymentGateway().isEmpty()) {
            clientToken = gateway.clientToken().generate();
        } else {
            PaymentGatewayUser paymentGatewayUser = billingAccount.getPaymentGateway().getLast();
            switch (paymentGatewayUser.getPaymentGateway()) {
                case BT:
                    ClientTokenRequest clientTokenRequest = new ClientTokenRequest().customerId(paymentGatewayUser.getCustomerId());
                    clientToken = gateway.clientToken().generate(clientTokenRequest);
                    break;
                default:
                    LOG.error("Reached unreachable condition ", billingAccount.getPaymentGateway());
                    throw new IllegalStateException("Reached unreachable condition for payment gateway");
            }
        }
        Assert.hasText(clientToken, "Client token is empty");
        return clientToken;
    }

    //https://developers.braintreepayments.com/ios+java/reference/general/testing
    public boolean paymentPersonal(
            String rid,
            BigDecimal amount,
            String firstName,
            String lastName,
            String cardNumber,
            String month,
            String year,
            String cvv,
            String postal
    ) {
        BillingAccountEntity billingAccount = billingAccountManager.getBillingAccount(rid);
        PaymentGatewayUser paymentGatewayUser;
        if (billingAccount.getPaymentGateway().isEmpty()) {
            TransactionRequest request = new TransactionRequest();
            request.customer()
                    .firstName(firstName)
                    .lastName(lastName);
            request.creditCard()
                    .number(cardNumber)
                    .expirationMonth(month)
                    .expirationYear(year)
                    .cvv(cvv);
            request.billingAddress()
                    .firstName(firstName)
                    .lastName(lastName)
                    .postalCode(postal);
            request.amount(amount)
                    .paymentMethodNonce("nonce-from-the-client")
                    .options()
                    .submitForSettlement(true)
                    .storeInVaultOnSuccess(true)
                    .addBillingAddressToPaymentMethod(true)
                    .done();
            request.recurring(true);

            Result<Transaction> result = gateway.transaction().sale(request);
            if (result.isSuccess()) {
                paymentGatewayUser = new PaymentGatewayUser();
                paymentGatewayUser.setCustomerId(result.getTarget().getCustomer().getId());
                paymentGatewayUser.setPaymentGateway(PaymentGatewayEnum.BT);
                paymentGatewayUser.setFirstName(firstName);
                paymentGatewayUser.setLastName(lastName);
                paymentGatewayUser.setAddressId(result.getTarget().getBillingAddress().getId());
                paymentGatewayUser.setPostalCode(postal);
                billingAccount.addPaymentGateway(paymentGatewayUser);
                billingAccountManager.save(billingAccount);
                return true;
            } else {
                return false;
            }
        } else {
            paymentGatewayUser = billingAccount.getPaymentGateway().getLast();

            updateCustomer(firstName, lastName, paymentGatewayUser, billingAccount);
            updateBillingAddress(postal, paymentGatewayUser, billingAccount);

            TransactionRequest request = new TransactionRequest();
            request.customerId(paymentGatewayUser.getCustomerId());
            request.creditCard()
                    .number(cardNumber)
                    .expirationMonth(month)
                    .expirationYear(year)
                    .cvv(cvv);
            request.amount(amount)
                    .paymentMethodNonce("nonce-from-the-client")
                    .options()
                    .submitForSettlement(true)
                    .done();

            Result<Transaction> result = gateway.transaction().sale(request);
            return result.isSuccess();
        }
    }

    private void updateCustomer(
            String firstName,
            String lastName,
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

        if (modified) {
            billingAccountManager.save(billingAccount);
            CustomerRequest customerRequest = new CustomerRequest();
            customerRequest
                    .firstName(firstName)
                    .lastName(lastName);
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

//    AddressRequest request = new AddressRequest()
//            .firstName("Jenna")
//            .lastName("Smith")
//            .company("Braintree")
//            .streetAddress("1 E Main St")
//            .extendedAddress("Suite 403")
//            .locality("Chicago")
//            .region("Illinois")
//            .postalCode("60622")
//            .countryCodeAlpha2("US");

    public boolean paymentBusiness(String rid) {
        TransactionRequest request = new TransactionRequest();
        request.customer()
                .firstName("Jac")
                .lastName("Paui")
                .company("Jones Co.");

        request.creditCard().number("4111111111111111").expirationMonth("05").expirationYear("2016");

        request.amount(new BigDecimal("100.00"))
                .customerId(rid)
                .options()
                .submitForSettlement(true)
                .storeInVaultOnSuccess(true)
                .done();

        Result<Transaction> result = gateway.transaction().sale(request);
        return result.isSuccess();
    }
}
