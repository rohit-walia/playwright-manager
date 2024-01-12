package org.playwright.common;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.LoadState;
import lombok.extern.slf4j.Slf4j;
import org.playwright.failsafe.FailsafeFallback;
import org.playwright.failsafe.FailsafeRetry;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Utility class for common wait functions.
 */
@Slf4j
public class WaitUtils {

  /**
   * Wait for all page load states: onLoad, domContentLoad, and network. This function is wrapped
   * with Failsafe, so it will never halt execution. If there is an exception while waiting for
   * load states, it will be logged and execution will continue.
   *
   * <pre>
   * "load" - wait for the load event to be fired.
   * "domcontentloaded" - wait for the DOMContentLoaded event to be fired.
   * "networkidle" - wait until there are no network connections for at least 500 ms
   * </pre>
   *
   * @param page Page object
   */
  public static void waitForAllLoadStates(Page page) {
    FailsafeFallback.withLog(() -> {
      page.waitForLoadState(LoadState.LOAD);
      page.waitForLoadState(LoadState.DOMCONTENTLOADED);
      page.waitForLoadState(LoadState.NETWORKIDLE);
    }, "Error during waitForLoadState. Absorbing exception.");
  }

  /**
   * Get response text from navigation. Because Chrome clears network activity on navigation,
   * page.waitForResponse may sometimes throw exception in edge cases. This helper method tries to capture
   * the response in more than one way without throwing exception.
   *
   * @param page          page object
   * @param urlToNavigate url to navigate
   * @param urlToWaitFor  url to wait for
   * @return response body
   */
  public static String waitForResponseOnNavigation(Page page, String urlToNavigate, String urlToWaitFor) {
    AtomicReference<String> responseText = new AtomicReference<>();

    page.onResponse(r -> {
      if (r.url().contains(urlToWaitFor)) {
        responseText.set(r.text());
      }
    });

    Response response = page.waitForResponse(r -> r.url().contains(urlToWaitFor),
        () -> FailsafeRetry.withDefault(() -> page.navigate(urlToNavigate)));
    WaitUtils.waitForAllLoadStates(page);

    try {
      responseText.set(response.text());
    } catch (Exception e) {
      log.warn("Absorbing potential expected exception in waitForResponseOnNavigation function.");
    }
    return responseText.get();
  }
}
