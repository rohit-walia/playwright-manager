package org.playwright.core;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.playwright.common.PlaywrightResource;
import org.playwright.core.options.BrowserLaunchOption;

class PlaywrightManagerTest {
  @Test
  void testGetResourcesBeforeCreating() {
    Assertions.assertTrue(PlaywrightManager.get(PlaywrightResource.PLAYWRIGHT).isEmpty());
    Assertions.assertTrue(PlaywrightManager.get(PlaywrightResource.BROWSER).isEmpty());
    Assertions.assertTrue(PlaywrightManager.get(PlaywrightResource.BROWSER_CONTEXT).isEmpty());
  }

  @Test
  void testGetResourcesAfterCreating_ThenCloseResources() {
    Playwright playwright = PlaywrightManager.create(PlaywrightResource.PLAYWRIGHT);
    Browser browser = PlaywrightManager.create(PlaywrightResource.BROWSER, BrowserLaunchOption.builder().build());
    BrowserContext browserContext = PlaywrightManager.create(PlaywrightResource.BROWSER_CONTEXT);

    // verify resources are created with default options
    Assertions.assertNotNull(playwright);
    Assertions.assertNotNull(browser);
    Assertions.assertNotNull(browserContext);

    // verify we are able to get the created resources via PlaywrightManager#get
    Assertions.assertEquals(PlaywrightManager.get(PlaywrightResource.PLAYWRIGHT).orElseThrow(), playwright);
    Assertions.assertEquals(PlaywrightManager.get(PlaywrightResource.BROWSER).orElseThrow(), browser);
    Assertions.assertEquals(PlaywrightManager.get(PlaywrightResource.BROWSER_CONTEXT).orElseThrow(), browserContext);

    // close all resources via PlaywrightManager#close
    PlaywrightManager.close(browserContext);
    PlaywrightManager.close(browser);
    PlaywrightManager.close(playwright);

    // verify resources are disconnected and can't be invoked after they are closed
    Assertions.assertFalse(browser.isConnected(), "When a Browser resource is closed, it should be disconnected");
    Assertions.assertThrows(PlaywrightException.class, () -> playwright.chromium().launch(), "When a Playwright "
        + "resource is closed, it should not be possible to launch a browser.");
  }

  @Test
  void testCreateMultipleResourcesAtSameTime() {
    // create two Playwright connections
    Playwright playwright1 = PlaywrightManager.create(PlaywrightResource.PLAYWRIGHT);
    Playwright playwright2 = PlaywrightManager.create(PlaywrightResource.PLAYWRIGHT);

    Assertions.assertNotNull(playwright1);
    Assertions.assertNotNull(playwright2);
    Assertions.assertNotEquals(playwright1, playwright2);

    // create one Browser instance per Playwright resource
    Browser browser1 = PlaywrightManager.create(PlaywrightResource.BROWSER, playwright1);
    Browser browser2 = PlaywrightManager.create(PlaywrightResource.BROWSER, playwright2);

    Assertions.assertNotNull(browser1);
    Assertions.assertNotNull(browser2);
    Assertions.assertNotEquals(browser1, browser2);

    // create one BrowserContext for each Browser instance
    BrowserContext browserContext1 = PlaywrightManager.create(PlaywrightResource.BROWSER_CONTEXT, browser1);
    BrowserContext browserContext2 = PlaywrightManager.create(PlaywrightResource.BROWSER_CONTEXT, browser2);

    Assertions.assertNotNull(browserContext1);
    Assertions.assertNotNull(browserContext2);
    Assertions.assertNotEquals(browserContext1, browserContext2);

    // close all the resources
    PlaywrightManager.close(browser1);
    PlaywrightManager.close(browserContext1);
    PlaywrightManager.close(playwright1);
    PlaywrightManager.close(browserContext2);
    PlaywrightManager.close(browser2);
    PlaywrightManager.close(playwright2);
  }

  @Test
  void testCreateBrowserContextWithoutBrowserInstance_ThrowsException() {
    Assertions.assertThrows(PlaywrightException.class,
        () -> PlaywrightManager.create(PlaywrightResource.BROWSER_CONTEXT),
        "When creating BrowserContext without creating upstream resource first, it should throw an exception.");
  }

  @Test
  void testCreateBrowserWithoutPlaywrightInstance_ThrowsException() {
    Assertions.assertThrows(PlaywrightException.class, () -> PlaywrightManager.create(PlaywrightResource.BROWSER),
        "When creating Browser without creating upstream resource first, it should throw an exception.");
  }
}