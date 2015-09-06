<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/static/images/Receipt-46x46.png" />
    <link rel="shortcut icon" type="image/x-icon" href="${pageContext.request.contextPath}/static/images/Receipt-46x46.png" />

    <link href='//fonts.googleapis.com/css?family=Open+Sans:400,300|Merriweather' rel='stylesheet' type='text/css'>
    <link rel="stylesheet" href="//receiptofi.com/css/reset.css"> <!-- CSS reset -->
    <link rel="stylesheet" href="//receiptofi.com/css/style.css"> <!-- Resource style -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/stylelogin-nn.css"> <!-- Resource style -->
    <link rel="stylesheet" href="//cdnjs.cloudflare.com/ajax/libs/font-awesome/4.4.0/css/font-awesome.css">

    <title>Receiptofi | Receipt App to park your Receipts</title>
</head>
<body>
<section class="cd-fixed-background" style="background-color: #93a748" data-type="slider-item">
    <div class="cd-content">
        <fieldset class="cd-form floating-labels" id="login-title-fieldset">
            <h2>ReceiptApp<span class="tm">&#8482;</span> for receipts</h2>
            <p>Mobile ReceiptApp APIs</p>
            <p>All request needs authorization.</p>
        </fieldset>
    </div>
</section>
<div class="footer-container">
    <footer class="wrapper fine-print">
        &#169; 2015 Receiptofi, Inc. <a href="//receiptofi.com/termsofuse">Terms</a> and <a href="//receiptofi.com/privacypolicy">Privacy</a>.<br>
        All other trademarks and logos belong to their respective owners. (<fmt:message key="build.version" />)<br>
    </footer>
</div>
</body>
</html>