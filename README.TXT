This project contains two sample Java web applications: an Identity Provider
(IDP) and a Service Provider (SP).  The goal of this project is to show how Open
SAML can be integrated with Spring Security to provide a cross domain security
solution.  This does not use the Spring Security SAML extension project.  This
project is only a recipe, it was not meant provide a framework with re-usable
components. 

SAML is only a specification. It does mandate the answers to many questions that
will come up in any in any cross domain security solution.  How is trust
established between the Service Provider and Identity Provider?  What binding is
used to send SAML messages between the IDP and SP?  How will a Subject be
identified to a Service Provider?  What level of information does an Identity
Provider give to a Service about a Subject?  Does this include role/access
rights?  And many more that I can't think of off the top of my head... 

This solution is not meant to replace the functionality that is offered with a
fully featured Identity Provider (e.g Shibboleth, PingIdentiy).  It does not
offer the flexibility that these other solutions provide, but it does offer a
simple, SAML compliant solution that might be useful is some situations.
 


#############################################
#                                           # 
# Authentication Flow                       #
#                                           # 
#############################################

Requests to protected resources in the SP will result in a Signed AuthnRequest
to be sent to the IDP.

The IDP will use form based login to challenge users that have not yet
authenticated beyond a level of ROLE_ANONYMOUS.

Failed login attempts at the IDP will result with a signed AuthnResponse being
returned to the SP with a status code of
urn:oasis:names:tc:SAML:2.0:status:Responder 

Successful login attempts OR  users that still have a valid session at the IDP
will result with a signed AuthnResponse being returned to the SP with a status
code of urn:oasis:names:tc:SAML:2.0:status:Success.

The SP will convert successful AuthnResponses into a SAMLAuthenticationToken.
The principal and GrantedAuthorities are conveyed in the Assertion. 

Unsuccessful AuthnResponses will be logged and will return the user to the IDP
for a chance to successfully authenticate. 


#############################################
#                                           # 
#   Additional Features/Details             #
#                                           # 
#############################################


All SAML Requests/Response are made using the HTPPPostBack Binding.

Private/Public keys are stored in a JKS keystore whose contents have been Base
64 encoded for easy access by the credential manager.  The keystores are
included with the source to allow for modification/inspection.  All passwords
are 'secret'.

The digital signatures of the Request and Response are verified against the
senders public key.

In addition to the signature check, there are two additional Open SAML Security
Policy Checks: IssueInstant and MessageReplay.  IssueInstant insures that a
message is only valid for a narrow window of time.  MessageReplay prevents the
same Message from being processed multiple times.

The IDP allows for direct access through form based login.  That is, it supports
users accessing it directly as an application as well as providing SSO services
for an SP. 

There are several interesting integration tests that are included with the
projects. They produce/consume SAML messages and output to the log.


#############################################
#                                           # 
# Deploying the pre-built WARs.             #
#                                           # 
#############################################

You should be able to deploy both applications by updating the
application.properties file contained in each WAR.  At a minimum, the
WEB_APPLICATION_HOST_AND_PORT will need to modified in both applications, as
well as the IDP_SSO_SERVICE_URL in the sp.war.

Xerces and Xalan need to be in the endorsed directory of your application
server.  Open SAML will NOT work if you use a different XML Parser.  

These applications have been tested in Tomcat 6.  But should work in other
containers as well.


#############################################
#                                           # 
# Building the sample applications.         #
#                                           # 
#############################################

All three projects are built using Apache Ivy and Apache Ant.

The spring-saml-common project must be built first as it contains common code that
is used by both the IDP and SP.  There is also an ivysettings file that is included
that can be used to resolve dependencies in the shib repo.




 
