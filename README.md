receipt-mobile
==============

There two ways to test through command line
- curl on mac
- httpie https://gist.github.com/BlakeGardner/5586954

Below there are couple of examples using curl and httpie. 

**User Authentication**
____________

Use following curl or httpie with your username and password. 
Note: Call made over **secure protocol like HTTPS** is only supported.

QA Secure login for getting <code>X-R-AUTH</code> code from user's account

    curl -ik -X POST -d mail=test@receiptofi.com -d password=test https://67.148.60.37:9443/receipt-mobile/j_spring_security_check

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
    X-R-AUTH: $2a$15$x9M5cc3mR24Ns4wgL47gaut/3.pM2tW9J.0SWeLroGbi2q8OU2k4C
    Set-Cookie: id="test@receiptofi.com|$2a$15$x9M5cc3mR24Ns4wgL47gaut/3.pM2tW9J.0SWeLroGbi2q8OU2k4C"; Version=1; Domain=localhost; Max-Age=1814400; Expires=Sun, 06-Jul-2014 04:09:38 GMT; Path=/receipt-mobile
    Content-Length: 0
    Date: Sun, 15 Jun 2014 04:09:38 GMT

Values from *X-R-MAIL* and *X-R-AUTH* has to be supplied in http header with every API call that gets invoked in APP
Note: X-R-AUTH code has to be encoded before sending in with the header.


**API Call**
________

All API call should have the <code>X-R-MAIL</code> and <code>X-R-AUTH</code> in http header.
To query use following curl or http (replace XXX with valid user id and auth key)
Example

*Check if user has access using X-R-AUTH code*

    curl -i -X GET -H "X-R-MAIL: XXX" -H "X-R-AUTH: XXX" http://localhost:9090/receipt-mobile/api/hasAccess.json
    
    http GET http://localhost:9090/receipt-mobile/api/hasAccess.json X-R-MAIL:test@receiptofi.com X-R-AUTH:%242a%2415%24x9M5cc3mR24Ns4wgL47gaut%2F3.pM2tW9J.0SWeLroGbi2q8OU2k4C
    
    
    curl -ik -X GET -H "X-R-MAIL: test@receiptofi.com" -H "X-R-AUTH: %242a%2415%24x9M5cc3mR24Ns4wgL47gaut%2F3.pM2tW9J.0SWeLroGbi2q8OU2k4C" https://67.148.60.37:9443/receipt-mobile/api/hasAccess.json
    

If user has access then JSON Response <code>{"access":"granted"}</code><br>
If user is denied access then Response header is like below with *HTTP/1.1 401 Unauthorized*

    HTTP/1.1 401 Unauthorized
    Server: Apache-Coyote/1.1
    Cache-Control: no-cache, no-store, max-age=0, must-revalidate
    Pragma: no-cache
    Expires: 0
    Strict-Transport-Security: max-age=31536000 ; includeSubDomains
    X-XSS-Protection: 1; mode=block
    X-Frame-Options: DENY
    X-Content-Type-Options: nosniff
    Content-Type: text/html;charset=utf-8
    Content-Language: en
    Content-Length: 975
    Date: Sun, 15 Jun 2014 04:29:39 GMT
    

Note: <code>X-R-AUTH</code> code needs to be encoded by going to site http://www.url-encode-decode.com/;

    Decoded X-R-AUTH code:  $2a$15$x9M5cc3mR24Ns4wgL47gaut/3.pM2tW9J.0SWeLroGbi2q8OU2k4C
    Encoded X-R-AUTH code:  %242a%2415%24x9M5cc3mR24Ns4wgL47gaut%2F3.pM2tW9J.0SWeLroGbi2q8OU2k4C

*Check if site is working*

    
