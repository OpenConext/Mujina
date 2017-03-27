<pre>___  ___        _  _
|  \/  |       (_)(_)
| .  . | _   _  _  _  _ __    __ _
| |\/| || | | || || || '_ \  / _` |
| |  | || |_| || || || | | || (_| |
\_|  |_/ \__,_|| ||_||_| |_| \__,_|
              _/ |
             |__/

  Mock Identity and Service Provider using OpenSAML & Spring Boot
</pre>

Mujina
======

[![Build Status](https://travis-ci.org/OpenConext/Mujina.svg)](https://travis-ci.org/OpenConext/Mujina)
[![codecov.io](https://codecov.io/github/OpenConext/Mujina/coverage.svg)](https://codecov.io/github/OpenConext/Mujina)

Mujina is a SAML2 Identity and Service Provider (IdP & SP). Note that backward incompatibilities were introduced in version 5.0.0. If you want to migrate from pre-5 versions to the 5 version 
or post-5 version then the following has changed:
 
* We no longer use Tomcat, but standalone Spring boot applications
* The API has changed for all end-points requiring a single value (e.g. String or boolean) and only that value is required in the request body. See the API documentation below.
 
Almost all characteristics of either the IdP or SP can be configured on-the-fly using a REST API. This approach removes the need for special test configuration sets in your set-up.
Mujina can be used in combination with test suites like Selenium or Jmeter to automate authentication testing for your applications.

Mujina is used to test the SURFconext middleware which enables Dutch educational services to use cloud based SAAS-services.

Features
--------
- A SAML2-compliant Identity Provider. The IdP will authenticate known users, providing known attributes to the SP. A REST api allows for the 'just-in-time' manipulation of:
  * user credentials (either a specific username & password or allow any username and password)
  * user role
  * any user attributes
  * signing certificate
  * entityID
  * ACS endpoint
  * SLO Endpoint of the SP (HTTP Post Binding)

- A SAML2-compliant Service Provider. The SP displays the attributes as these were received from an IdP. A REST api allows for the 'just-in-time' manipulation of:
  * entityID
  * signing certificate  
  * sso Service URL
  * SLO Endpoint of the IDP (HTTP Post Binding)

Defaults
--------
The default Identity Provider configuration is as follows:

* The Entity ID is "http://mock-idp"
* It has a user with login "admin" and password "secret" with roles ROLE_USER and ROLE_ADMIN
* It has a user with login "user" and password "secret" with role ROLE_USER
* It has the following attributes. Attributes are always stored as lists. Even when they contain a single value.
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
* By default the ACS endpoint should be provided by the SP as an attribute in the AuthnRequest.
  If the ACS endpoint is set using the IdP api this is not neccesary. Use of the api overrides values set in AuthnRequests
* By default the peers SLO endpoint is not configured and no SLO Requests are sent

The default Service Provider configuration is as follows:

* The Entity ID is "http://mock-sp"
* There is a default certificate and private key available
* By default the peers SLO endpoint is not configured and no SLO Requests are sent

In this document you will find some examples for overriding the default configuration.
After you override configuration you can go back to the default using the reset API.

Build Mujina
---------------

[Maven 3](http://maven.apache.org) is needed to build and run Mujina.

The build dependencies are hosted on https://build.openconext.org/repository/public/
(and will be fetched automatically by Maven).

Run the IDP 
-----------------------

```bash
mvn clean install
cd mujina-idp
mvn spring-boot:run
```

Then, go to http://localhost:8080/. If you want the application to run over https, please refer
to the [spring boots docs](https://docs.spring.io/spring-boot/docs/current/reference/html/howto-embedded-servlet-containers.html#howto-configure-ssl).

Run the SP
----------------------

```bash
mvn clean install
cd mujina-sp
mvn spring-boot:run
```

Then, go to http://localhost:9090/. You can access the secure page and will be redirected to the IdP, where you can
login with username admin and password secret.

## [Private signing key and public certificate](#signing-keys)

The SAML Spring Security library needs a private DSA key / public certificate pair for the IdP / SP which can be re-generated
if you want to use new key pairs.

```bash
openssl req -subj '/O=Organization, CN=Mujina/' -newkey rsa:2048 -new -x509 -days 3652 -nodes -out mujina.crt -keyout mujina.pem
```

The Java KeyStore expects a pkcs8 DER format for RSA private keys so we have to re-format that key:

```bash
openssl pkcs8 -nocrypt  -in mujina.pem -topk8 -out mujina.der
```
 
Remove the whitespace, heading and footer from the mujina.crt and mujina.der:

```bash
cat mujina.der |head -n -1 |tail -n +2 | tr -d '\n'; echo
cat mujina.crt |head -n -1 |tail -n +2 | tr -d '\n'; echo
```

Above commands work on linux distributions. On mac you can issue the same command with `ghead` after you install `coreutils`:

```bash
brew install coreutils

cat mujina.der |ghead -n -1 |tail -n +2 | tr -d '\n'; echo
cat mujina.crt |ghead -n -1 |tail -n +2 | tr -d '\n'; echo
```

Add the mujina key pair to the application.yml file:

```yml
idp:
  private_key: ${output from cleaning the der file}
  certificate: ${output from cleaning the crt file}

sp:
  private_key: ${output from cleaning the der file}
  certificate: ${output from cleaning the crt file}
```

Resetting the IDP
-----------------

This API is available on both the IDP and the SP.

```bash
curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X POST \
        http://localhost:8080/api/reset
```

Changing the entityID
---------------------

This API is available on both the IDP and the SP.

```bash
curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X PUT -d '"myEntityId"' \
        http://localhost:8080/api/entityid
```

Changing the signing credentials (Both IDP and SP)
--------------------------------

This API is available on both the IDP and the SP.
The certificate should be in PEM format.
The key should be in base64 encoded pkcs6 DER format.

```bash
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
```

Setting attribute foo to bar (e.g. urn:mace:dir:attribute-def:foo to bar)
-------------------------------------------------------

This API is only available on the IDP. **Note:** An attribute is always a list.

```bash
curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X PUT -d '["bar"]' \
        http://localhost:8080/api/attributes/urn:mace:dir:attribute-def:foo
```

Removing an attribute
---------------------

This API is only available on the IDP.

```bash
curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X DELETE \
        http://localhost:8080/api/attributes/urn:mace:dir:attribute-def:foo
```

Adding a user
-------------

This API is only available on the IDP.

```bash
curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X PUT -d '{"name": "hacker", "password": "iamgod", "authorities": ["ROLE_USER", "ROLE_ADMIN"]}' \
        http://localhost:8080/api/users
```

Setting the authentication method
---------------------------------

This API is only available on the IDP.

```bash
curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X PUT -d '"ALL"' \
        http://localhost:8080/api/authmethod
```

Setting the Assertion Consumer Service (ACS) endpoint
---------------------------------

This API is only available on the IDP.

```bash
curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X PUT -d '"https://my_sp.no:443/acsendpoint_path"' \
        http://localhost:8080/api/acsendpoint
```

The authentication method API has two possible values.

* USER
* ALL

The default setting is USER.
This setting requires a valid user to be known in Mujina's IdP.

The ALL setting allows any username and password combination.
As a side effect, the urn:mace:dir:attribute-def:uid attribute is set to the username each time a user logs in.

Setting the SSO Service URL
-------------

This API is only available on the SP.

```bash
curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X PUT -d '"http://localhost:8080/SingleSignOnService/vo:test"' \
        http://localhost:9090/api/ssoServiceURL
```

Changing the peers SLO Service URL
---------------------

This API is available on both the IDP and the SP.

```bash
curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X PUT -d '"http://my_sp/SingleLogoutService"' \
        http://localhost:8080/api/sloendpoint
```
