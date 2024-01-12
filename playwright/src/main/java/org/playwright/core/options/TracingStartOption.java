package org.playwright.core.options;


import com.microsoft.playwright.Tracing;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class TracingStartOption implements IOption<Tracing.StartOptions> {
  @Builder.Default
  boolean enableScreenshot = true;

  @Builder.Default
  boolean enableSnapshot = true;

  @Builder.Default
  boolean enableSource = false;

  @Override
  public Tracing.StartOptions forPlaywright() {
    return new Tracing.StartOptions()
        .setScreenshots(enableScreenshot)
        .setSnapshots(enableSnapshot)
        .setSources(enableSource);
  }
}
