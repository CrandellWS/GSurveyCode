<#-- @ftlvariable name="" type="views.LoginView" -->
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>GameSurvey 2.0 Beta</title>
    <!-- Tell the browser to be responsive to screen width -->
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <!-- Bootstrap 3.3.5 -->
    <link rel="stylesheet" href="${basePath}assets/lib/bootstrap/css/bootstrap.min.css">
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.4.0/css/font-awesome.min.css">
    <!-- Ionicons -->
    <link rel="stylesheet" href="https://code.ionicframework.com/ionicons/2.0.1/css/ionicons.min.css">
    <!-- Theme style -->
    <link rel="stylesheet" href="${basePath}assets/adminlte/css/AdminLTE.min.css">
    <!-- AdminLTE Skins. Choose a skin from the css/skins
         folder instead of downloading all of them to reduce the load. -->
    <link rel="stylesheet" href="${basePath}assets/adminlte/css/skins/skin-gs.css">

    <script src="${basePath}assets/lib/dom/dom.js"></script>
    <script src="${basePath}assets/lib/fetch.js"></script>
    <link rel="stylesheet" href="${basePath}assets/lib/animate/animate.min.css">

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/html5shiv/3.7.3/html5shiv.min.js"></script>
    <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
</head>

<body class="hold-transition register-page">
<div class="register-box">
    <div class="register-logo">
        <img src="${basePath}assets/Gamesurvey-Logo-Transparent-1.png" style="width: 100%"></img>
    </div>

    <div class="register-box-body">
        <p class="login-box-msg">Register a new membership</p>

        <form id="registerForm" method="post">
            <div class="form-group has-feedback">
                <input id="name" type="text" name="name" class="form-control" placeholder="Full name" required>
                <span class="glyphicon glyphicon-user form-control-feedback"></span>
            </div>
            <div class="form-group has-feedback">
                <input id="email" type="email" name="email" class="form-control" placeholder="Email" required>
                <span class="glyphicon glyphicon-envelope form-control-feedback"></span>
            </div>
            <div class="row">
                <div class="col-xs-8">
                </div>
                <!-- /.col -->
                <div class="col-xs-4">
                    <button id="submit" type="submit" class="btn btn-primary btn-block btn-flat">Register</button>
                </div>
                <!-- /.col -->
            </div>
        </form>
        <div id="other">
                <a href="login" class="text-center">I already have a membership</a>
        </div>
    </div>
    <!-- /.form-box -->
</div>
<!-- /.register-box -->
</body>
<script>

    onload = function () {
        var name = document.getElementById("name");
        var email = document.getElementById("email");
        var submit = document.getElementById("submit");
        email.oninput = function(){submit.disabled=false};
        email.onpropertychange = email.oninput; // for IE8

        var form = document.getElementById('registerForm');
        form.addEventListener("submit", function (evt) {
            var other = document.getElementById('other')
            submit.disabled = true;

            var form = document.getElementById('registerForm');
            fetch('${basePath}register', {
                method: 'post',
                headers: {
                    'Accept': 'text/html',
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    name: name.value,
                    email: email.value
                })
            }).then(function (response) {
                return response.text()
            }).then(function (body) {
                if (body.length != 0) {
                    other.innerHTML = body;
                }
                else {
                    other.innerHTML = "Mail sent, please check your inbox.<br/>" +
                            "If you do not see the email in your Inbox please also check your Spam folder.";
                    email.disabled = true;
                    name.disabled = true;
                }
                submit.disabled = true;
            });
            evt.preventDefault();
        });
    };

</script>
</html>