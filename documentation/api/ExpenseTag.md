## Expense Tag

There are three actions 

- Add Expense Tag
- Update Expense Tag
- Delete Expense Tag

### Add Expense Tag

API call `POST`. API path `/api/expenseTag/add.json`

Required `tagName` and `tagColor`

    curl -X "POST" "https://test.receiptofi.com/receipt-mobile/api/expenseTag/add.json" \
    	-H "X-R-AUTH: x0wvlBm" \
    	-H "X-R-MAIL: test@receiptofi.com" \
    	-d "{\"tagName\":\"DADA\",\"tagColor\":\"CECECE\",\"tagIcon\":\"V101\"}"
    	
Failure response when incorrect data is sent

    HTTP/1.1 200 OK
    Server: Apache-Coyote/1.1   

    {
      "error": {
        "reason": "Either Expense Tag or Color received as empty.",
        "systemErrorCode": "100",
        "systemError": "USER_INPUT"
      }
    }
    
Success Response   

    HTTP/1.1 200 OK
    Server: Apache-Coyote/1.1
 
    {
      "billing": null,
      "coupons": [],
      "expenseTags": [
          {
            "color": "#713BE6",
            "d": false,
            "ic": "V100",
            "id": "57f753a3adaa966bf76b4d14",
            "tag": "AAAA"
          },
          {
            "color": "#68bb05",
            "d": true,
            "ic": "V100",
            "id": "57008aeef4a3b6a4d06a232d",
            "tag": "BIN 1"
          }
      ],
      "items": [],
      "notifications": [],
      "profile": null,
      "receipts": [],
      "unprocessedDocuments": {
        "unprocessedCount": 1
      }
    }

Besides this there is regular authentication passed `X-R-MAIL` and `X-R-AUTH`
    
### Update Expense Tag

API call `POST`. API path `/api/expenseTag/update.json`

Required `tagName` and `tagColor` and `tagId`

    curl -X "POST" "https://test.receiptofi.com/receipt-mobile/api/expenseTag/update.json" \
    	-H "X-R-AUTH: $2a$15$e2kRPwglBm" \
    	-H "X-R-MAIL: test@receiptofi.com" \
    	-d "{\"tagName\":\"DADA\",\"tagColor\":\"CCC\",\"tagId\":\"57fa1087adaa96163e769d8e\",\"tagIcon\":\"V102\"}"
    	
Failure Response 

    HTTP/1.1 200 OK
    Server: Apache-Coyote/1.1
    	
    {
      "error": {
        "reason": "Expense Tag does not exists.",
        "systemErrorCode": "500",
        "systemError": "SEVERE"
      }
    }
    
Failure Response when missing required field

    HTTP/1.1 200 OK
    Server: Apache-Coyote/1.1
    
    {
      "error": {
        "reason": "Either Expense Tag or Color or Id received as empty.",
        "systemErrorCode": "500",
        "systemError": "SEVERE"
      }
    }       

Success Response 

    HTTP/1.1 200 OK
    Server: Apache-Coyote/1.1

    {
      "billing": null,
      "expenseTags": [
        {
          "color": "#68bb05",
          "d": true,
          "ic": "V100",
          "id": "57008aeef4a3b6a4d06a232d",
          "tag": "BIN 1"
        },
        {
          "color": "#274B94",
          "d": false,
          "ic": "V100",
          "id": "55bb4574f4a3b6188e15c7f2",
          "tag": "BUSINESS"
        }
      ],
      "items": [],
      "notifications": [],
      "profile": null,
      "receipts": [],
      "unprocessedDocuments": {
        "unprocessedCount": 1
      }
    }
 
Besides this there is regular authentication passed `X-R-MAIL` and `X-R-AUTH`

### Delete Expense Tag

API call `POST`. API path `/api/expenseTag/delete.json`

Required `tagName` and `tagId`

    curl -X "POST" "https://test.receiptofi.com/receipt-mobile/api/deleteExpenseTag.json" \
    	-H "X-R-AUTH: $2a$15$e2kRPwgvlBm" \
    	-H "X-R-MAIL: test@receiptofi.com" \
    	-d "{\"tagName\":\"DADAB\",\"tagId\":\"553b68d1bd2898546e65ede4\"}"

Failure response when incorrect data is sent

    HTTP/1.1 200 OK
    Server: Apache-Coyote/1.1 
    
    {
      "error": {
        "reason": "Expense Tag does not exists.",
        "systemErrorCode": "500",
        "systemError": "SEVERE"
      }
    }
    
Success Response  
 
    HTTP/1.1 200 OK
    Server: Apache-Coyote/1.1

    {
      "billing": null,
      "expenseTags": [
        {
          "color": "#0D060D",
          "id": "54cc7fdbd4c6bde31a31c978",
          "tag": "BUSINESS",
          "d": false
        },
        {
          "color": "#E625C9",
          "id": "54cd6896d4c6e568298c9dc1",
          "tag": "HOME",
          "d": false
        },
        {
          "color": "#463BE6",
          "id": "550cd248036487b4351ab869",
          "tag": "QWE",
          "d": false
        }
      ],
      "items": [],
      "notifications": [],
      "profile": null,
      "receipts": [],
      "unprocessedDocuments": {
        "unprocessedCount": 1
      }
    }

Besides this there is regular authentication passed `X-R-MAIL` and `X-R-AUTH` 
 
