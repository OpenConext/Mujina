<pre>                   _         _      _   _
  /\/\   ___   ___| | _____ | | ___| |_| |__
 /    \ / _ \ / __| |/ / _ \| |/ _ \ __| '_ \
/ /\/\ \ (_) | (__|   &lt; (_) | |  __/ |_| | | |
\/    \/\___/ \___|_|\_\___/|_|\___|\__|_| |_|

  Mock Identity and Service Provider using OpenSAML
</pre>

Mockoleth
=========

Mockoleth mocks a SAML2 Identity and Service Provider. 
Almost all characteristics of either the IdP or SP can be configured on-the-fly using a REST API. This approach removes the need for special test configuration sets in your set-up. Thus, Mockoloth makes testing your stack a breeze! Mockoloth can be used in combination with test suites like Selenium or Jmeter to automate authentication testing for your applications.

Features
--------
- A SAML2 complient Identity Provider. The IdP will authenticate known users, providing known attributes to the SP. A REST api allows for the 'just-in-time' manipulation of:
  * user credentials (either a specific username & password or allow any username and password)
  * user role
  * any user attributes
  * signing certificate
  * entityID

- A SAML2 complient Service Provider. The SP displays the attributes as these were recieved from an IdP. A REST api allows for the 'just-in-time' manipulation of:
  * entityID
  * signing certificate  
  * sso Service URL
  


Defaults
--------
The default Identity Provider configuration is as follows:

* The Entity ID is "http://mock-idp"
* It has a user with login "admin" and password "secret" with roles ROLE_USER and ROLE_ADMIN
* It has a user with login "user" and password "secret" with role ROLE_USER
* It has the following attributes
    * "urn:mace:dir:attribute-def:uid" is "john.doe"
    * "urn:mace:dir:attribute-def:cn" is "John Doe"
    * "urn:mace:dir:attribute-def:givenName" is "John"
    * "urn:mace:dir:attribute-def:sn" is "Doe"
    * "urn:mace:dir:attribute-def:displayName" is "John Doe"
    * "urn:mace:dir:attribute-def:mail" is "j.doe@example.com"
    * "urn:mace:terena.org:attribute-def:schacHomeOrganization" is "example.com"
    * "urn:mace:dir:attribute-def:eduPersonPrincipalName" is "j.doe@example.com"
    * "urn:oid:1.3.6.1.4.1.1076.20.100.10.10.1" is "guest"
* There is a default certificate and private key available

The default Service Provider configuration is as follows:

* The Entity ID is "http://mock-sp"
* There is a default certificate and private key available

In this document you will find some examples for overriding the default configuration.
After you override configuration you can go back to the default using the reset API.

