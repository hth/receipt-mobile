package com.receiptofi.mobile.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import com.receiptofi.domain.CommentEntity;
import com.receiptofi.domain.ReceiptEntity;
import com.receiptofi.domain.SplitExpensesEntity;
import com.receiptofi.domain.json.JsonFriend;
import com.receiptofi.domain.json.JsonReceiptSanitized;
import com.receiptofi.domain.json.JsonReceiptSplit;
import com.receiptofi.domain.types.CommentTypeEnum;
import com.receiptofi.domain.types.SplitActionEnum;
import com.receiptofi.mobile.domain.AvailableAccountUpdates;
import com.receiptofi.mobile.repository.ReceiptManagerMobile;
import com.receiptofi.mobile.util.Util;
import com.receiptofi.service.CommentService;
import com.receiptofi.service.FriendService;
import com.receiptofi.service.ItemService;
import com.receiptofi.service.ReceiptService;
import com.receiptofi.service.SplitExpensesService;
import com.receiptofi.service.UserProfilePreferenceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * User: hitender
 * Date: 4/5/15 2:02 PM
 */
@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
@Service
public class ReceiptMobileService {
    private static final Logger LOG = LoggerFactory.getLogger(ReceiptMobileService.class);

    private static final int SIZE_1 = 1;
    private static final int LIMIT_SIZE = 25;
    private static Cache<String, List<JsonReceiptSanitized>> recentReceipts = CacheBuilder.newBuilder()
            .maximumSize(SIZE_1)
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .build();

    @Autowired private ReceiptService receiptService;
    @Autowired private CommentService commentService;
    @Autowired private DocumentMobileService documentMobileService;
    @Autowired private ReceiptManagerMobile receiptManagerMobile;
    @Autowired private ItemService itemService;
    @Autowired private SplitExpensesService splitExpensesService;
    @Autowired private FriendService friendService;
    @Autowired private UserProfilePreferenceService userProfilePreferenceService;

    public ReceiptEntity findReceipt(String receiptId, String rid) {
        return receiptService.findReceipt(receiptId, rid);
    }

    public ReceiptEntity findReceiptForMobile(String receiptId, String rid) {
        return receiptService.findReceiptForMobile(receiptId, rid);
    }

    public void updateReceiptExpenseTag(ReceiptEntity receipt, String expenseTagId) {
        receiptService.updateReceiptExpenseTag(receipt, expenseTagId);
    }

    /**
     * Saves the comment and updates receipts with comment.
     *
     * @param notes
     * @param receipt
     */
    public void saveComment(String notes, ReceiptEntity receipt) {
        CommentEntity comment = receipt.getNotes();
        if (null == comment) {
            comment = CommentEntity.newInstance(CommentTypeEnum.NOTES);
            comment.setText(notes);
        } else {
            comment.setText(notes);
        }
        commentService.save(comment);
        receipt.setNotes(comment);
        receiptService.save(receipt);
    }

    public void recheck(String receiptId, String rid) throws Exception {
        receiptService.recheck(receiptId, rid);
    }

    public AvailableAccountUpdates getUpdateForChangedReceipt(ReceiptEntity receipt) {
        Assert.notNull(receipt, "ReceiptEntity should not be null");
        AvailableAccountUpdates availableAccountUpdates = AvailableAccountUpdates.newInstance();
        getReceiptAndItemUpdates(availableAccountUpdates, receipt.getReceiptUserId(), Collections.singletonList(receipt));
        documentMobileService.getUnprocessedDocuments(receipt.getReceiptUserId(), availableAccountUpdates);
        return availableAccountUpdates;
    }

    /**
     * Get all the receipt updates.
     * Fail safe mechanism.
     *
     * @param rid
     * @return
     */
    public AvailableAccountUpdates getUpdateForAllReceipt(String rid) {
        List<ReceiptEntity> receipts = getAllReceipts(rid);
        AvailableAccountUpdates availableAccountUpdates = AvailableAccountUpdates.newInstance();
        getReceiptAndItemUpdates(availableAccountUpdates, rid, receipts);
        documentMobileService.getUnprocessedDocuments(rid, availableAccountUpdates);
        return availableAccountUpdates;
    }

    /**
     * Do not use this open end query.
     *
     * @param profileId
     * @return
     */
    public List<ReceiptEntity> getAllReceipts(String profileId) {
        return receiptManagerMobile.getAllReceipts(profileId);
    }

    public List<ReceiptEntity> getAllUpdatedReceiptSince(String profileId, Date since) {
        return receiptManagerMobile.getAllUpdatedReceiptSince(profileId, since);
    }

