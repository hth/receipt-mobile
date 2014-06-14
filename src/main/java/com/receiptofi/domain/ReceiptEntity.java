/**
 *
 */
package com.receiptofi.domain;

import com.receiptofi.domain.types.DocumentStatusEnum;
import com.receiptofi.utils.HashText;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.format.annotation.NumberFormat.Style;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author hitender
 * @since Dec 26, 2012 12:09:01 AM
 *
 */
@Document(collection = "RECEIPT")
@CompoundIndexes(value = {
        @CompoundIndex(name = "receipt_idx",           def = "{'FS': -1, 'USER_PROFILE_ID': -1}"),
        @CompoundIndex(name = "receipt_unique_idx",    def = "{'CHECK_SUM': -1}", unique = true),
        @CompoundIndex(name = "receipt_expense_Report",def = "{'EXP_FILENAME': -1}")
} )
@JsonIgnoreProperties({
        "receiptStatus",
        "year",
        "month",
        "day",
        "tax",
        "receiptOCRId",
        "recheckComment",
        "checksum",

        "version",
        "updated",
        "created",
        "active",
        "deleted"
})
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
public final class ReceiptEntity extends BaseEntity {

	@NotNull
    @Field("DS_E")
	private DocumentStatusEnum receiptStatus;

    @JsonProperty("files")
    @DBRef
    @Field("FS")
	private Collection<FileSystemEntity> fileSystemEntities;

    @JsonProperty("date")
	@NotNull
    @DateTimeFormat(iso = ISO.DATE_TIME)
    @Field("RECEIPT_DATE")
	private Date receiptDate;

	@NotNull
    @Field("YEAR")
	private int year;

	@NotNull
    @Field("MONTH")
	private int month;

	@NotNull
    @Field("DAY")
	private int day;

	@NotNull
	@NumberFormat(style = Style.CURRENCY)
    @Field("TOTAL")
	private Double total;

	@NumberFormat(style = Style.CURRENCY)
    @Field("TAX")
	private Double tax = 0.00;

    @JsonProperty("ptax")
    @NotNull
    @NumberFormat(style = Style.PERCENT)
    @Field("PERCENT_TAX")
    private String percentTax;

    @JsonProperty("rid")
	@NotNull
    @Field("USER_PROFILE_ID")
	private String userProfileId;

    @DBRef
    @Field("BIZ_NAME")
    private BizNameEntity bizName;

    @DBRef
    @Field("BIZ_STORE")
    private BizStoreEntity bizStore;

    @NotNull
    @Field("RECEIPT_OCR_ID")
    private String receiptOCRId;

    @DBRef
    @Field("COMMENT_RECHECK")
    private CommentEntity recheckComment;

    @DBRef
    @Field("COMMENT_NOTES")
    private CommentEntity notes;

    /**
     * Note: During recheck of a receipt EXP_FILENAME is dropped as this is
     * not persisted between the two event
     */
    @JsonProperty("expenseReport")
    @Field("EXP_FILENAME")
    private String expenseReportInFS;

    /**
     * Used to flush or avoid duplicate receipt entry
     */
    @Field("CHECK_SUM")
    private String checksum;

    /** To keep bean happy */
	public ReceiptEntity() {}

    @Deprecated
	private ReceiptEntity(Date receiptDate, Double total, Double tax, DocumentStatusEnum receiptStatus, FileSystemEntity fileSystemEntities, String userProfileId) {
		super();
		this.receiptDate = receiptDate;
		this.total = total;
		this.tax = tax;
		this.receiptStatus = receiptStatus;
		this.fileSystemEntities.add(fileSystemEntities);
		this.userProfileId = userProfileId;
	}

	/**
	 * Use this method to create the Entity for OCR Entity
	 *
	 * @param receiptDate
	 * @param total
	 * @param tax
	 * @param receiptStatus
	 * @param receiptBlobId
	 * @param userProfileId
	 * @return
	 */
    @Deprecated
	public static ReceiptEntity newInstance(Date receiptDate, Double total, Double tax, DocumentStatusEnum receiptStatus, FileSystemEntity receiptBlobId, String userProfileId) {
		return new ReceiptEntity(receiptDate, total, tax, receiptStatus, receiptBlobId, userProfileId);
	}

