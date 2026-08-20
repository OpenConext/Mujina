import { defineConfig, devices } from '@playwright/test';

// Starts both mujina-idp (:8080) and mujina-sp (:9090) via `mvn spring-boot:run`, polling each
// app's /internal/health endpoint until it's up, before running the tests. mujina-common (and
// ideally the whole reactor) must already be `mvn install`ed so spring-boot:run can resolve
// org.openconext:mujina-common as a Maven coordinate - see README for the one-time setup step.
export default defineConfig({
  testDir: './tests',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: 1,
  reporter: 'html',
  use: {
    baseURL: 'http://localhost:9090',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  webServer: [
    {
      command: 'mvn -pl mujina-idp spring-boot:run',
      cwd: '..',
      url: 'http://localhost:8080/internal/health',
      timeout: 120_000,
      reuseExistingServer: !process.env.CI,
    },
    {
      command: 'mvn -pl mujina-sp spring-boot:run',
      cwd: '..',
      url: 'http://localhost:9090/internal/health',
      timeout: 120_000,
      reuseExistingServer: !process.env.CI,
    },
  ],
});
