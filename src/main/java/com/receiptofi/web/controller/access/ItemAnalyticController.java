/**
 *
 */
package com.receiptofi.web.controller.access;

import com.receiptofi.domain.ExpenseTagEntity;
import com.receiptofi.domain.ItemEntity;
import com.receiptofi.domain.site.ReceiptUser;
import com.receiptofi.service.ExpensesService;
import com.receiptofi.service.ItemAnalyticService;
import com.receiptofi.utils.DateUtil;
import com.receiptofi.utils.PerformanceProfiling;
import com.receiptofi.web.form.ItemAnalyticForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import org.joda.time.DateTime;

/**
 * @author hitender
 * @since Jan 9, 2013 10:23:55 PM
 *
 */
@Controller
@RequestMapping(value = "/access/itemanalytic")
public final class ItemAnalyticController {
	private static final Logger log = LoggerFactory.getLogger(ItemAnalyticController.class);
	private static final String nextPage = "/itemanalytic";

    private static final int NINETY_DAYS = 90;

	@Autowired private ItemAnalyticService itemAnalyticService;
    @Autowired private ExpensesService expensesService;

	@RequestMapping(value = "{id}", method = RequestMethod.GET)
	public ModelAndView loadForm(@PathVariable String id, @ModelAttribute("itemAnalyticForm") ItemAnalyticForm itemAnalyticForm) {
        DateTime time = DateUtil.now();
        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        ItemEntity item = itemAnalyticService.findItemById(id, receiptUser.getRid());
        if(item != null) {
            itemAnalyticForm.setItem(item);
            itemAnalyticForm.setDays(NINETY_DAYS);

            DateTime untilThisDay = DateTime.now().minusDays(NINETY_DAYS);
            if(item.getReceipt().getReceiptDate().before(untilThisDay.toDate())) {
                itemAnalyticForm.setMessage("Since the item " + item.getName() + " was purchased more than " + NINETY_DAYS + " days ago no average could be calculated.");
            }

            //TODO make sure a duplicate is reported when user uploads a new receipt and the old deleted receipt still existing with same information
            //so comparing is essential and its better to remove the duplicate

            /** Gets site average */
            List<ItemEntity> siteAverageItems = itemAnalyticService.findAllByNameLimitByDays(item.getName(), untilThisDay);
            itemAnalyticForm.setSiteAverageItems(siteAverageItems);

            BigDecimal siteAveragePrice = itemAnalyticService.calculateAveragePrice(siteAverageItems);
            itemAnalyticForm.setSiteAveragePrice(siteAveragePrice);

            /** Your average */
            List<ItemEntity> yourAverageItems = itemAnalyticService.findAllByNameLimitByDays(item.getName(), receiptUser.getRid(), untilThisDay);
            itemAnalyticForm.setYourAverageItems(yourAverageItems);

            BigDecimal yourAveragePrice = itemAnalyticService.calculateAveragePrice(yourAverageItems);
            itemAnalyticForm.setYourAveragePrice(yourAveragePrice);

            /** Users historical items */
            List<ItemEntity> yourItems = itemAnalyticService.findAllByName(item, receiptUser.getRid());
            itemAnalyticForm.setYourHistoricalItems(yourItems);

            /** Loads expense types */
            List<ExpenseTagEntity> expenseTypes = expensesService.activeExpenseTypes(item.getUserProfileId());
            itemAnalyticForm.setExpenseTags(expenseTypes);
        }

        ModelAndView modelAndView = new ModelAndView(nextPage);
        modelAndView.addObject("itemAnalyticForm", itemAnalyticForm);

        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());
        return modelAndView;
	}

}