	public static ReceiptEntity newInstance() {
		return new ReceiptEntity();
	}

	public DocumentStatusEnum getReceiptStatus() {
		return receiptStatus;
	}

	public void setReceiptStatus(DocumentStatusEnum receiptStatus) {
		this.receiptStatus = receiptStatus;
	}

	public Collection<FileSystemEntity> getFileSystemEntities() {
		return fileSystemEntities;
	}

    public void setFileSystemEntities(Collection<FileSystemEntity> fileSystemEntities) {
        this.fileSystemEntities = fileSystemEntities;
    }

    public Date getReceiptDate() {
		return receiptDate;
	}

	public void setReceiptDate(Date receiptDate) {
        DateTime dt = new DateTime(receiptDate);
        this.year = dt.getYear();
        this.month = dt.getMonthOfYear();
        this.day = dt.getDayOfMonth();
		this.receiptDate = receiptDate;
	}

	public int getYear() {
		return year;
	}

	public int getMonth() {
		return month;
	}

	public int getDay() {
		return day;
	}

	public Double getTotal() {
		return total;
	}

	public void setTotal(Double total) {
		this.total = total;
	}

    /**
     * Used to show the value in notification
     *
     * @return
     */
    @Transient
    public String getTotalString() {
        //TODO try using JODA currency
        java.text.NumberFormat currencyFormatter = java.text.NumberFormat.getCurrencyInstance();
        return currencyFormatter.format(getTotal());
    }

	public Double getTax() {
		return tax;
	}

    public void setTax(Double tax) {
		this.tax = tax;
	}

    /**
     * Percentage of tax paid for all the items that were taxed
     *
     * @return
     */
    public String getPercentTax() {
        return percentTax;
    }

    /**
     * Percentage of tax paid for all the items that were taxed. Scaled till 6th value
     * @param percentTax - 0.666667
     */
    public void setPercentTax(String percentTax) {
        this.percentTax = percentTax;
    }

    /**
     * Used for displaying on receipt page till 4th decimal. 0.666
     *
     * @return
     */
    @Transient
    public String getPercentTax4Display() {
        return StringUtils.substring(this.percentTax, 0, 5);
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

    public BizStoreEntity getBizStore() {
        return bizStore;
    }

    public void setBizStore(BizStoreEntity bizStore) {
        this.bizStore = bizStore;
    }

    public String getReceiptOCRId() {
        return receiptOCRId;
    }

    public void setReceiptOCRId(String receiptOCRId) {
        this.receiptOCRId = receiptOCRId;
    }

    public CommentEntity getRecheckComment() {
        return recheckComment;
    }

    public void setRecheckComment(CommentEntity recheckComment) {
        this.recheckComment = recheckComment;
    }

    public CommentEntity getNotes() {
        return notes;
    }

    public void setNotes(CommentEntity notes) {
        this.notes = notes;
    }

    public String getChecksum() {
        return checksum;
    }

    /**
     * Create for making sure no duplicate receipt could be entered. At a time, there can only be two status of receipt
     * co-exists
     *  1) Receipt deleted
     *  2) Receipt not deleted
     */
    public void computeChecksum() {
        this.checksum = HashText.calculateChecksum(userProfileId, receiptDate, total, isDeleted());
    }

    public String getExpenseReportInFS() {
        return expenseReportInFS;
    }

    public void setExpenseReportInFS(String expenseReportInFS) {
        this.expenseReportInFS = expenseReportInFS;
    }

    @Override
    public String toString() {
        return "ReceiptEntity{" +
                "receiptStatus=" + receiptStatus +
                ", fileSystemEntities=" + fileSystemEntities +
                ", receiptDate=" + receiptDate +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", total=" + total +
                ", tax=" + tax +
                ", percentTax='" + percentTax + '\'' +
                ", userProfileId='" + userProfileId + '\'' +
                ", bizName=" + bizName +
                ", bizStore=" + bizStore +
                ", receiptOCRId='" + receiptOCRId + '\'' +
                ", recheckComment=" + recheckComment +
                ", notes=" + notes +
                ", expenseReportInFS='" + expenseReportInFS + '\'' +
                ", checksum='" + checksum + '\'' +
                '}';
    }
}
