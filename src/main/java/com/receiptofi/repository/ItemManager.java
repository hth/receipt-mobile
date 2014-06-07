/**
 *
 */
package com.receiptofi.repository;

import com.receiptofi.domain.ExpenseTagEntity;
import com.receiptofi.domain.ItemEntity;
import com.receiptofi.domain.ReceiptEntity;

import java.util.List;

import org.joda.time.DateTime;

import com.mongodb.WriteResult;

/**
 * @author hitender
 * @since Dec 26, 2012 3:10:11 PM
 *
 */
public interface ItemManager extends RepositoryManager<ItemEntity> {

	void saveObjects(List<ItemEntity> objects) throws Exception;

	WriteResult updateObject(ItemEntity object);

	List<ItemEntity> getWhereReceipt(ReceiptEntity receipt);

    /**
     * Finds users item as the session supplies userProfileId
     *
     * @param itemId
     * @param userProfileId
     * @return
     */
    ItemEntity findItem(String itemId, String userProfileId);

    /**
     * Gets items with specified name until the specified date
     *
     * @param name - Name of the item
     * @param untilThisDay - Show result from this day onwards
     * @return
     */
	List<ItemEntity> findAllByNameLimitByDays(String name, DateTime untilThisDay);

    /**
     * Gets items with specified name until the specified date
     *
     * @param name - Name of the item
     * @param userProfileId
     * @param untilThisDay - Show result from this day onwards
     * @return
     */
    List<ItemEntity> findAllByNameLimitByDays(String name, String userProfileId, DateTime untilThisDay);


    /**
     * Should be used only for listing historical data of the items for a particular user
     *
     * @param itemEntity
     * @param userProfileId
     * @return
     */
    List<ItemEntity> findAllByName(ItemEntity itemEntity, String userProfileId);

    /**
     * Delete Entity
     *
     * @param receipt
     */
	void deleteWhereReceipt(ReceiptEntity receipt);

    /**
     * Marks an entity deleted but does not delete it
     *
     * @param receipt
     */
    void deleteSoft(ReceiptEntity receipt);

    /**
     * Populate with just 'name' field based on first token. Used in AJAX call.
     *
     * @param name
     * @param bizName
     * @return
     */
    List<ItemEntity> findItems(String name, String bizName);

    /**
     * Get the Item from DB and then update with changed ExpenseType before persisting Item
     *
     * @param item
     */
    void updateItemWithExpenseType(ItemEntity item) throws Exception;

    /**
     * Count how many Items are using a particular Expense Type
     *
     * @param expenseTypeId
     * @return
     */
    long countItemsUsingExpenseType(String expenseTypeId, String userProfileId);

    List<ItemEntity> getItemEntitiesForSpecificExpenseTypeForTheYear(ExpenseTagEntity expenseType);

    List<ItemEntity> getItemEntitiesForUnAssignedExpenseTypeForTheYear(String userProfileId);
}