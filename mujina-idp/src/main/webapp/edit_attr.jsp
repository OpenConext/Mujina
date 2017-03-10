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
  <title>Mujina</title>
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
<h1>Identity Provider Attribute Editor</h1>
<div id="messages"></div>
<div id="cookie_value"></div>
<hr>
<form id="cookie_form">
<table>
<tr>
<th>key</th><th>value</th>
</tr>
<tr>
<td><select name="select_attr_key"></select></td><td><input type="text" size="50" name="new_attr_value"></td>
</tr>
<tr>
<td colspan="3" align="right"><input type="button" value="add" onclick="addEntry();"><input type="button" value="save" onclick="saveCookie();"></td>
</tr>
</table>
</form>
<hr>
&nbsp;--&nbsp;<a href="#" onclick="removeCookie()">Remove Entire Cookie</a>&nbsp;--&nbsp;<a href="/index.jsp">Back to Home</a>&nbsp;--&nbsp;
<script src="js/jquery-2.0.3.min.js"></script>
<script src="js/jquery.cookie.js"></script>
<script src="available_attributes.json"></script>
<script>
var new_cookie_value = {};

function displayCookie() {
	 $("#cookie_value").html('');
	 $.each(new_cookie_value, function(key, value) {
	      $("#cookie_value").append(key+'='+value+"&nbsp;<a href='#' onclick='removeEntry(\""+key+"\")'>X</a><br>");
	 });
};

function addEntry() {
	  var key = $("select[name='select_attr_key']").val();
	  var value = $("input[name='new_attr_value']").val();
	  $("input[name='new_attr_value']").val('');
	  if (new_cookie_value[key]) {
         new_cookie_value[key].push(value);
      } else {
        new_cookie_value[key] = [value];
      }

	  displayCookie();
};

function removeEntry(key) {
	delete new_cookie_value[key];
	displayCookie();
};

function saveCookie() {
	$.cookie.json = true;
	$.cookie("mujina-attr", new_cookie_value);
	$("#messages").fadeIn(0);
	$("#messages").html("cookie saved...");
	$("#messages").delay(400).fadeOut(400);
	displayCookie();
};

function removeCookie() {
  $.removeCookie("mujina-attr");
  new_cookie_value = {};
  $("#messages").fadeIn(0);
  $("#messages").html("completely removed cookie");
  $("#messages").delay(400).fadeOut(400);
  displayCookie();
};

$(document).ready(function() {
	// check for cookie
	var cookie = $.cookie("mujina-attr");
	if (null == cookie) {
		$("#cookie_value").html("cookie is not found, start filling it below");
	} else {
	 new_cookie_value = JSON.parse(cookie);
	 displayCookie();
	}

	//build up the select of known attributes
	$.each(available_attributes, function(key, value) {
		$("select[name='select_attr_key']").append("<option value='"+key+"'>"+value.desc+" -- "+key+"</option>");
	});
});
</script>
</body>
</html>
