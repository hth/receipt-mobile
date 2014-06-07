package com.receiptofi.domain;

import com.receiptofi.utils.DateUtil;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * User: hitender
 * Date: 5/13/13
 * Time: 7:47 PM
 */

/** As reference field : ET_R */
@Document(collection = "EXPENSE_TAG")
@CompoundIndexes(value = {
        @CompoundIndex(name = "expense_tag_idx",    def = "{'RID': 1, 'TAG': 1}",  unique=true),
} )
public final class ExpenseTagEntity extends BaseEntity {

    @NotNull
    @Size(min = 0, max = 6)
    @Field("TAG")
    private String tagName;

    @NotNull
    @Size(max = 4)
    @Field("YEAR")
    private int forYear;

    @NotNull
    @Field("RID")
    private String receiptUserId;

    /** To make bean happy */
    public ExpenseTagEntity() {}

    public static ExpenseTagEntity newInstance(String expName, String receiptUserId) {
        ExpenseTagEntity expenseTagEntity = new ExpenseTagEntity();
        expenseTagEntity.setTagName(expName);
        expenseTagEntity.setReceiptUserId(receiptUserId);
        expenseTagEntity.setForYear(DateUtil.now().getYear());
        return expenseTagEntity;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public int getForYear() {
        return forYear;
    }

    public void setForYear(int forYear) {
        this.forYear = forYear;
    }

    public String getReceiptUserId() {
        return receiptUserId;
    }

    public void setReceiptUserId(String receiptUserId) {
        this.receiptUserId = receiptUserId;
    }
}
