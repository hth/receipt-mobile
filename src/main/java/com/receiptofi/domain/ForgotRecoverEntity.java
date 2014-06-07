package com.receiptofi.domain;

import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * User: hitender
 * Date: 6/4/13
 * Time: 12:02 AM
 */
@Document(collection = "FORGOT_RECOVER")
@CompoundIndexes(value = {
        @CompoundIndex(name = "forgot_recover_idx",     def = "{'RID': 0, 'AUTH' : 0}", unique = true)
} )
public final class ForgotRecoverEntity extends BaseEntity {

    @NotNull
    @Field("RID")
    private final String receiptUserId;

    @NotNull
    @Field("AUTH")
    private final String authenticationKey;

    private ForgotRecoverEntity(String receiptUserId, String authenticationKey) {
        this.receiptUserId = receiptUserId;
        this.authenticationKey = authenticationKey;
    }

    public static ForgotRecoverEntity newInstance(String receiptUserId, String authenticationKey) {
        return new ForgotRecoverEntity(receiptUserId, authenticationKey);
    }

    public String getReceiptUserId() {
        return receiptUserId;
    }

    public String getAuthenticationKey() {
        return authenticationKey;
    }
}
