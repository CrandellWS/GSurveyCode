<#-- @ftlvariable name="" type="views.LoginView" -->
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>GameSurvey 2.0 Beta</title>
</head>
<body>
Logging out..
<script>
    window.fbAsyncInit = function() {
        FB.init({
            appId      : '${FBAppID}',
            cookie     : true,  // enable cookies to allow the server to access
                                // the session
            xfbml      : false,  // parse social plugins on this page
            version    : 'v2.7' // use graph api version 2.5
        });

        FB.getLoginStatus(function(response) {
            statusChangeCallback(response);
        });
    };

    function statusChangeCallback(response) {
        console.log('statusChangeCallback');
        console.log(response);
        setTimeout('top.location.href = "${basePath}"', 3000);
        FB.logout(function(){
            top.location.href = '${basePath}';
        });
    }

    // Load the SDK asynchronously
    (function(d, s, id) {
        var js, fjs = d.getElementsByTagName(s)[0];
        if (d.getElementById(id)) return;
        js = d.createElement(s); js.id = id;
        js.src = "//connect.facebook.net/en_US/sdk.js";
        fjs.parentNode.insertBefore(js, fjs);
    }(document, 'script', 'facebook-jssdk'));
</script>
</body>
</html>