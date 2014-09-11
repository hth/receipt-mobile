package com.receiptofi.mobile.domain.mapping;

import java.util.Collection;
import java.util.LinkedList;

import com.receiptofi.domain.FileSystemEntity;
import com.receiptofi.domain.ReceiptEntity;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * User: hitender
 * Date: 8/24/14 9:35 PM
 */
@JsonAutoDetect (
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
@JsonPropertyOrder (alphabetic = true)
@JsonIgnoreProperties (ignoreUnknown = true)
//@JsonInclude (JsonInclude.Include.NON_NULL)
@SuppressWarnings ({"unused", "PMD.BeanMembersShouldSerialize"})
public final class Receipt {
    private static final DateTimeFormatter FMT = ISODateTimeFormat.dateTime();

    @JsonProperty ("id")
    private String id;

    @JsonProperty ("total")
    private Double total;

    @JsonProperty ("bizName")
    private BizName bizName;

    @JsonProperty ("bizStore")
    private BizStore bizStore;

    @JsonProperty ("notes")
    private Comment notes;

    @JsonProperty ("files")
    private Collection<FileSystem> fileSystems = new LinkedList<>();

    @JsonProperty ("date")
    private String receiptDate;

    @JsonProperty ("ptax")
    private String percentTax;

    @JsonProperty ("rid")
    private String userProfileId;

    @JsonProperty ("expenseReport")
    private String expenseReportInFS;

    private Receipt(ReceiptEntity receiptEntity) {
        this.id = receiptEntity.getId();
        this.total = receiptEntity.getTotal();
        this.bizName = BizName.newInstance(receiptEntity.getBizName());
        this.bizStore = BizStore.newInstance(receiptEntity.getBizStore());
        this.notes = Comment.newInstance(receiptEntity.getNotes());

        for (FileSystemEntity fileSystemEntity : receiptEntity.getFileSystemEntities()) {
            this.fileSystems.add(FileSystem.newInstance(fileSystemEntity));
        }

        this.receiptDate = FMT.print(new DateTime(receiptEntity.getReceiptDate()));
        this.percentTax = receiptEntity.getPercentTax();
        this.userProfileId = receiptEntity.getUserProfileId();
        this.expenseReportInFS = receiptEntity.getExpenseReportInFS();
    }

    public static Receipt newInstance(ReceiptEntity receiptEntity) {
        return new Receipt(receiptEntity);
    }
}
