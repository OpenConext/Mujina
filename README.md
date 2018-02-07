## LAA customisation of the Mujina project

#### Useful sections in this document
* [Local development of the mock IdP/SP](#local-development-of-the-mock-idpsp)
* [Configure Spring Boot applications to run in an AWS EC2 instance](#configure-spring-boot-applications-to-run-in-an-aws-ec2-instance)
* [AWS EC2 installation instructions](/aws-ec2)
* [Build Mujina](#build-mujina)
* [Run the IDP](#run-the-idp)
* [Run the SP](#run-the-sp)


---

#### OneLogin Service Provider properties
* [Sample OneLogin Service Provider properties](#sample-onelogin-service-provider-properties)

---

### Custom API call scripts
* [View the custom API call scripts here](/api)

### Mujina API Calls
* [Resetting the IDP](#resetting-the-idp)
* [Changing the entityID](#changing-the-entityid)
* [Setting attribute foo to bar for the testuser user](#setting-attribute-foo-to-bar-for-the-testuser-user)
* [Adding a user](#adding-a-user)
* [Setting the Assertion Consumer Service (ACS) endpoint](#setting-the-assertion-consumer-service-acs-endpoint)

##### The original project can be found @ https://github.com/OpenConext/Mujina

---

### Local development of the mock IdP/SP
All commands being run are assumed to be executed in a linux terminal (_e.g. OS X or Git Bash in Windows_)
#### Clone the repository and run a Maven build
[Maven 3](http://maven.apache.org) is needed to build and run Mujina.

```
cd ~
git clone https://github.com/ministryofjustice/laa-saml-mock/
cd laa-saml-mock
mvn clean install
```

#### Custom Spring Boot application.yml
You can specify a custom spring boot configuration file to inject values in to the application
```
java -jar laa-saml-mock-idp-1.0.0.jar --spring.config.location=laa-saml-mock/mujina-idp/laa-saml-mock-idp-application.yml
```

For example:
```
vim laa-saml-mock/mujina-idp/laa-saml-mock-idp-application.yml
```
```
idp:
  base_url: http://10.0.1.1:8080

samlUserStore:
  samlUsers:
    - username: test-user
      password: test password
      samlAttributes:
        attribute 1: test attribute 1 value
        attribute 2: test attribute 2 value
```

#### Start the IdP with the bash script
```
cd mujina-idp
sh start-idp.sh
```

#### Stop the IdP with the bash script
```
cd mujina-idp
sh stop-idp.sh
```

#### Start the SP with the bash script
```
cd mujina-sp
sh start-sp.sh
```

#### Stop the SP with the bash script
```
cd mujina-sp
sh stop-sp.sh
```

---

### Configure Spring Boot applications to run in an AWS EC2 instance
Get the code and run a maven build as described at [Clone the github repository](#clone-the-repository-and-run-a-maven-build)
#### IdP
##### Add Spring Boot configuration
```
vim /home/ec2-user/laa-saml-mock/mujina-idp/laa-saml-mock-idp-application.yml
```
```
idp:
  base_url: http://${EC2_PUBLIC_HOST}:8080

samlUserStore:
  samlUsers:
    - username: test-user
      password: test password
      samlAttributes:
        attribute 1: test attribute 1 value
        attribute 2: test attribute 2 value
```

##### Start IdP app
```
#!/bin/bash
export EC2_PUBLIC_HOST=`curl http://169.254.169.254/latest/meta-data/public-ipv4`;

cd /home/ec2-user/laa-saml-mock/mujina-idp/target; sudo -u ec2-user nohup java -DEC2_PUBLIC_HOST=${EC2_PUBLIC_HOST} -jar laa-saml-mock-idp-1.0.0.jar --spring.config.location=/home/ec2-user/laa-saml-mock/mujina-idp/laa-saml-mock-idp-application.yml &
```

##### Tail the log file
```
tail -f /home/ec2-user/laa-saml-mock/mujina-sp/target/nohup.out
```

#### SP
###### Add Spring Boot configuration
```
vim /home/ec2-user/laa-saml-mock/mujina-sp/laa-saml-mock-sp-application.yml
```
```
sp:
  base_url: http://${EC2_PUBLIC_HOST}:9090
  entity_id: http://mock-sp
  idp_metadata_url: http://${EC2_PUBLIC_HOST}:8080/metadata
  single_sign_on_service_location: http://${EC2_PUBLIC_HOST}:8080/SingleSignOnService
  acs_location_path: /saml/SSO
```

##### Start SP app
```
#!/bin/bash
export EC2_PUBLIC_HOST=`curl http://169.254.169.254/latest/meta-data/public-ipv4`;

cd /home/ec2-user/laa-saml-mock/mujina-sp/target; sudo -u ec2-user nohup java -DEC2_PUBLIC_HOST=${EC2_PUBLIC_HOST} -jar laa-saml-mock-sp-1.0.0.jar --spring.config.location=/home/ec2-user/laa-saml-mock/mujina-sp/laa-saml-mock-sp-application.yml &
```

##### Tail the log file
```
tail -f /home/ec2-user/laa-saml-mock/mujina-sp/target/nohup.out
```

---

Sample OneLogin Service Provider properties
-------------------------------------------
A sample properties file to be used in conjunction with OneLogin's SAML
Java Toolkit can be found [here](/onelogin/onelogin.saml.properties).

You can read more about the OneLogin SAML Java Toolkit @ https://github.com/onelogin/java-saml

### Required Properties
The following properties need to be supplied to the service provider web
application, such as Tomcat Catalina properties, or Spring Boot
application properties:

##### Service Provider properties
* saml.sp.entity.id
* saml.sp.acs.url
* saml.sp.x509cert
* saml.sp.privatekey

##### Identity Provider properties
* saml.idp.entity.id
* saml.idp.sso.url
* saml.idp.x509cert

##### Security settings
* saml.encryption.assertions.enabled

##### Service Provider contact properties
* saml.sp.org.name
* saml.sp.org.displayname
* saml.sp.org.url
* saml.sp.contact.technical.name
* saml.sp.contact.technical.email
* saml.sp.contact.support.name
* saml.sp.contact.support.email

### Optional Properties
##### Service Provider contact properties
* saml.sp.org.language

---
# Original project documentation

<pre>___  ___        _  _
|  \/  |       (_)(_)
| .  . | _   _  _  _  _ __    __ _
| |\/| || | | || || || '_ \  / _` |
| |  | || |_| || || || | | || (_| |
\_|  |_/ \__,_|| ||_||_| |_| \__,_|
              _/ |
             |__/

  Configurable Identity and Service Provider built with OpenSAML & Java Spring Boot
</pre>

Mujina
======

[![Build Status](https://travis-ci.org/ministryofjustice/laa-saml-mock.svg)](https://travis-ci.org/ministryofjustice/laa-saml-mock)
[![codecov.io](https://codecov.io/github/ministryofjustice/laa-saml-mock/coverage.svg)](https://codecov.io/github/ministryofjustice/laa-saml-mock)

Mujina is a SAML2 Identity and Service Provider (IdP & SP). 

Note that backward incompatibilities were introduced in version 5.0.0. If you want to migrate from pre-5 versions to the post-5 versions 
then the following has changed:
 
* We no longer use Tomcat, but standalone Spring boot applications
* The API has changed for all end-points requiring a single value (e.g. String or boolean) and only that value is required in the request body. See the API documentation below.
 
Characteristics of both the IdP or SP can be runtime changed with the REST API. 

Mujina is used to test the SURFconext middleware which enables Dutch educational services to use cloud based SAAS-services.

Features
--------
- A SAML2-compliant Identity Provider. The IdP will authenticate known users, providing known attributes to the SP. The REST api allows for the manipulation of:
  * user credentials (either a specific username & password or allow any username and password)
  * user role
  * any user attributes
  * signing certificate
  * entityID
  * ACS endpoint
  * signature Algorithm

- A SAML2-compliant Service Provider. The SP displays the attributes as these were received from an IdP. The REST api allows for the manipulation of:
  * entityID
  * signing certificate  
  * SSO Service URL
  * signature Algorithm

Defaults
--------
The default Identity Provider configuration is as follows:

* The Entity ID is "http://mock-idp"
* The signatureAlgorithm is "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"
* It has a user with login "admin" and password "secret" with roles ROLE_USER and ROLE_ADMIN
* It has a user with login "user" and password "secret" with role ROLE_USER
* It has no default attributes. Attributes are always stored as lists. Even when they contain a single value.
* There is a default certificate and private key available
* By default the ACS endpoint should be provided by the SP as an attribute in the AuthnRequest.
  If the ACS endpoint is set using the IdP api this is not necessary. Use of the api overrides values set in AuthnRequests

The default Service Provider configuration is as follows:

* The Entity ID is "http://mock-sp"
* The signatureAlgorithm is "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"
* There is a default certificate and private key available

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
        -X PUT -d "myEntityId" \
        http://localhost:8080/api/entityid
```

Setting the Signature Algorithm
-------------

This API is available on both the IDP and the SP.

```bash
curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X PUT -d "http://www.w3.org/2000/09/xmldsig#rsa-sha1" \
        http://localhost:9090/api/signatureAlgorithm
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

Setting attribute foo to bar for the __testuser__ user
----------------------------
(e.g. urn:mace:dir:attribute-def:foo to bar)

This API is only available on the IDP. **Note:** An attribute is always a list.

```bash
curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X PUT -d '["bar"]' \
        http://localhost:8080/api/attributes/testuser/urn:mace:dir:attribute-def:foo
```

Removing an attribute from the __testuser__ user
------------------------------------------------

This API is only available on the IDP.

```bash
curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X DELETE \
        http://localhost:8080/api/attributes/testuser/urn:mace:dir:attribute-def:foo
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
        -X PUT -d "ALL" \
        http://localhost:8080/api/authmethod
```

Setting the Assertion Consumer Service (ACS) endpoint
---------------------------------

```bash
curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X PUT -d "https://my_sp.no:443/acsendpoint_path" \
        http://localhost:8080/api/acsendpoint
```

The authentication method API has two possible values.

* USER
* ALL

The setting is configurable in the application.yml
```
# Authentication method ALL for every username / password combination and USER for the configured users
auth_method: USER
```

The USER setting requires a valid user to be known in Mujina's IdP and the ALL accepts everything.

The ALL setting allows any username and password combination.
As a side effect, the urn:mace:dir:attribute-def:uid attribute is set to the username each time a user logs in.

Setting the SSO Service URL
-------------

This API is only available on the SP.

```bash
curl -v -H "Accept: application/json" \
        -H "Content-type: application/json" \
        -X PUT -d "http://localhost:8080/SingleSignOnService/vo:test" \
        http://localhost:9090/api/ssoServiceURL
```
