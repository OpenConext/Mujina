<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

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
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>Mujina IdP User Page</title>
</head>
<body>

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

<h1>Identity Provider User Page</h1>

<h3>This page is secured. You must have the ROLE_USER authority to be here.</h3>

<a href="index.jsp">unprotected IDP home page</a> <br/>
<a href="admin.jsp">protected IDP admin page</a> <br/>
<a href="j_spring_security_logout">End your session with the Identity Provider</a> <i>Does not end your session with the
  SP</i> <br/>

<h3>Your current Spring Security Credentials are:</h3>

<H4>Authentication Principal is: </H4>

<p><sec:authentication property="principal"></sec:authentication></p>
<H4>Authentication Credentials are: </H4>

<p><sec:authentication property="credentials"></sec:authentication></p>
<H4>Authentication Details are: </H4>

<p><sec:authentication property="details"></sec:authentication></p>

</body>
</html>
