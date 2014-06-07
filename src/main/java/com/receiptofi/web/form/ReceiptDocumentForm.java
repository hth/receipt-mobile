/**
 *
 */
package com.receiptofi.web.form;

import com.receiptofi.domain.DocumentEntity;
import com.receiptofi.domain.ItemEntity;
import com.receiptofi.domain.ItemEntityOCR;
import com.receiptofi.domain.MileageEntity;
import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.types.DocumentStatusEnum;
import com.receiptofi.domain.types.TaxEnum;
import com.receiptofi.utils.DateUtil;
import com.receiptofi.utils.Formatter;
import com.receiptofi.utils.Maths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

/**
 * @author hitender
 * @since Jan 7, 2013 9:30:32 AM
 *
 * This is a Form Backing Object (FBO) for showing the receipt and its items
 */
public final class ReceiptDocumentForm {
    private static final Logger log = LoggerFactory.getLogger(ReceiptDocumentForm.class);

	DocumentEntity receiptDocument;
    MileageEntity mileage;
	List<ItemEntityOCR> items;

    /** Used for showing error messages to user when the request action fails to execute */
    String errorMessage;

	/**
	 * Need for bean instantiation in ReceiptUpdateForm
	 */
	private ReceiptDocumentForm() {}

	private ReceiptDocumentForm(DocumentEntity receiptDocument, List<ItemEntityOCR> items) {
		this.receiptDocument = receiptDocument;
		this.items = items;
	}

	public static ReceiptDocumentForm newInstance(DocumentEntity receipt, List<ItemEntityOCR> items) {
		return new ReceiptDocumentForm(receipt, items);
	}

	public static ReceiptDocumentForm newInstance() {
		return new ReceiptDocumentForm();
	}

	public DocumentEntity getReceiptDocument() {
		return receiptDocument;
	}

	public void setReceiptDocument(DocumentEntity receiptDocument) {
		this.receiptDocument = receiptDocument;
	}

	public List<ItemEntityOCR> getItems() {
		return items;
	}

	public void setItems(List<ItemEntityOCR> items) {
		this.items = items;
	}

    public MileageEntity getMileage() {
        return mileage;
    }

    public void setMileage(MileageEntity mileage) {
        this.mileage = mileage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

	@Override
	public String toString() {
		return "ReceiptDocumentForm [receiptDocument=" + receiptDocument + ", items=" + items + "]";
	}

	public ReceiptEntity getReceiptEntity() throws NumberFormatException, ParseException {
        ReceiptEntity receipt = ReceiptEntity.newInstance();
        receipt.setReceiptDate(DateUtil.getDateFromString(receiptDocument.getReceiptDate()));
        receipt.setTotal(Formatter.getCurrencyFormatted(receiptDocument.getTotal()).doubleValue());
        receipt.setTax(Formatter.getCurrencyFormatted(receiptDocument.getTax()).doubleValue());
        receipt.setReceiptStatus(DocumentStatusEnum.TURK_PROCESSED);
        receipt.setUserProfileId(receiptDocument.getUserProfileId());
		receipt.setCreated(receiptDocument.getCreated());
        receipt.setUpdated();
        receipt.setBizName(receiptDocument.getBizName());
        receipt.setBizStore(receiptDocument.getBizStore());
        receipt.computeChecksum();

        //If this is not set then user cannot reopen the a receipt for re-check.
        //TODO When deleting historical receiptDocument make sure to remove this id from receipt referencing Document
        receipt.setReceiptOCRId(receiptDocument.getId());
        receipt.setRecheckComment(receiptDocument.getRecheckComment());
        receipt.setNotes(receiptDocument.getNotes());

        //This condition is mostly true for receipt when re-checked
        if(StringUtils.isNotEmpty(receiptDocument.getReceiptId())) {
            receipt.setId(receiptDocument.getReceiptId());
        }

		return receipt;
	}

	/**
	 *
	 * @param receipt - Required receipt with Id
	 * @return
	 * @throws ParseException
	 */
	public List<ItemEntity> getItemEntity(ReceiptEntity receipt) throws ParseException, NumberFormatException {
		List<ItemEntity> listOfItems = new ArrayList<>();

		for(ItemEntityOCR itemOCR : items) {
			if(itemOCR.getName().length() != 0) {
                String name = itemOCR.getName().trim();
                name = StringUtils.replace(name, "\t", " ");
                name = name.replaceAll("\\s+", " ");

                ItemEntity item = ItemEntity.newInstance();
                item.setName(WordUtils.capitalize(WordUtils.capitalizeFully(name),  '.', '(', ')'));
                item.setPrice(Formatter.getCurrencyFormatted(itemOCR.getPrice()).doubleValue());
                item.setQuantity(itemOCR.getQuantity());
                item.setTaxed(itemOCR.getTaxed());
                item.setSequence(itemOCR.getSequence());
                item.setReceipt(receipt);
                item.setUserProfileId(receipt.getUserProfileId());
				item.setExpenseTag(itemOCR.getExpenseTag());
                item.setCreated(itemOCR.getCreated());
				item.setUpdated();

                item.setBizName(receipt.getBizName());
				listOfItems.add(item);
			}
		}

		return listOfItems;
	}

    public MileageEntity getMileageEntity() {
        MileageEntity mileageEntity = new MileageEntity();
        mileageEntity.setUserProfileId(receiptDocument.getUserProfileId());
        mileageEntity.setStart(mileage.getStart());
        mileageEntity.setEnd(mileage.getEnd());
        mileageEntity.setStartDate(DateUtil.nowDate());
        return mileageEntity;
    }

    /**
     * Used for calculating individual item tax calculation
     *
     * @param items
     * @param receipt
     */
    public void updateItemWithTaxAmount(List<ItemEntity> items, ReceiptEntity receipt) {
        BigDecimal taxedItemTotalWithoutTax = BigDecimal.ZERO;

        for(ItemEntity item : items) {
            if(item.getTaxed() == TaxEnum.TAXED) {
                taxedItemTotalWithoutTax = Maths.add(taxedItemTotalWithoutTax, item.getTotalPriceWithoutTax());
            }
        }

        BigDecimal tax = Maths.calculateTax(receipt.getTax(), taxedItemTotalWithoutTax);
        if(tax.compareTo(BigDecimal.ZERO) == 0) {
            receipt.setPercentTax("0.0000");
        } else {
            receipt.setPercentTax(tax.toString());
        }

        for(ItemEntity item : items) {
            if(item.getTaxed() == TaxEnum.TAXED) {
                BigDecimal taxedAmount = Maths.multiply(item.getPrice().toString(), receipt.getPercentTax());
                item.setTax(new Double(taxedAmount.toString()));
            } else {
                item.setTax(0.00);
            }
        }

    }
}
