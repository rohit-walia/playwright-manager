package org.playwright.core.options;

import com.microsoft.playwright.Playwright;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class PlaywrightOption implements IOption<Playwright.CreateOptions> {
  @Builder.Default
  boolean enableDebugMode = false;

  @Override
  public Playwright.CreateOptions forPlaywright() {
    Playwright.CreateOptions options = new Playwright.CreateOptions();

    if (enableDebugMode) {
      options.setEnv(Map.of("PWDEBUG", "1", "PLAYWRIGHT_JAVA_SRC", "src/test/java"));
    }

    return options;
  }
}
