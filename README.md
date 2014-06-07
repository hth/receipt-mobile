receipt-mobile
==============

To validate REST use following curl

Local

	curl -i -X POST -d j_username=user -d j_password=userPass http://localhost:8080/receipt-mobile/j_spring_security_check

QA secure

    curl -ik -X POST -d j_username=farooq@receiptofi.com -d j_password=zero https://67.148.60.37:9443/receipt-mobile/j_spring_security_check


QA

    curl -i -X POST -d j_username=farooq@receiptofi.com -d j_password=zero http://67.148.60.37:9090/receipt-mobile/j_spring_security_check

