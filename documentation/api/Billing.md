



### Get Billing Plan

    http://localhost:9090/receipt-mobile/api/plans.json
    
    curl -X "GET" "http://localhost:9090/receipt-mobile/api/plans.json" 
    	-H "X-R-AUTH: $2a$15$e2kR" 
    	-H "X-R-MAIL: test@receiptofi.com"

    [{"billingDayOfMonth":25,"billingFrequency":1,"description":"Process 10 receipts every month","id":"M10","name":"Monthly 10","price":2.00}]
    
    
### Payment

    http://localhost:9090/receipt-mobile/api/btp.json
    
    curl -X "POST" "http://localhost:9090/receipt-mobile/api/btp.json" \
    	-H "X-R-AUTH: $2a$15$e2kRPwg04Ld6W9u4WWwvTuYZdbUhf5PSz8BLtQCRzDRwP5x0wvlBm" \
    	-H "X-R-DID: 12347" \
    	-H "X-R-MAIL: test@receiptofi.com"
