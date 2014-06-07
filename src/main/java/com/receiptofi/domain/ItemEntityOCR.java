/**
 *
 */
package com.receiptofi.domain;

import com.receiptofi.domain.types.TaxEnum;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.format.annotation.NumberFormat.Style;

/**
 * @author hitender
 * @since Jan 6, 2013 1:17:12 PM
 *
 */
@Document(collection = "ITEM_OCR")
@CompoundIndexes({ @CompoundIndex(name = "user_item_ocr_idx", def = "{'RECEIPT': -1, 'USER_PROFILE_ID': 1}") })
public final class ItemEntityOCR extends BaseEntity {

	@Size(min = 1, max = 128)
    @Field("NAME")
	private String name;

	@NumberFormat(style = Style.CURRENCY)
    @Field("PRICE")
	private String price;

    @Field("QUANTITY")
    private Double quantity = 1.00;

	@NotNull
    @Field("TAX_ENUM")
	private TaxEnum taxed = TaxEnum.NOT_TAXED;

	@NotNull
    @Field("SEQUENCE")
	private int sequence;

	@DBRef
    @Field("RECEIPT")
	private DocumentEntity receipt;

    @Field("R_D")
    private String receiptDate;

	@NotNull
    @Field("USER_PROFILE_ID")
	private String userProfileId;

    @DBRef
    @Field("BIZ_NAME")
    private BizNameEntity bizName;

    @DBRef
    @Field("ET_R")
    private ExpenseTagEntity expenseTag;

	/** To keep spring happy in recreating the bean from form during submit action */
	public ItemEntityOCR() {}

	/**
	 * This method is used when the Entity is created for the first time or during receipt re-check.
	 */
	public static ItemEntityOCR newInstance() {
	    return new ItemEntityOCR();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

	public TaxEnum getTaxed() {
		return taxed;
	}

	public void setTaxed(TaxEnum taxed) {
		this.taxed = taxed;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public DocumentEntity getReceipt() {
		return this.receipt;
	}

	public void setReceipt(DocumentEntity receipt) {
		this.receipt = receipt;
        this.receiptDate = receipt.getReceiptDate();
	}

    public String getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(String receiptDate) {
        this.receiptDate = receiptDate;
    }

    public String getUserProfileId() {
		return userProfileId;
	}

	public void setUserProfileId(String userProfileId) {
		this.userProfileId = userProfileId;
	}

    public BizNameEntity getBizName() {
        return bizName;
    }

    public void setBizName(BizNameEntity bizName) {
        this.bizName = bizName;
    }

    public ExpenseTagEntity getExpenseTag() {
        return expenseTag;
    }

    public void setExpenseTag(ExpenseTagEntity expenseTag) {
        this.expenseTag = expenseTag;
    }

    @Override
	public String toString() {
		return "ItemEntity [name=" + name + ", price=" + price + ", taxed=" + taxed + "]";
	}
}