Build Mockoleth
---------------
[Maven 3](http://maven.apache.org) is needed to build and run Mockoleth.

Mockoleth may depend on artifacts (poms, jars) from open source projects that are not available in a public Maven
repository. Dependencies with groupId org.surfnet.coin can be built from source from the following locations:

  - coin-master: git://github.com/OpenConext/OpenConext-parent.git
  - coin-test: git://github.com/OpenConext/OpenConext-test.git

Run the IDP using jetty
-----------------------

<pre>
mvn clean install
cd mockoleth-idp
mvn jetty:run
</pre>

Then, go to https://localhost:8443/idp or http://localhost:8080/idp

Run the SP using jetty
----------------------

<pre>
mvn clean install
cd mockoleth-sp
mvn jetty:run
</pre>

Then, go to http://localhost:9090/idp

Changing the entityID
---------------------

This API is available on both the IDP and the SP.

<pre>
curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X PUT -d '{"value": "myEntityId"}' \
        http://localhost:8080/api/entityid
</pre>

Changing the signing credentials (Both IDP and SP)
--------------------------------

This API is available on both the IDP and the SP.
The certificate should be in PEM format.
The key should be in base64 encoded pkcs6 DER format.

<pre>
export CERT=MIICHzCCAYgCCQD7KMJ17XQa7TANBgkqhkiG9w0BAQUFADBUMQswCQYDVQQGEwJO\
TDEQMA4GA1UECAwHVXRyZWNodDEQMA4GA1UEBwwHVXRyZWNodDEQMA4GA1UECgwH\
U3VyZm5ldDEPMA0GA1UECwwGQ29uZXh0MB4XDTEyMDMwODA4NTQyNFoXDTEzMDMw\
ODA4NTQyNFowVDELMAkGA1UEBhMCTkwxEDAOBgNVBAgMB1V0cmVjaHQxEDAOBgNV\
BAcMB1V0cmVjaHQxEDAOBgNVBAoMB1N1cmZuZXQxDzANBgNVBAsMBkNvbmV4dDCB\
nzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEA2slVe459WUDL4RXxJf5h5t5oUbPk\
PlFZ9lQysSoS3fnFTdCgzA6FzQzGRDcfRj0HnWBdA1YH+LxBjNcBIJ/nBc7Ssu4e\
4rMO3MSAV5Ouo3MaGgHqVq6dCD47f52b98df6QTAA3C+7sHqOdiQ0UDCAK0C+qP5\
LtTcmB8QrJhKmV8CAwEAATANBgkqhkiG9w0BAQUFAAOBgQCvPhO0aSbqX7g7IkR7\
9IFVdJ/P7uSlYFtJ9cMxec85cYLmWL1aVgF5ZFFJqC25blyPJu2GRcSxoVwB3ae8\
sPCECWwqRQA4AHKIjiW5NgrAGYR++ssTOQR8mcAucEBfNaNdlJoy8GdZIhHZNkGl\
yHfY8kWS3OWkGzhWSsuRCLl78A==
export KEY=MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBANrJVXuOfVlAy+EV8SX+YebeaFGz\
5D5RWfZUMrEqEt35xU3QoMwOhc0MxkQ3H0Y9B51gXQNWB/i8QYzXASCf5wXO0rLuHuKzDtzEgFeT\
rqNzGhoB6launQg+O3+dm/fHX+kEwANwvu7B6jnYkNFAwgCtAvqj+S7U3JgfEKyYSplfAgMBAAEC\
gYBaPvwkyCTKYSD4Co37JxAJJCqRsQtv7SyXoCl8zKcVqwaIz4rUQRVN/Hv3/WjIFzqB3xLe4mjN\
YBIF31YWt/6ZslaLL5YJIXISrMgDuQzPKL8VqvvsH9XEpi/qSUsVAWa9Vaqqwa8JTPELK8QhHKaX\
TxGtatEuW1x6kSNXFCoasQJBAPUaYdj9oCDOGTaOaupF0GB6TIgIItpQESY1Dfpn4cvwB0jH8wBJ\
SBVeBqSa6dg4RI5ydD3J82xlF7NrQnvWpYkCQQDkg26KzQckoJ39HX2gYS4olSeQDAyIDzeCMkj7\
McDhigy0cL6k9nOQrKlq6V3vkBISTRg7JceJ4z3QE00edXWnAkEAoggv2WBJxIYbOurJmVhP2gff\
oiomyEYYIDcAp6KXLdffKOkuJulLIv0GzTiwEMWZ5MWbPOHN78Gg+naU/AM5aQJBALfbsANpt4eW\
28ceBUgXKMZqS+ywZRzL8YOF5gaGH4TYSCSeWiXsTUtoQN/OaFAqAQBMm2Rrn0KoXcGe5fvN0h0C\
QQDgNLxVcByrVgmRmTPTwLhSfIveOqE6jBlQ8o0KyoQl4zCSDDtMEb9NEFxxvI7NNjgdZh1RKrzZ\
5JCAUQcdrEQJ
curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X POST -d "{\"certificate\": \"$CERT\",\"key\":\"$KEY\"}" \
        http://localhost:8080/api/signing-credential
</pre>

Setting attribute foo to bar (e.g. urn:mace:dir:attribute-def:foo to bar)
-------------------------------------------------------

This API is only available on the IDP.

<pre>
curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X PUT -d '{"value": "bar"}' \
        http://localhost:8080/api/attributes/urn:mace:dir:attribute-def:foo
</pre>

Removing an attribute
---------------------

This API is only available on the IDP.

<pre>
curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X DELETE \
        http://localhost:8080/api/attributes/urn:mace:dir:attribute-def:foo
</pre>

Adding a user
-------------

This API is only available on the IDP.

<pre>
curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X PUT -d '{"name": "hacker", "password": "iamgod", "authorities": ["ROLE_USER", "ROLE_ADMIN"]}' \
        http://localhost:8080/api/users
</pre>

Setting the authentication method
---------------------------------

This API is only available on the IDP.

<pre>
curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X PUT -d '{"value": "ALL"}' \
        http://localhost:8080/api/authmethod
</pre>

The authentication method API has two possible values.

* USER
* ALL

The default setting is USER.
This setting requires a valid user to be known in Mockoleth's IdP.

The ALL setting allows any username and password combination.
As a side effect, the urn:mace:dir:attribute-def:uid attribute is set to the username each time a user logs in.

Setting the SSO Service URL
-------------

This API is only available on the SP.

<pre>
curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X PUT -d '{"value": "http://localhost:8080/SingleSignOnService/vo:test"}' \
        http://localhost:9090/api/ssoServiceURL
</pre>