<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <link rel='stylesheet' type='text/css' href='${pageContext.request.contextPath}/static/css/receipt.css'>

    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/static/images/circle-leaf-sized_small.png" />
    <link rel="shortcut icon" type="image/x-icon" href="${pageContext.request.contextPath}/static/images/circle-leaf-sized_small.png" />
</head>
<body>
<div class="wrapper">
    <img src="${pageContext.request.contextPath}/static/images/circle-leaf-sized_small.png" alt="receipt-o-fi logo" height="46px"/>
    <p>
        Mobile Receiptofi APIs;

        All request needs authorization.
    </p>
</div>
<div class="footer">
    <p>
        <a href="${pageContext.request.contextPath}/aboutus.html">About Us</a> -
        <a href="${pageContext.request.contextPath}/tos.html">Terms of Service</a>
    </p>
    <p>&copy; 2014 Receiptofi Inc. All Rights Reserved. (<fmt:message key="build.version" />)</p>
</div>
</body>
</html>