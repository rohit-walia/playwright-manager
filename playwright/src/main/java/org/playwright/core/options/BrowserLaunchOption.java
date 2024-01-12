package org.playwright.core.options;

import com.microsoft.playwright.BrowserType;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class BrowserLaunchOption implements IOption<BrowserType.LaunchOptions> {
  @Builder.Default
  boolean headless = true;

  @Builder.Default
  double slowmo = 300;

  @Builder.Default
  String browser = "chrome";

  @Builder.Default
  double browserStartTimeout = 30000;

  @Override
  public BrowserType.LaunchOptions forPlaywright() {
    BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
        .setHeadless(headless)
        .setSlowMo(slowmo)
        .setTimeout(browserStartTimeout);

    if ("chrome".equalsIgnoreCase(browser) || "msedge".equalsIgnoreCase(browser)) {
      launchOptions.setChannel(browser);
    }

    return launchOptions;
  }
}
