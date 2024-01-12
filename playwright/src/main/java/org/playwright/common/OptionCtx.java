package org.playwright.common;

import com.microsoft.playwright.PlaywrightException;
import lombok.Getter;
import org.playwright.core.options.IOption;

import java.util.LinkedHashMap;
import java.util.Map;

public class OptionCtx {
  @Getter
  private static final Map<OptionCtx.Key, IOption> context = new LinkedHashMap<>();

  private OptionCtx() {
    throw new PlaywrightException("OptionContext should not be instantiated!");
  }

  public static void add(OptionCtx.Key key, IOption value) {
    context.put(key, value);
  }

  public static void addAll(Map<OptionCtx.Key, IOption> optionContext) {
    context.putAll(optionContext);
  }

  public static void clearTestContext() {
    context.clear();
  }

  public static boolean exists(OptionCtx.Key key) {
    return context.containsKey(key);
  }

  public enum Key {
    PLAYWRIGHT_OPTION,
    BROWSER_LAUNCH_OPTION,
    BROWSER_CONTEXT_OPTION,
    SCREENSHOT_OPTION,
    TRACE_START_OPTION,
    TRACE_STOP_OPTION,
  }
}
