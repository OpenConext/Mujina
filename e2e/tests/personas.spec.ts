import { test, expect } from '@playwright/test';

const IDP_LOGIN = 'http://localhost:8080/login';

test.describe('IdP login personas (client-side saved settings)', () => {

  test.beforeEach(async ({ request }) => {
    // Reset the IdP to its default seeded state before each test.
    await request.post('http://localhost:8080/api/reset', { headers: { 'Content-Type': 'application/json' } });
  });

  test('save a persona, load it into the form, persist across reload, delete it', async ({ page }) => {
    await page.goto(IDP_LOGIN);

    // The save button is hidden while no attribute row exists.
    await expect(page.locator('#save-persona')).toBeHidden();

    await page.fill('#username', 'jdoe');
    await page.fill('#password', 'secret');
    await page.check('#persist-me');

    // Add one dynamically added attribute row (field + value must be part of the persona).
    await page.selectOption('#add-attribute', { index: 1 });
    await page.fill('#attribute-list .attribute-value input.input-attribute-value', 'Engineering');

    // The save button appears once at least one attribute row exists.
    await expect(page.locator('#save-persona-button')).toBeVisible();
    await page.click('#save-persona-button');

    // The inline name input is pre-filled with the current username.
    const nameInput = page.locator('#persona-name');
    await expect(nameInput).toBeVisible();
    await expect(nameInput).toHaveValue('jdoe');
    await page.click('#persona-name-save');

    // The persona shows up in the list below the form.
    await expect(page.locator('li.persona')).toHaveCount(1);
    await expect(page.locator('button.persona-load')).toHaveText('jdoe');
    await expect(page.locator('#save-persona-button')).toBeVisible();

    // The suggested name is incremented while a persona with the same name exists.
    await page.click('#save-persona-button');
    await expect(nameInput).toHaveValue('jdoe-1');
    await page.click('#persona-name-save');
    await expect(page.locator('li.persona')).toHaveCount(2);
    await expect(page.locator('button.persona-load', { hasText: /^jdoe-1$/ })).toBeVisible();

    // ...and once more for jdoe-2.
    await page.click('#save-persona-button');
    await expect(nameInput).toHaveValue('jdoe-2');
    await page.click('#persona-name-save');
    await expect(page.locator('button.persona-load', { hasText: /^jdoe-2$/ })).toBeVisible();

    // Saving with an already-existing name overwrites that persona.
    await page.fill('#attribute-list .attribute-value input.input-attribute-value', 'Overwritten');
    await page.click('#save-persona-button');
    await nameInput.fill('jdoe');
    await page.click('#persona-name-save');
    await expect(page.locator('li.persona')).toHaveCount(3);

    // Drop the two duplicates again.
    await page.locator('li.persona', { hasText: /^jdoe-2/ }).locator('button.persona-delete').click();
    await page.locator('li.persona', { hasText: /^jdoe-1/ }).locator('button.persona-delete').click();
    await expect(page.locator('li.persona')).toHaveCount(1);
    const personaEntry = page.locator('li.persona');

    // Clear the form and load the (overwritten) persona back in.
    await page.fill('#username', '');
    await page.fill('#password', '');
    await page.locator('#attribute-list .attribute-value span.remove-attribute-value').click();
    await expect(page.locator('#save-persona')).toBeHidden();

    await personaEntry.locator('button.persona-load').click();
    await expect(page.locator('#username')).toHaveValue('jdoe');
    await expect(page.locator('#password')).toHaveValue('secret');
    await expect(page.locator('#persist-me')).toBeChecked();
    await expect(page.locator('#save-persona')).toBeVisible();
    const row = page.locator('#attribute-list .attribute-value');
    await expect(row).toHaveCount(1);
    await expect(row.locator('label')).toHaveText('urn:mace:dir:attribute-def:cn');
    await expect(row.locator('input.input-attribute-value')).toHaveValue('Overwritten');

    // The persona survives a page reload (localStorage).
    await page.reload();
    await expect(page.locator('li.persona', { hasText: 'jdoe' })).toBeVisible();
    await expect(page.locator('#attribute-list .attribute-value')).toHaveCount(0);

    // Delete the persona - the list disappears again.
    await page.locator('li.persona', { hasText: 'jdoe' }).locator('button.persona-delete').click();
    await expect(page.locator('li.persona')).toHaveCount(0);
    await expect(page.locator('#personas')).toBeHidden();
  });

  test('copying a persona produces a shareable URL that imports in a fresh browser', async ({ page, context, browser }) => {
    await page.goto(IDP_LOGIN);
    await page.fill('#username', 'shared');
    await page.fill('#password', 'topsecret');
    await page.selectOption('#add-attribute', { index: 1 });
    await page.fill('#attribute-list .attribute-value input.input-attribute-value', 'Shared value');

    await page.click('#save-persona-button');
    await page.click('#persona-name-save'); // name pre-filled with 'shared'
    const personaEntry = page.locator('li.persona', { hasText: 'shared' });
    await expect(personaEntry).toBeVisible();

    await context.grantPermissions(['clipboard-read', 'clipboard-write'], { origin: IDP_LOGIN });
    await personaEntry.locator('button.persona-copy').click();

    const url = await page.evaluate(() => navigator.clipboard.readText());
    expect(url).toMatch(/^http:\/\/localhost:8080\/login#data=.+$/);

    // The #data fragment base64-decodes to the persona payload.
    const payload = await page.evaluate((u: string) => {
      const b64 = u.split('#data=')[1];
      return JSON.parse(decodeURIComponent(escape(atob(b64))));
    }, url);
    expect(payload.name).toBe('shared');
    expect(payload.username).toBe('shared');
    expect(payload.password).toBe('topsecret');
    expect(payload.attributes).toHaveLength(1);
    expect(payload.attributes[0].value).toBe('Shared value');

    // Opening the URL in a fresh context (a "different browser": empty storage)
// saves the persona, shows it in the list and loads it into the form directly.
    const freshContext = await browser.newContext();
    const freshPage = await freshContext.newPage();
    await freshPage.goto(url);
    await expect(freshPage.locator('li.persona', { hasText: 'shared' })).toBeVisible();
    await expect(freshPage.locator('#username')).toHaveValue('shared');
    await expect(freshPage.locator('#password')).toHaveValue('topsecret');
    await expect(freshPage.locator('#attribute-list .attribute-value input.input-attribute-value')).toHaveValue('Shared value');

    // The hash is stripped so refreshing does not re-import the data.
    expect(freshPage.url()).toBe(IDP_LOGIN);
    const stored = await freshPage.evaluate(() => JSON.parse(localStorage.getItem('mujina.personas')));
    expect(stored.shared.username).toBe('shared');
    expect(stored.shared.password).toBe('topsecret');

    await freshPage.reload();
    await expect(freshPage.locator('#username')).toHaveValue('');
    await expect(freshPage.locator('li.persona', { hasText: 'shared' })).toBeVisible();

    // Re-opening the same URL overwrites the existing persona instead of duplicating it.
    await freshPage.goto(url);
    await expect(freshPage.locator('li.persona')).toHaveCount(1);

    await freshContext.close();
  });

  test('pressing Enter in the persona name field submits the login form', async ({ page }) => {
    await page.goto(IDP_LOGIN);
    await page.fill('#username', 'admin');
    await page.fill('#password', 'secret');
    await page.selectOption('#add-attribute', { index: 1 });
    await page.fill('#attribute-list .attribute-value input.input-attribute-value', 'Value');

    await page.click('#save-persona-button');
    await page.locator('#persona-name').press('Enter');

    // Enter keeps its default action: the form is submitted (login) instead of
    // saving the persona. The IdP accepts any credentials by default.
    await page.waitForURL(/localhost:8080\/user\.html/);
    expect(await page.evaluate(() => localStorage.getItem('mujina.personas'))).toBeNull();
  });

  test('loading a persona does not leak persona fields into the SAML assertion', async ({ page }) => {
    await page.goto(IDP_LOGIN);
    await page.fill('#username', 'admin');
    await page.fill('#password', 'secret');
    await page.selectOption('#add-attribute', { index: 1 });
    await page.fill('#attribute-list .attribute-value input.input-attribute-value', 'LeakCheckValue');
    await page.click('#save-persona-button');
    await page.fill('#persona-name', 'leakcheck');
    await page.click('#persona-name-save');
    await expect(page.locator('li.persona', { hasText: 'leakcheck' })).toBeVisible();

    // Walk the SP-initiated SAML flow and log in with the loaded persona.
    await page.goto('http://localhost:9090/');
    await page.click('#user-link');
    await page.waitForURL(/localhost:8080\/login/);

    await page.locator('li.persona', { hasText: 'leakcheck' }).locator('button.persona-load').click();
    await page.click('form.login-form input[type=submit]');
    await page.waitForURL(/localhost:9090\/user\.html/);

    const attributes = page.locator('section.attributes');
    await expect(attributes).toContainText('LeakCheckValue');
    // The persona name must not end up as a SAML attribute, i.e. none of the new
    // client-side form fields may be submitted to the backend.
    await expect(attributes).not.toContainText('leakcheck');
  });

});