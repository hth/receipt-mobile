package com.receiptofi.web.controller.access;

import com.receiptofi.domain.ExpenseTagEntity;
import com.receiptofi.domain.ItemEntity;
import com.receiptofi.domain.site.ReceiptUser;
import com.receiptofi.service.ExpensesService;
import com.receiptofi.service.ItemService;
import com.receiptofi.utils.DateUtil;
import com.receiptofi.utils.PerformanceProfiling;
import com.receiptofi.web.form.ExpenseForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
 * Lists out expenses related items. Call made from Pie chart on Tab 2
 *
 * User: hitender
 * Date: 5/23/13
 * Time: 11:28 PM
 */
@Controller
@RequestMapping(value = "/access/expenses")
public final class ExpensesController {
    private static final Logger log = LoggerFactory.getLogger(ExpensesController.class);
    private static final String nextPage = "/expenses";

    @Autowired private ItemService itemService;
    @Autowired private ExpensesService expensesService;

    @RequestMapping(value = "{tag}", method = RequestMethod.GET)
    public ModelAndView forExpenseType(@PathVariable String tag, @ModelAttribute("expenseForm") ExpenseForm expenseForm) {
        DateTime time = DateUtil.now();
        ReceiptUser receiptUser = (ReceiptUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<ExpenseTagEntity> expenseTypes = expensesService.activeExpenseTypes(receiptUser.getRid());
        List<ItemEntity> items = new ArrayList<>();

        if(!tag.equalsIgnoreCase("Un-Assigned")) {
            for(ExpenseTagEntity expenseTagEntity : expenseTypes) {
                if(expenseTagEntity.getTagName().equalsIgnoreCase(tag)) {
                    items = itemService.itemsForExpenseType(expenseTagEntity);
                    break;
                }
            }
        } else if(tag.equalsIgnoreCase("Un-Assigned")) {
            items = itemService.itemsForUnAssignedExpenseType(receiptUser.getRid());
        }

        expenseForm.setName(tag);
        expenseForm.setExpenseTags(expenseTypes);
        expenseForm.setItems(items);

        ModelAndView modelAndView = new ModelAndView(nextPage);
        modelAndView.addObject("expenseForm", expenseForm);

        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());
        return modelAndView;
    }
}
