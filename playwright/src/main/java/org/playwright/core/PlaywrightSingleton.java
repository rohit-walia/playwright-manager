package org.playwright.core;

import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;

public class PlaywrightSingleton {
  private static ThreadLocal<Playwright> playwright = new ThreadLocal<>();

  private PlaywrightSingleton() {
    throw new PlaywrightException("PlaywrightSingleton should not be instantiated!");
  }

  public static Playwright getInstance() {
    return playwright.get();
  }

  public static void setInstance(Playwright playwright) {
    PlaywrightSingleton.playwright.set(playwright);
  }
}
