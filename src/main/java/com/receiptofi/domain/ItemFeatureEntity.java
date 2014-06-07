/**
 *
 */
package com.receiptofi.domain;

import com.receiptofi.domain.types.FeaturesOnItemEnum;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Add various features to an existing Item. It could be like price check for next thirty days. Or add a return reminder on an item.
 *
 * @author hitender
 * @since Dec 26, 2012 1:47:36 PM
 *
 */
@Document(collection = "ITEM_FEATURE")
public final class ItemFeatureEntity extends BaseEntity {

	@DBRef
    @Field("ITEM")
	private ItemEntity itemEntity;

	private FeaturesOnItemEnum featureOnItem;

	private ItemFeatureEntity(FeaturesOnItemEnum featureOnItem, ItemEntity itemEntity) {
		super();
		this.featureOnItem = featureOnItem;
		this.itemEntity = itemEntity;
	}

	/**
	 * This method is used when the Entity is created for the first time.
	 *
	 * @param featureOnItem
	 * @param itemEntity
	 * @return
	 */
	public static ItemFeatureEntity newInstance(FeaturesOnItemEnum featureOnItem, ItemEntity itemEntity) {
		return new ItemFeatureEntity(featureOnItem, itemEntity);
	}

	public ItemEntity getItemEntity() {
		return itemEntity;
	}

	public FeaturesOnItemEnum getFeatureOnItem() {
		return featureOnItem;
	}

}
