receipt-mobile
==============

There two ways to test through command line
- <code>curl</code> using mac
- <code>httpie</code> on windows or mac. https://github.com/jakubroztocil/httpie and helpful command can be found https://gist.github.com/BlakeGardner/5586954

##User Authentication##
____________

Use following <code>curl</code> or <code>httpie</code> with your <code>username</code> and <code>password</code>. 
Note: Application should make secure HTTPS calls, only HTTPS calls will be supported and responded. Any other call will get exception.

QA Secure login for getting <code>X-R-AUTH</code> code from user's account

    curl -ik -X POST -d mail=test@receiptofi.com -d password=test https://67.148.60.37:9443/receipt-mobile/j_spring_security_check

HTTP Response Header

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

Values from <code>X-R-MAIL</code> and <code>X-R-AUTH</code> has to be supplied in http header with every API call that gets invoked through mobile application.<br>
Note: <code>X-R-AUTH</code> code has to be encoded before sending in with the header. Any Java encoding API should help. For testing go to the site http://www.url-encode-decode.com/ for encoding <code>X-R-AUTH</code> string

Note: <code>X-R-AUTH</code> code needs to be encoded by going to site http://www.url-encode-decode.com/;

    Decoded X-R-AUTH code:  $2a$15$x9M5cc3mR24Ns4wgL47gaut/3.pM2tW9J.0SWeLroGbi2q8OU2k4C
    Encoded X-R-AUTH code:  %242a%2415%24x9M5cc3mR24Ns4wgL47gaut%2F3.pM2tW9J.0SWeLroGbi2q8OU2k4C


##API Call##
________

###Check if site is up and running###

Following call will make sure if site is up and running

    curl -ik -X GET https://67.148.60.37:9443/receipt-mobile/healthCheck.json
    
HTTP Response success

    HTTP/1.1 200 OK
    Server: Apache-Coyote/1.1
    ......
    
HTTP Body

    {"working":true}

If there is no response then site is not working. This call should return a response very quickly. 

###Check if user has access###

All API call should have the <code>X-R-MAIL</code> and <code>X-R-AUTH</code> in http header.
To query use following curl or http (replace XXX with valid user id and auth key)
Example

Check if user has access using <code>X-R-AUTH</code> code

    curl -i -X GET -H "X-R-MAIL: XXX" -H "X-R-AUTH: XXX" http://localhost:9090/receipt-mobile/api/hasAccess.json
    curl -ik -X GET -H "X-R-MAIL: test@receiptofi.com" -H "X-R-AUTH: %242a%2415%24x9M5cc3mR24Ns4wgL47gaut%2F3.pM2tW9J.0SWeLroGbi2q8OU2k4C" https://67.148.60.37:9443/receipt-mobile/api/hasAccess.json
    http GET http://localhost:9090/receipt-mobile/api/hasAccess.json X-R-MAIL:test@receiptofi.com X-R-AUTH:%242a%2415%24x9M5cc3mR24Ns4wgL47gaut%2F3.pM2tW9J.0SWeLroGbi2q8OU2k4C
    
HTTP Header response when success

    HTTP/1.1 200 OK
    Server: Apache-Coyote/1.1
    ......
    
HTTP body response when success
    
    {"access":"granted"}

HTTP Header response when access denied **HTTP/1.1 401 Unauthorized**

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

    
