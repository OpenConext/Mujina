<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

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
<title>Service Provider Social Queries</title>
<link rel="stylesheet"
	href="<c:url value="/assets/bootstrap-2.0.2/css/bootstrap.css"/>"></link>
<link rel="stylesheet" href="<c:url value="/assets/css/main.css"/>"></link>
<script type="text/javascript"
	src="<c:url value="/assets/js/jquery-1.7.2.js"/>"></script>
<script type="text/javascript"
	src="<c:url value="/assets/bootstrap-2.0.2/js/bootstrap.js"/>"></script>
<script type="text/javascript"
	src="<c:url value="/assets/js/social-queries.js"/>"></script>
</head>
<body>
	<div class="page-header">
		<h1>
			<small>SURFconext API test framework</small>
		</h1>
	</div>
	<div class="row">
		<div class="span12 columns">
			<p>Play with the different OAuth versions and API scenario's and see the subsequent request and
				responses from the SURFconext OpenSocial API.</p>

		</div>
	</div>
	<div class="row">
		<div class="span6 columns">
			<div class="accordion" id="mainOptions">
				<div class="accordion-group">
					<div class="accordion-heading">
						<a class="accordion-toggle" data-toggle="collapse"
							data-parent="#mainOptions" href="#collapseZero"> 
							<span class="badge badge-info">0</span> API Settings </a>
					</div>
					<div id="collapseZero" class="accordion-body collapse in">
						<div class="accordion-inner">TODO: settings, oauth version, end-points, preferences</div>
					</div>
				</div>
				<div class="accordion-group">
					<div class="accordion-heading">
						<a class="accordion-toggle" data-toggle="collapse"
							data-parent="#mainOptions" href="#collapseOne"> 
							<span class="badge badge-info">1</span> Authorize SP application </a>
					</div>
					<div id="collapseOne" class="accordion-body collapse in">
						<div class="accordion-inner">TODO: enter authorization URL,
							redirect to showing request / response</div>
					</div>
				</div>
				<div class="accordion-group">
					<div class="accordion-heading">
						<a class="accordion-toggle" data-toggle="collapse"
							data-parent="#mainOptions" href="#collapseTwo"> <span class="badge badge-info">2</span> Exchange
							authorization code for tokens </a>
					</div>
					<div id="collapseTwo" class="accordion-body collapse">
						<div class="accordion-inner">TODO: show authorization code +
							tokens</div>
					</div>
				</div>
				<div class="accordion-group">
					<div class="accordion-heading">
						<a class="accordion-toggle" data-toggle="collapse"
							data-parent="#mainOptions" href="#collapseThree"> <span class="badge badge-info">3</span> Configure
							request to API </a>
					</div>
					<div id="collapseThree" class="accordion-body collapse">
						<div class="accordion-inner">TODO show input box for request
							url + examples, show request/ response</div>
					</div>
				</div>
			</div>
		</div>
		<div class="span6 columns">
			<div id="request">
				<div class="alert alert-info alert-http">HTTP Request</div>
				<pre class="prettyprint pre-scrollable pre-json">
					<c:out value="${requestInfo}"/>
				</pre>
			</div>
			<div id="response">
				<div class="alert alert-info alert-http">HTTP Response</div>
				<pre class="prettyprint pre-scrollable pre-json">
					<c:out value="${responseInfo}"/>
				</pre>
			</div>
		</div>
	</div>

</body>
</html>
