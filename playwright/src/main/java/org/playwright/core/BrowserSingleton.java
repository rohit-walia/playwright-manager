package org.playwright.core;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.PlaywrightException;

public class BrowserSingleton {
  private static ThreadLocal<Browser> browser = new ThreadLocal<>();

  private BrowserSingleton() {
    throw new PlaywrightException("BrowserSingleton should not be instantiated!");
  }

  public static synchronized Browser getInstance() {
    return browser.get();
  }

  public static synchronized void setInstance(Browser browser) {
    BrowserSingleton.browser.set(browser);
  }
}
