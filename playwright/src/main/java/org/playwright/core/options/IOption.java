package org.playwright.core.options;

/**
 * Contract for Playwright Option builder objects.
 */
public interface IOption<T> {

  /**
   * Convert builder instance to Playwright Option object.
   *
   * @return Playwright Option
   */
  T forPlaywright();
}
