<#-- @ftlvariable name="" type="views.DebugView" -->
<#import "adminlayout.ftl" as layout>
<@layout.layout>
<!DOCTYPE html>
<html>
<head>
    <title>Debug</title>
</head>
<body>
<form id="form" action="/debug/reset" method="post">
    <input type="submit" name="reset" value="reset">

</form>

</body>
</html>
</@layout.layout>