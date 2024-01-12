package org.playwright.core.options;

import com.microsoft.playwright.Page;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.nio.file.Path;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class ScreenshotOption implements IOption<Page.ScreenshotOptions> {
  @Builder.Default
  Path path = Path.of("target/screenshot/screenshot.png");

  @Builder.Default
  boolean fullPage = true;

  @Override
  public Page.ScreenshotOptions forPlaywright() {
    return new Page.ScreenshotOptions()
        .setPath(path)
        .setFullPage(fullPage);
  }
}
