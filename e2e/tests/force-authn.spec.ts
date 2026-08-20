import { test, expect } from '@playwright/test';

test.describe('Force Authn request? checkbox', () => {

  test.beforeEach(async ({ request }) => {
    await request.post('http://localhost:8080/api/reset', { headers: { 'Content-Type': 'application/json' } });
    await request.post('http://localhost:9090/api/reset', { headers: { 'Content-Type': 'application/json' } });
  });

  test('toggles the login link\'s force-authn query parameter', async ({ page }) => {
    await page.goto('/');

    const userLink = page.locator('#user-link');
    await expect(userLink).toHaveAttribute('href', /force-authn=false/);

    await page.check('#force-authn');
    await expect(userLink).toHaveAttribute('href', /force-authn=true/);

    await page.uncheck('#force-authn');
    await expect(userLink).toHaveAttribute('href', /force-authn=false/);
  });

  test('forces re-authentication at the IdP even with an existing session', async ({ page }) => {
    // First login establishes a session at both the SP and the IdP.
    await page.goto('/');
    await page.click('#user-link');
    await page.waitForURL(/localhost:8080\/login/);
    await page.fill('#username', 'admin');
    await page.fill('#password', 'secret');
    await page.click('form.login-form input[type=submit]');
    await page.waitForURL(/localhost:9090\/user\.html/);

    // Log out of the SP only - this does not touch the IdP's own session, matching
    // real SSO semantics (single sign-ON here, not single sign-OUT). The SP's "/" only
    // shows the login link again when *its own* session is gone (see UserController#index).
    await page.getByRole('link', { name: 'Logout' }).click();
    await expect(page.locator('#user-link')).toBeVisible();

    // Without force-authn: the IdP still has a valid session, so logging in again
    // is expected to be transparent SSO - no login form should be shown.
    await page.click('#user-link');
    await page.waitForURL(/localhost:9090\/user\.html/);
    expect(page.url()).not.toContain('localhost:8080/login');

    // Log out of the SP again, then retry with force-authn checked.
    await page.getByRole('link', { name: 'Logout' }).click();
    await expect(page.locator('#user-link')).toBeVisible();

    // With force-authn checked: the IdP must show the login form again, even
    // though the same session that just satisfied SSO above is still valid.
    await page.check('#force-authn');
    await page.click('#user-link');
    await page.waitForURL(/localhost:8080\/login/);
    await expect(page.locator('form.login-form')).toBeVisible();

    // Completing the forced login still lands back on the SP as normal.
    await page.fill('#username', 'admin');
    await page.fill('#password', 'secret');
    await page.click('form.login-form input[type=submit]');
    await page.waitForURL(/localhost:9090\/user\.html/);
  });

});
