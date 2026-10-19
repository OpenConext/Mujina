<!-- markdownlint-disable MD024 -->
# Changelog

All notable changes to Mujina are documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## Unreleased (10.0.2-SNAPSHOT)

- Add client-side personas to the IdP login form (#125)

## [10.0.1]

### Added

- Log full authentication details in DEBUG mode
- Client-side personas on the IdP login form
- Scripts to merge/approve PRs via the GitHub API

### Changed

- Docker images now based on OpenConext-Basecontainers, running as root
- Improved handling of `forceAuthn`

### Fixed

- Wrong replace versions in the SAML request/response flow

## [10.0.0]

> **Breaking**: as of 10.0.0 Mujina runs on Spring Boot 4 with
> `spring-security-saml2-service-provider` and the custom IdP library
> [openconext-saml-java](https://github.com/OpenConext/openconext-saml-java).
> A large-scale refactor/modernization of the codebase was performed.

### Changed

- Upgraded Spring Boot to 4.1.1
- Migrated tests to rest-assured 6.0.1

### Added

- Playwright-based e2e tests
- Stale issues workflow

## [9.0.x]

### Changed

- **Breaking**: migrated to JDK 21
- Fixed an Eclipse build issue by excluding the `xml-apis` dependency

### Added

- `eduPersonAssurance` attribute
- Support for multiple emails
- Additional example attributes
- arm64 Docker images, Codecov reporting and Dependabot config
- Docker images tagged with `:latest` / `:snapshot`

## [8.0.11]

### Added

- A completely unknown attribute in the example namespace (RFC 6963),
  useful for testing

## [8.0.10]

### Added

- `surf-autorisaties` attribute
- Option to select `AuthnContextClassRef` for the SAML response
- Endpoint to retrieve users
- GitHub Actions workflows to build Docker images of the SP and IdP

### Changed

- Attributes are sorted in the UI

## [8.0.6]

### Added

- `urn:oasis:names:tc:SAML:attribute:subject-id` to the default attributes
- Configurable SAML binding via `idp.saml_binding`

### Changed

- Restored the configurable validity of the metadata entity descriptor
- Capitalized attribute display names

### Fixed

- Attribute-related test failures (#87)

## [8.0.2]

### Added

- `urn:oasis:names:tc:SAML:attribute:subject-id` to the attributes list
- `ou` attribute

### Changed

- **Breaking**: migrated to Java 11
- Externalized SAML attribute and default value configuration
- Management endpoint served on the internal address

### Fixed

- Bug in signing IdP metadata
- Attribute add-select rendering in Internet Explorer 10/11

## [7.4.x]

### Added

- "Remember me" persistence on login (with secure cookie enforcement)
- Additional SAML attributes
- Support for `forceAuthn` on both the IdP and the SP

### Changed

- Avoided hard-coding `localhost` in IdP metadata by using `idp.base_url`
- Mobile-friendly login page
- Upgraded Spring Boot and spring-security-saml

### Fixed

- Adding/removing attributes
- `SubjectConfirmationData Address` no longer sent with the URL

## [7.0.x]

### Added

- Attributes can be added while logging in
- The NameID Format can be changed via attributes
- `user.html` alias

### Changed

- Uses Java 1.8

### Fixed

- Empty usernames are rejected
- Entering an attribute value no longer posts the form

## [5.0.5]

### Fixed

- Use the current port instead of hardcoding 8080

## [5.0.0]

> **Breaking**: Mujina no longer uses Tomcat; the IdP and SP are now standalone
> Spring Boot applications. The REST API changed for all end-points that
> require a single value (e.g. String or boolean): only that value is required
> in the request body.

## [4.1.3]

### Added

- HTTP-POST Single Logout handling
- Default `signatureAlgorithm` set to `rsa-sha256`

### Fixed

- Key name handling
- Various typos and issues (#9, #10)

## [4.0.0]

- First release on the new git-flow-based release process