    public boolean deleteReceipt(String receiptId, String rid) {
        return receiptService.deleteReceipt(receiptId, rid);
    }

    /**
     * Gets item updates for the set of receipts.
     *
     * @param availableAccountUpdates
     * @param rid
     * @param receipts
     */
    public void getReceiptAndItemUpdates(
            AvailableAccountUpdates availableAccountUpdates,
            String rid,
            List<ReceiptEntity> receipts
    ) {
        //TODO match with ReceiptController.loadForm
        if (!receipts.isEmpty()) {
            availableAccountUpdates.addJsonReceipts(receipts);
            Map<String, JsonFriend> jsonFriendMap;

            for (ReceiptEntity receipt : receipts) {
                String fetchReceiptId = null == receipt.getReferReceiptId() ? receipt.getId() : receipt.getReferReceiptId();
                availableAccountUpdates.addJsonReceiptItems(itemService.getAllItemsOfReceipt(fetchReceiptId));

                if (null == receipt.getReferReceiptId()) {
                    /** Refers to original user accessing original receipt. */
                    jsonFriendMap = friendService.getFriends(rid);

                    if (receipt.getSplitCount() > 1) {
                        JsonReceiptSplit jsonReceiptSplit = new JsonReceiptSplit();

                        jsonReceiptSplit.setReceiptId(receipt.getId());
                        jsonReceiptSplit.setSplits((splitExpensesService.populateProfileOfFriends(
                                fetchReceiptId,
                                jsonFriendMap
                        )));

                        LOG.debug("Found split count={}", receipt.getSplitCount());
                        availableAccountUpdates.addJsonReceiptSplits(jsonReceiptSplit);
                    }
                } else {
                    /** Refers to split user accessing shared receipt. */
                    ReceiptEntity originalReceipt = receiptManagerMobile.findReceipt(receipt.getReferReceiptId());
                    jsonFriendMap = friendService.getFriends(originalReceipt.getReceiptUserId());

                    JsonReceiptSplit jsonReceiptSplit = new JsonReceiptSplit()
                            .setReceiptId(receipt.getId())
                            .setSplits((splitExpensesService.populateProfileOfFriends(
                                    fetchReceiptId,
                                    jsonFriendMap
                            )));

                    jsonReceiptSplit.getSplits().remove(new JsonFriend(rid, "", ""));
                    jsonReceiptSplit.addSplit(new JsonFriend(userProfilePreferenceService.findByReceiptUserId(originalReceipt.getReceiptUserId())));

                    availableAccountUpdates.addJsonReceiptSplits(jsonReceiptSplit);
                }
            }
        }
    }

    public void executeSplit(String fidAdd, String receiptId, ReceiptEntity receipt) {
        List<SplitExpensesEntity> splitExpenses = splitExpensesService.getSplitExpensesFriendsForReceipt(receiptId);
        for (SplitExpensesEntity splitExpense : splitExpenses) {
            LOG.debug("{} fid={}", SplitActionEnum.R, splitExpense.getFriendUserId());
            receiptService.splitAction(splitExpense.getFriendUserId(), SplitActionEnum.R, receipt);
        }

        List<String> addFids = Util.convertCommaSeparatedStringToList(fidAdd);
        for (String friendId : addFids) {
            LOG.debug("{} fid={}", SplitActionEnum.A, friendId);
            receiptService.splitAction(friendId, SplitActionEnum.A, receipt);
        }
    }

    /**
     * Gets one of the recently processed receipt.
     *
     * @return
     */
    public JsonReceiptSanitized getRecentReceipts() {
        int random = new Random().nextInt(LIMIT_SIZE);
        JsonReceiptSanitized jsonReceiptSanitized = new JsonReceiptSanitized();
        try {
            if (recentReceipts.getIfPresent("RECENT_RECEIPTS") == null) {
                List<JsonReceiptSanitized> jsonReceipts = getRecentReceipts(LIMIT_SIZE);
                recentReceipts.put("RECENT_RECEIPTS", jsonReceipts);
                jsonReceiptSanitized = jsonReceipts.get(random);
            } else {
                jsonReceiptSanitized = recentReceipts.getIfPresent("RECENT_RECEIPTS").get(random);
            }
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            LOG.error("Failed to find receipt at random location={} in list size={}", random, recentReceipts.size());
        }

        return jsonReceiptSanitized;
    }

    private List<JsonReceiptSanitized> getRecentReceipts(int limit) {
        return receiptManagerMobile.getRecentReceipts(limit).stream().map(JsonReceiptSanitized::new).collect(Collectors.toList());
    }
}
