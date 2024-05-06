package org.playwright.core;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.playwright.common.OptionCtx;
import org.playwright.common.PlaywrightResource;
import org.playwright.common.ResourceOptionArg;
import org.playwright.core.options.BrowserContextOption;
import org.playwright.core.options.BrowserLaunchOption;
import org.playwright.core.options.IOption;
import org.playwright.core.options.PlaywrightOption;
import org.playwright.core.options.TracingStartOption;

class PlaywrightManagerTest {
  @Test
  void testPlaywrightManager_CreatingResourcesInChronologicalOrder() {
    // test as much functionality as possible without opening more than one Playwright connection.
    // create resources in chronological order as this is the expected usage pattern.
    Playwright playwright = PlaywrightManager.create(PlaywrightResource.PLAYWRIGHT);
    Browser browser = PlaywrightManager.create(PlaywrightResource.BROWSER);
    BrowserContext browserContext = PlaywrightManager.create(PlaywrightResource.BROWSER_CONTEXT);

    Assertions.assertNotNull(playwright);
    Assertions.assertNotNull(browser);
    Assertions.assertNotNull(browserContext);
    verifyOptionContextHasDefaultValues();

    // verify factory behavior when creating multiple resources
    verifyOnMultipleBrowserContext(browserContext);
    verifyOnMultipleBrowser(browser);
    verifyOnMultiplePlaywright(playwright);

    // close all resources
    PlaywrightManager.close(browserContext);
    PlaywrightManager.close(browser);
    PlaywrightManager.close(playwright);

    // verify resources are disconnected and can't be invoked after they are closed
    Assertions.assertFalse(browser.isConnected(), "When a Browser resource is closed, it should be disconnected");
    Assertions.assertThrows(PlaywrightException.class, () -> playwright.chromium().launch(), "When a Playwright "
        + "resource is closed, it should not be possible to launch a browser.");
  }

  @Test
  void testPlaywrightManager_CreatingResourcesInNonChronologicalOrder() {
    // try creating BrowserContext without creating upstream resources first
    Assertions.assertThrows(PlaywrightException.class,
        () -> PlaywrightManager.create(PlaywrightResource.BROWSER_CONTEXT),
        "When creating BrowserContext without creating upstream resource first, it should throw an exception.");

    // try creating Browser without creating upstream resource first
    Assertions.assertThrows(PlaywrightException.class, () -> PlaywrightManager.create(PlaywrightResource.BROWSER),
        "When creating Browser without creating upstream resource first, it should throw an exception.");
  }

  private void verifyOnMultiplePlaywright(Playwright originalPlaywright) {
    Playwright newPlaywright = PlaywrightManager.create(PlaywrightResource.PLAYWRIGHT);
    Assertions.assertEquals(originalPlaywright, newPlaywright, "Creating new Playwright resource when there "
        + "is an existing instance, by default, should return the existing Playwright instance");
  }

  private void verifyOnMultipleBrowser(Browser originalBrowser) {
    Browser newBrowser1 = PlaywrightManager.create(PlaywrightResource.BROWSER);
    Assertions.assertEquals(originalBrowser, newBrowser1, "Creating new Browser resource when there is an "
        + "existing Browser instance, by default, should return the existing browser instance");

    IOption<?> originalBrowserLaunchOption = OptionCtx.getContext().get(OptionCtx.Key.BROWSER_LAUNCH_OPTION);
    BrowserLaunchOption newBrowserLaunchOptions = BrowserLaunchOption.builder().slowmo(500).build();
    Browser newBrowser2 = PlaywrightManager.create(PlaywrightResource.BROWSER,
        ResourceOptionArg.NEW_BROWSER_INSTANCE, newBrowserLaunchOptions);

    Assertions.assertNotEquals(newBrowser1, newBrowser2, "Creating new Browser resource with "
        + "ResourceOptionArg.NEW_BROWSER_INSTANCE should return a new instance");
    Assertions.assertNotEquals(newBrowserLaunchOptions, originalBrowserLaunchOption, "Creating new Browser resource "
        + "with ResourceOptionArg.NEW_BROWSER_INSTANCE should override the existing BrowserLaunchOption.");

    PlaywrightManager.close(newBrowser2);
    Assertions.assertFalse(newBrowser2.isConnected(), "When a Browser resource is closed, it should be disconnected");
    Assertions.assertTrue(newBrowser1.isConnected(), "When a new Browser resource is created, the original Browser "
        + "resource should still be connected");
  }

