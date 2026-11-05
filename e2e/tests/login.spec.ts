import { test, expect } from '@playwright/test';

test.describe('SP-initiated SAML login', () => {

  test.beforeEach(async ({ request }) => {
    // Reset both apps to their default seeded state before each test.
    await request.post('http://localhost:8080/api/reset', { headers: { 'Content-Type': 'application/json' } });
    await request.post('http://localhost:9090/api/reset', { headers: { 'Content-Type': 'application/json' } });
  });

  test('login as admin lands on /user.html with the expected attributes', async ({ page }) => {
    await page.goto('/');
    await page.click('#user-link');

    // SP redirects into the IdP's SAML login flow.
    await page.waitForURL(/localhost:8080\/login/);

    await page.fill('#username', 'admin');
    await page.fill('#password', 'secret');
    await page.click('form.login-form input[type=submit]');

    // IdP posts the signed SAMLResponse back to the SP's ACS, landing on /user.html.
    await page.waitForURL(/localhost:9090\/user\.html/);

    const items = page.locator('section.attributes ul li');
    await expect(items.nth(1)).toHaveText('admin'); // nameID == the submitted username

    // mujina-idp derives cn/displayName/mail etc. from the submitted username (see
    // mujina.idp.SsoController#attributes), so logging in as "admin" yields "Admin Doe" /
    // admin@example.com rather than the static "John Doe" defaults - this assertion is
    // exercising that derivation, not just a hardcoded default.
    const attributes = page.locator('section.attributes');
    await expect(attributes).toContainText('Admin Doe');
    await expect(attributes).toContainText('admin@example.com');
  });

  // Regression test for https://github.com/OpenConext/Mujina/commit/4926bf815234a140f76bfe5e5fa11e6693e4acce:
  // mujina.idp.SsoController#attributes now derives the uid used for generating mail/cn/displayName/givenName
  // (and for the uid attribute itself) from a submitted urn:mace:dir:attribute-def:uid form field when present,
  // instead of always from the authenticated username - mimicking a federated/proxied login that supplies its
  // own uid distinct from the local account used to sign in.
  test('login with a submitted uid attribute derives attributes from it instead of the username', async ({ page }) => {
    await page.goto('/');
    await page.click('#user-link');
    await page.waitForURL(/localhost:8080\/login/);

    await page.fill('#username', 'admin');
    await page.fill('#password', 'secret');

    await page.selectOption('#add-attribute', 'urn:mace:dir:attribute-def:uid');
    await page.fill('#attribute-list li.attribute-value .input-attribute-value', 'janesmith');

    await page.click('form.login-form input[type=submit]');
    await page.waitForURL(/localhost:9090\/user\.html/);

    // nameID still reflects the local account used to authenticate.
    const items = page.locator('section.attributes ul li');
    await expect(items.nth(1)).toHaveText('admin');

    // but the generated attributes are derived from the submitted uid, not "admin".
    const attributes = page.locator('section.attributes');
    await expect(attributes).toContainText('Janesmith Doe');
    await expect(attributes).toContainText('janesmith@example.com');
    await expect(attributes).not.toContainText('Admin Doe');
    await expect(attributes).not.toContainText('admin@example.com');
  });

});
