<%@ page import="org.opensaml.saml2.core.AttributeStatement" %>
<%@ page import="java.util.List" %>
<%@ page import="org.opensaml.saml2.core.Attribute" %>
<%@ page import="org.opensaml.xml.XMLObject" %>
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
  <title>Service Provider User Page</title>
</head>
<body>

<pre>
___  ___        _  _
|  \/  |       (_)(_)
| .  . | _   _  _  _  _ __    __ _
| |\/| || | | || || || '_ \  / _` |
| |  | || |_| || || || | | || (_| |
\_|  |_/ \__,_|| ||_||_| |_| \__,_|
              _/ |
             |__/

           Service Provider
</pre>

<h3>This page is secured. You must have the ROLE_USER authority to be here.</h3>

<a href="index.jsp">unprotected home page</a> <br/>
<a href="admin.jsp">protected admin page</a> <br/>
<a href="j_spring_security_logout">End your session with the Service Provider</a><br/>

<h3>The following attributes were present:</h3>

<dl id="assertionAttributes">
<%
  final List<AttributeStatement> attributeStatements = (List<AttributeStatement>)session.getAttribute("assertionAttributes");
  if (attributeStatements != null) {
    for (AttributeStatement attributeStatement : attributeStatements) {
      final List<Attribute> attributes = attributeStatement.getAttributes();
      for (Attribute attribute : attributes) {
        out.print("<dt style=\"font-weight: bold;\">");
        out.print(attribute.getName());
        out.print("</dt><dd id=\"" + attribute.getName() + "\">");
        final List<XMLObject> attributeValues = attribute.getAttributeValues();
        for (XMLObject attributeValue : attributeValues) {
          out.print(attributeValue.getDOM().getTextContent());
          out.print(" ");
        }
        out.print("</dd>");
      }
    }
  }
%>
</dl>

<h3>Your current Spring Security Credentials are:</h3>

<H4>Authentication Principal is: </H4>
<p><sec:authentication property="principal"></sec:authentication></p>

<H4>Authentication Credentials are: </H4>
<p><sec:authentication property="credentials"></sec:authentication></p>

<H4>Authentication Details are: </H4>
<p><sec:authentication property="details"></sec:authentication></p>

</body>
</html>
