package org.playwright.core.options;


import com.microsoft.playwright.Tracing;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Value
@Builder(toBuilder = true)
@Jacksonized
public class TracingStopOption implements IOption<Tracing.StopOptions> {
  @Builder.Default
  Path tracingPath = Paths.get("target/trace/default.zip");

  @Override
  public Tracing.StopOptions forPlaywright() {
    log.info("TracingStopOptions: trace file recorded in directory: {}", tracingPath);
    return new Tracing.StopOptions().setPath(tracingPath);
  }
}
