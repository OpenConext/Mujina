<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>

<%--
  Copyright 2012 SURFnet bv, The Netherlands

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  --%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <title>Mujina Login Page</title>
</head>
<body onload='document.f.j_username.focus();'>

<pre style="front-weight: bold;">
___  ___        _  _
|  \/  |       (_)(_)
| .  . | _   _  _  _  _ __    __ _
| |\/| || | | || || || '_ \  / _` |
| |  | || |_| || || || | | || (_| |
\_|  |_/ \__,_|| ||_||_| |_| \__,_|
              _/ |
             |__/

          Identity Provider
</pre>

<c:if test="${not empty SPRING_SECURITY_LAST_EXCEPTION }">
  <p><font color='red'>Your login attempt was not successful, try again.<br/><br/>Reason: <c:out
      value="${SPRING_SECURITY_LAST_EXCEPTION.message}"/></font></p>
</c:if>

<h3>Login with Username and Password</h3>

<form name='login' action='/idp/j_spring_security_check' method='POST'>
  <table>
    <tr>
      <td>User:</td>
      <td><input type='text' name='j_username' value=''></td>
    </tr>
    <tr>
      <td>Password:</td>
      <td><input type='password' name='j_password'/></td>
    </tr>
    <tr>
      <td colspan='2'><input name="submit" type="submit"/></td>
    </tr>
    <tr>
      <td colspan='2'><input name="reset" type="reset"/></td>
    </tr>
  </table>

</form>
</body>
</html>