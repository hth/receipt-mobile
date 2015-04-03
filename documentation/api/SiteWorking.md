### Check if site is up and running ###

Following call will make sure if site is up and running

    curl -ik -X GET https://test.receiptofi.com/receipt-mobile/healthCheck.json

HTTP Response success

    HTTP/1.1 200 OK
    Server: Apache-Coyote/1.1
    ......

HTTP Body

    {"working":true}

If there is no response then site is not working. This call should return a response very quickly.