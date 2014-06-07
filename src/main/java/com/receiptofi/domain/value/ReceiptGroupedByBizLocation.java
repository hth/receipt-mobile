package com.receiptofi.domain.value;

import com.receiptofi.domain.BizNameEntity;
import com.receiptofi.domain.BizStoreEntity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;

/**
 * TODO this class can be further extended to individually list out the expense in that store on a particular date.
 *
 * User: hitender
 * Date: 8/27/13 8:04 PM
 */
public final class ReceiptGroupedByBizLocation implements Serializable {

    private BigDecimal total;
    private BizNameEntity bizName;
    private BizStoreEntity bizStore;

    @SuppressWarnings("unused")
    private ReceiptGroupedByBizLocation() {}

    @SuppressWarnings("unused")
    private ReceiptGroupedByBizLocation(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getTotalStr() {
        NumberFormat fmt = NumberFormat.getCurrencyInstance();
       return fmt.format(getTotal());
    }

    public BizNameEntity getBizName() {
        return bizName;
    }

    public void setBizName(BizNameEntity bizName) {
        this.bizName = bizName;
    }

    public BizStoreEntity getBizStore() {
        return bizStore;
    }

    public void setBizStore(BizStoreEntity bizStore) {
        this.bizStore = bizStore;
    }
}
