package org.playwright.core.options;

import com.microsoft.playwright.Browser;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.awt.Dimension;
import java.nio.file.Paths;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class BrowserContextOption implements IOption<Browser.NewContextOptions> {
  @Builder.Default
  String recordVideoDir = "target/video/";

  @Builder.Default
  Dimension dimension = new Dimension(1920, 1080);

  @Override
  public Browser.NewContextOptions forPlaywright() {
    return new Browser.NewContextOptions()
        .setViewportSize(dimension.width, dimension.height)
        .setRecordVideoDir(Paths.get(recordVideoDir))
        .setRecordVideoSize(dimension.width, dimension.height);
  }
}
