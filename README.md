receipt-mobile
==============

To Authenticate use following curl with your username and password. In test and prod only call made over secure protocol be supported

Local

	curl -i -X POST  -d mail=test@receiptofi.com -d password=test http://localhost:9090/receipt-mobile/j_spring_security_check

QA secure

	curl -ik -X POST -d mail=test@receiptofi.com -d password=test https://67.148.60.37:9443/receipt-mobile/j_spring_security_check

QA

    curl -i -X POST  -d mail=test@receiptofi.com -d password=test http://67.148.60.37:9090/receipt-mobile/j_spring_security_check


All API call should have the user and auth key in the header
To query use following curl (replace XXX with valid user id and auth key)

	curl -i -X GET -H "X-R-MAIL: XXX" -H "X-R-AUTH: XXX" http://localhost:9090/receipt-mobile/api/haveAccess.json
	curl -i -X GET -H "X-R-MAIL: test@receiptofi.com" -H "X-R-AUTH: 88a3ddadbb709a5284c58255845a5af59a49c01b"  http://localhost:9090/receipt-mobile/api/version.json