  private void verifyOnMultipleBrowserContext(BrowserContext originalBrowserContext) {
    // capture original browser context option
    IOption<?> origBrowserContextOption = OptionCtx.getContext().get(OptionCtx.Key.BROWSER_CONTEXT_OPTION);
    IOption<?> origTracingStartOption = OptionCtx.getContext().get(OptionCtx.Key.TRACE_START_OPTION);

    // create new browser context option (different from the original)
    BrowserContextOption newBrowserContextOption = BrowserContextOption.builder().recordVideoDir("test").build();
    TracingStartOption newTraceStartOption = TracingStartOption.builder().enableScreenshot(false).build();

    // create new BrowserContext and pass new options as arguments
    BrowserContext newBrowserContext = PlaywrightManager.create(PlaywrightResource.BROWSER_CONTEXT,
        newBrowserContextOption,
        newTraceStartOption);

    Assertions.assertNotEquals(originalBrowserContext, newBrowserContext, "When multiple BrowserContext are created"
        + "by invoking PlaywrightManager#create, each BrowserContext should be a new instance.");
    Assertions.assertNotEquals(origBrowserContextOption, newBrowserContextOption, "When multiple BrowserContext "
        + "is created, then the original BrowserContextOption should be overridden by the new BrowserContextOption.");
    Assertions.assertNotEquals(origTracingStartOption, newTraceStartOption, "When multiple BrowserContext is "
        + "created, then the original TracingStartOption should be overridden by the new TracingStartOption.");

    PlaywrightManager.close(newBrowserContext);
    Assertions.assertThrows(PlaywrightException.class, () -> newBrowserContext.setDefaultTimeout(1),
        "After closing a BrowserContext, it should not be possible to manipulate it.");
  }

  private void verifyOptionContextHasDefaultValues() {
    PlaywrightOption expectedDefaultPlaywrightOptions = PlaywrightOption.builder().build();
    IOption<?> actualPlaywrightOption = OptionCtx.getContext().get(OptionCtx.Key.PLAYWRIGHT_OPTION);
    Assertions.assertEquals(expectedDefaultPlaywrightOptions, actualPlaywrightOption, "When Playwright is created"
        + "by invoking PlaywrightManager#create, without passing any arguments, the default PlaywrightOption "
        + "should be set in the context.");

    BrowserLaunchOption expectedBrowserLaunchOptions = BrowserLaunchOption.builder().build();
    IOption<?> actualBrowserLaunchOption = OptionCtx.getContext().get(OptionCtx.Key.BROWSER_LAUNCH_OPTION);
    Assertions.assertEquals(expectedBrowserLaunchOptions, actualBrowserLaunchOption, "When Browser is created"
        + "by invoking PlaywrightManager#create, without passing any arguments, the default BrowserLaunchOption "
        + "should be set in the context.");

    BrowserContextOption expectedBrowserContextOption = BrowserContextOption.builder().build();
    IOption<?> actualBrowserContextOption = OptionCtx.getContext().get(OptionCtx.Key.BROWSER_CONTEXT_OPTION);
    Assertions.assertEquals(expectedBrowserContextOption, actualBrowserContextOption, "When BrowserContext is created"
        + "by invoking PlaywrightManager#create, without passing any arguments, the default BrowserContextOption "
        + "should be set in the context.");

    TracingStartOption expectedTraceStartOption = TracingStartOption.builder().build();
    IOption<?> actualTracingStartOption = OptionCtx.getContext().get(OptionCtx.Key.TRACE_START_OPTION);
    Assertions.assertEquals(expectedTraceStartOption, actualTracingStartOption, "When BrowserContext is created"
        + "by invoking PlaywrightManager#create the default TracingStartOption should be set in the context.");
  }
}