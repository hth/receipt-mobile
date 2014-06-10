receipt-mobile
==============

There two ways to test through command line
- curl on mac
- httpie https://gist.github.com/BlakeGardner/5586954

Below there are couple of examples using curl and httpie. For encoding password please use http://www.url-encode-decode.com/

**Authenticate**
____________

Use following curl or httpie with your username and password. 
Note: In test and prod environment only call made over **secure protocol** will be supported

Local

    curl -i -X POST  -d mail=test@receiptofi.com -d password=test http://localhost:9090/receipt-mobile/j_spring_security_check

QA secure

    curl -ik -X POST -d mail=test@receiptofi.com -d password=test https://67.148.60.37:9443/receipt-mobile/j_spring_security_check

QA

    curl -i -X POST  -d mail=test@receiptofi.com -d password=test http://67.148.60.37:9090/receipt-mobile/j_spring_security_check

Example of response

    HTTP/1.1 200 OK
    Server: Apache-Coyote/1.1
    Cache-Control: no-cache, no-store, max-age=0, must-revalidate
    Pragma: no-cache
    Expires: 0
    Strict-Transport-Security: max-age=31536000 ; includeSubDomains
    X-XSS-Protection: 1; mode=block
    X-Frame-Options: DENY
    X-Content-Type-Options: nosniff
    X-R-MAIL: test@receiptofi.com
    X-R-AUTH: $2a$15$8Ny7RNsMAzzsTGxmJdKirOChLWidNO.scuMGoNl2n1G1bHEwXsoq.
    Set-Cookie: id="test@receiptofi.com|$2a$15$8Ny7RNsMAzzsTGxmJdKirOChLWidNO.scuMGoNl2n1G1bHEwXsoq."; Version=1; Domain=localhost; Max-Age=1814400; Expires=Tue, 01-Jul-2014 09:08:48 GMT; Path=/receipt-mobile
    Content-Length: 0
    Date: Tue, 10 Jun 2014 09:08:48 GMT

X-R-MAIL and X-R-AUTH needs to be saved locally and has to be supplied with http header in every call that gets invoked from app


**API Call**
________

All API call should have the MAIL and AUTH in http header.
To query use following curl or http (replace XXX with valid user id and auth key)

    curl -i -X GET -H "X-R-MAIL: XXX" -H "X-R-AUTH: XXX" http://localhost:9090/receipt-mobile/api/haveAccess.json


    curl -i -X GET -H "X-R-MAIL: test@receiptofi.com" -H "X-R-AUTH: %242a%2415%248Ny7RNsMAzzsTGxmJdKirOChLWidNO.scuMGoNl2n1G1bHEwXsoq."  http://localhost:9090/receipt-mobile/api/hasAccess.json
    

    http GET http://localhost:9090/receipt-mobile/api/hasAccess.json X-R-MAIL:test@receiptofi.com X-R-AUTH:%242a%2415%248Ny7RNsMAzzsTGxmJdKirOChLWidNO.scuMGoNl2n1G1bHEwXsoq.
    
Secure 

    curl -ik -X GET -H "X-R-MAIL: test@receiptofi.com" -H "X-R-AUTH: %242a%2415%248Ny7RNsMAzzsTGxmJdKirOChLWidNO.scuMGoNl2n1G1bHEwXsoq."  https://67.148.60.37:9443/receipt-mobile/api/hasAccess.json
    

Note: X-R-AUTH code needs to be encoded by going to site http://www.url-encode-decode.com/;
Decoded auth code is    $2a$15$8Ny7RNsMAzzsTGxmJdKirOChLWidNO.scuMGoNl2n1G1bHEwXsoq.
