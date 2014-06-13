/**
 *
 */
package com.receiptofi.domain;

import com.receiptofi.domain.types.DocumentOfTypeEnum;
import com.receiptofi.domain.types.DocumentStatusEnum;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author hitender
 * @since Jan 6, 2013 1:04:43 PM
 *
 */
@Document(collection = "DOCUMENT")
@CompoundIndexes({ @CompoundIndex(name = "document_idx", def = "{'FS': 1, 'USER_PROFILE_ID': 1}") })
public final class DocumentEntity extends BaseEntity {

	@NotNull
    @Field("DS_E")
	private DocumentStatusEnum documentStatus;

    @DBRef
    @Field("FS")
	private Collection<FileSystemEntity> fileSystemEntities;

	@NotNull
    @Field("RECEIPT_DATE")
	private String receiptDate;

    @Transient
    @Field("SUB_TOTAL")
    private String subTotal;

	@NotNull
    @Field("TOTAL")
	private String total;

	@NotNull
    @Field("TAX")
	private String tax = "0.00";

	@NotNull
    @Field("USER_PROFILE_ID")
	private String userProfileId;

    @DBRef
    @Field("BIZ_NAME")
    private BizNameEntity bizName;

    @DBRef
    @Field("BIZ_STORE")
    private BizStoreEntity bizStore;

    @Field("RECEIPT_ID")
    private String receiptId;

    @DBRef
    @Field("COMMENT_RECHECK")
    private CommentEntity recheckComment;

    @DBRef
    @Field("COMMENT_NOTES")
    private CommentEntity notes;

    @NotNull
    @Field("DOCUMENT_TYPE")
    private DocumentOfTypeEnum documentOfType;

    /** To keep bean happy */
	public DocumentEntity() {}

	public static DocumentEntity newInstance() {
		return new DocumentEntity();
	}

	public DocumentStatusEnum getDocumentStatus() {
		return documentStatus;
	}

	public void setDocumentStatus(DocumentStatusEnum documentStatus) {
		this.documentStatus = documentStatus;
	}

    public Collection<FileSystemEntity> getFileSystemEntities() {
		return fileSystemEntities;
	}

	public void addReceiptBlobId(FileSystemEntity receiptBlobId) {
        if(this.fileSystemEntities == null) {
            this.fileSystemEntities = new ArrayList<>();
        }
		this.fileSystemEntities.add(receiptBlobId);
	}

    public void setFileSystemEntities(Collection<FileSystemEntity> fileSystemEntities) {
        this.fileSystemEntities = fileSystemEntities;
    }

    public String getReceiptDate() {
		return receiptDate;
	}

	public void setReceiptDate(String receiptDate) {
		this.receiptDate = receiptDate;
	}

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

    public String getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(String subTotal) {
        this.subTotal = subTotal;
    }

    public String getTax() {
		return tax;
	}

	public void setTax(String tax) {
		this.tax = tax;
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

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
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

    @SuppressWarnings("unused")
    public DocumentOfTypeEnum getDocumentOfType() {
        return documentOfType;
    }

    public void setDocumentOfType(DocumentOfTypeEnum documentOfType) {
        this.documentOfType = documentOfType;
    }

    @Override
    public String toString() {
        return "DocumentEntity{" +
                "documentStatus=" + documentStatus +
                ", fileSystemEntities=" + fileSystemEntities +
                ", receiptDate='" + receiptDate + '\'' +
                ", subTotal='" + subTotal + '\'' +
                ", total='" + total + '\'' +
                ", tax='" + tax + '\'' +
                ", userProfileId='" + userProfileId + '\'' +
                ", bizName=" + bizName +
                ", bizStore=" + bizStore +
                ", receiptId='" + receiptId + '\'' +
                ", recheckComment=" + recheckComment +
                ", notes=" + notes +
                ", documentOfType=" + documentOfType +
                '}';
    }
}
